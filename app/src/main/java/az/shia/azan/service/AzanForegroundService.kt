package az.shia.azan.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import az.shia.azan.MainActivity
import az.shia.azan.R
import az.shia.azan.audio.AzanPlayer
import az.shia.azan.data.PrayerType

/**
 * 🔋 Arxa fonda azan oxutmaq üçün Foreground Service
 */
class AzanForegroundService : Service() {
    
    private lateinit var azanPlayer: AzanPlayer
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    
    companion object {
        const val CHANNEL_ID = "azan_playback_channel"
        const val NOTIFICATION_ID = 2001
        
        const val ACTION_START_AZAN = "az.shia.azan.START_AZAN"
        const val ACTION_STOP_AZAN = "az.shia.azan.STOP_AZAN"
        const val EXTRA_PRAYER_TYPE = "prayer_type"
        const val EXTRA_PRAYER_NAME = "prayer_name"
    }
    
    override fun onCreate() {
        super.onCreate()
        azanPlayer = AzanPlayer(applicationContext)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_AZAN -> {
                val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE)
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "Namaz"
                
                // Audio Focus tələb et (digər səsləri dayandırmaq üçün)
                requestAudioFocus()
                
                // Foreground service başlat
                startForeground(NOTIFICATION_ID, createForegroundNotification(prayerName))
                
                // Azan oxut
                prayerTypeName?.let {
                    try {
                        val prayerType = PrayerType.valueOf(it)
                        azanPlayer.playAzan(prayerType) {
                            stopSelf()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        stopSelf()
                    }
                }
            }
            ACTION_STOP_AZAN -> {
                azanPlayer.stop()
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .build()
            
            audioManager?.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }
    
    override fun onDestroy() {
        azanPlayer.release()
        releaseWakeLock()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    /**
     * Foreground notification yaradır
     */
    private fun createForegroundNotification(prayerName: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Dayandırma action
        val stopIntent = Intent(this, AzanForegroundService::class.java).apply {
            action = ACTION_STOP_AZAN
        }
        
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🕌 $prayerName Azanı")
            .setContentText("Azan oxunur...")
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.app_logo))
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_stop,
                "Dayandır",
                stopPendingIntent
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
    
    /**
     * Notification channel yaradır
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Azan Xidməti",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Arxa fonda azan oxutma xidməti"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Wake Lock al - ekran bağlı olsa belə işləsin
     */
    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ShiaAzan::AzanWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 dəqiqə maksimum
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Wake Lock-u burax
     */
    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
