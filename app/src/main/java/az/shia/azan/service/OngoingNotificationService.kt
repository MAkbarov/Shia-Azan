package az.shia.azan.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import az.shia.azan.MainActivity
import az.shia.azan.R
import az.shia.azan.calculator.PrayerTimesCalculator
import az.shia.azan.data.AppSettings
import az.shia.azan.data.LocationData
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.ShiaCities
import az.shia.azan.utils.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 📍 Daimi Bildiriş Servisi
 * Status barda ikonun qalmasını və namaz vaxtlarının göstərilməsini təmin edir
 */
class OngoingNotificationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private val calculator = PrayerTimesCalculator()
    
    companion object {
        const val CHANNEL_ID = "ongoing_prayer_times"
        const val NOTIFICATION_ID = 3001
        
        fun startService(context: Context) {
            val intent = Intent(context, OngoingNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, OngoingNotificationService::class.java)
            context.stopService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        createNotificationChannel()
        
        // Parametrləri və vaxtları izlə
        serviceScope.launch {
            combine(
                preferencesManager.settingsFlow,
                preferencesManager.getLastLocation()
            ) { settings, location ->
                Pair(settings, location)
            }.collectLatest { (settings, location) ->
                if (settings.ongoingNotificationEnabled) {
                    val currentLocation = location ?: ShiaCities.getDefaultCity()
                    updateNotification(settings, currentLocation)
                } else {
                    stopSelf()
                }
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
    
    private fun updateNotification(settings: AppSettings, location: LocationData) {
        val prayerTimes = calculator.calculatePrayerTimes(Calendar.getInstance(), location)
        val notification = createNotification(prayerTimes, settings)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(prayerTimes: az.shia.azan.data.DailyPrayerTimes, settings: AppSettings): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val prayers = prayerTimes.getAllPrayers()
        val contentText = StringBuilder()
        
        prayers.forEach { prayer ->
            val enabled = when (prayer.type) {
                az.shia.azan.data.PrayerType.FAJR -> settings.fajrNotificationEnabled
                az.shia.azan.data.PrayerType.DHUHR -> settings.dhuhrNotificationEnabled
                az.shia.azan.data.PrayerType.ASR -> settings.asrNotificationEnabled
                az.shia.azan.data.PrayerType.MAGHRIB -> settings.maghribNotificationEnabled
                az.shia.azan.data.PrayerType.ISHA -> settings.ishaNotificationEnabled
                else -> false
            }
            
            if (enabled) {
                if (contentText.isNotEmpty()) contentText.append(" | ")
                contentText.append("${prayer.name}: ${TimeFormatter.formatTime(prayer.time)}")
            }
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🕌 XIV Azan - Namaz Vaxtları")
            .setContentText(if (contentText.isEmpty()) "Heç bir namaz aktiv deyil" else contentText.toString())
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_logo))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daimi Namaz Vaxtları",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Yuxarı panelda sabit qalan namaz vaxtları"
                setShowBadge(false)
                setSound(null, null)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
