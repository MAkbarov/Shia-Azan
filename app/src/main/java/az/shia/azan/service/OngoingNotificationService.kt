package az.shia.azan.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import az.shia.azan.MainActivity
import az.shia.azan.R
import az.shia.azan.calculator.PrayerTimesCalculator
import az.shia.azan.data.AppSettings
import az.shia.azan.data.DailyPrayerTimes
import az.shia.azan.data.LocationData
import az.shia.azan.data.PrayerTime
import az.shia.azan.data.PrayerType
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.ShiaCities
import az.shia.azan.utils.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/** Status, bildiriş paneli və kilid ekranında canlı namaz vaxtları. */
class OngoingNotificationService : Service() {

    private data class NotificationSnapshot(
        val prayerTimes: DailyPrayerTimes,
        val nextPrayer: PrayerTime,
        val isTomorrow: Boolean,
        val now: Calendar
    )

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var alarmManager: AlarmManager
    private val calculator = PrayerTimesCalculator()
    private var currentSettings: AppSettings? = null
    private var currentLocation: LocationData? = null

    companion object {
        const val CHANNEL_ID = "ongoing_prayer_times_v2"
        const val NOTIFICATION_ID = 3001
        const val ACTION_REFRESH = "az.shia.azan.action.REFRESH_PRAYER_TIMES"
        private const val REFRESH_REQUEST_CODE = 7301

        fun startService(context: Context) {
            startService(context, null)
        }

        fun refresh(context: Context) {
            startService(context, ACTION_REFRESH)
        }

        private fun startService(context: Context, action: String?) {
            val intent = Intent(context, OngoingNotificationService::class.java).apply {
                this.action = action
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, OngoingNotificationService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()

        serviceScope.launch {
            combine(
                preferencesManager.settingsFlow,
                preferencesManager.getLastLocation()
            ) { settings, location ->
                settings to (location ?: ShiaCities.getDefaultCity())
            }.collect { (settings, location) ->
                currentSettings = settings
                currentLocation = location
                if (settings.ongoingNotificationEnabled) {
                    updateNotification(settings, location)
                } else {
                    stopOngoingNotification()
                }
            }
        }

        // Qalan vaxtı telefonun real dəqiqə sərhədindən dərhal sonra yenilə.
        // Service hansı saniyədə başlasa da countdown tam bir dəqiqə köhnə qalmır.
        // Prayer sərhədi üçün aşağıda ayrıca AlarmManager istifadə olunur ki,
        // doze/OEM throttling-də növbəti namaz köhnə namazda ilişməsin.
        serviceScope.launch {
            while (isActive) {
                val untilNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
                delay(untilNextMinute + 250L)
                refreshFromCurrentState()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_REFRESH) {
            refreshFromCurrentState()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun refreshFromCurrentState() {
        val settings = currentSettings
        val location = currentLocation
        if (settings?.ongoingNotificationEnabled == true && location != null) {
            updateNotification(settings, location)
        }
    }

    private fun updateNotification(settings: AppSettings, location: LocationData) {
        val snapshot = calculateSnapshot(settings, location)
        startForeground(NOTIFICATION_ID, createNotification(snapshot))
        scheduleBoundaryRefresh(snapshot.nextPrayer.time.timeInMillis + 1_000L)
    }

    private fun calculateSnapshot(
        settings: AppSettings,
        location: LocationData
    ): NotificationSnapshot {
        val now = Calendar.getInstance(TimeZone.getTimeZone(location.timeZone))
        val todayTimes = calculator.calculatePrayerTimes(
            date = now,
            location = location,
            method = settings.calculationMethod
        )
        val nextToday = todayTimes.getNextPrayer(now)
        if (nextToday != null) {
            return NotificationSnapshot(todayTimes, nextToday, false, now)
        }

        val tomorrow = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrowTimes = calculator.calculatePrayerTimes(
            date = tomorrow,
            location = location,
            method = settings.calculationMethod
        )
        val tomorrowFajr = tomorrowTimes.getAllPrayers().first {
            it.type == PrayerType.FAJR
        }
        return NotificationSnapshot(tomorrowTimes, tomorrowFajr, true, now)
    }

    private fun stopOngoingNotification() {
        cancelBoundaryRefresh()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun createNotification(snapshot: NotificationSnapshot): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextTime = TimeFormatter.formatTime(snapshot.nextPrayer.time)
        val remaining = TimeFormatter.getTimeRemaining(snapshot.nextPrayer.time, snapshot.now)
        val prefix = if (snapshot.isTomorrow) "Sabah" else "Növbəti"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("XIV Azan • ${snapshot.prayerTimes.locationName}")
            .setContentText("$prefix: ${snapshot.nextPrayer.name} $nextTime • $remaining qalıb")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF22BFC1.toInt())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setCustomContentView(createCompactView(snapshot, remaining))
            .setCustomBigContentView(createExpandedView(snapshot, remaining))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createCompactView(
        snapshot: NotificationSnapshot,
        remaining: String
    ): RemoteViews {
        val prefix = if (snapshot.isTomorrow) "Sabah" else "Növbəti"
        return RemoteViews(packageName, R.layout.notification_prayer_times_compact).apply {
            setTextViewText(
                R.id.notification_next_name,
                "$prefix • ${snapshot.nextPrayer.name}"
            )
            setTextViewText(
                R.id.notification_city,
                "${snapshot.prayerTimes.locationName} • $remaining qalıb"
            )
            setTextViewText(
                R.id.notification_next_time,
                TimeFormatter.formatTime(snapshot.nextPrayer.time)
            )
        }
    }

    private fun createExpandedView(
        snapshot: NotificationSnapshot,
        remaining: String
    ): RemoteViews {
        val prefix = if (snapshot.isTomorrow) "Sabah" else "Növbəti"
        return RemoteViews(packageName, R.layout.notification_prayer_times_expanded).apply {
            setTextViewText(
                R.id.notification_expanded_title,
                if (snapshot.isTomorrow) "Sabahın namaz vaxtları" else "Bu günün namaz vaxtları"
            )
            setTextViewText(R.id.notification_expanded_city, snapshot.prayerTimes.locationName)
            setTextViewText(
                R.id.notification_expanded_next,
                "$prefix ${snapshot.nextPrayer.name} ${TimeFormatter.formatTime(snapshot.nextPrayer.time)} • $remaining"
            )
            setTextViewText(R.id.time_fajr, TimeFormatter.formatTime(snapshot.prayerTimes.fajr))
            setTextViewText(R.id.time_dhuhr, TimeFormatter.formatTime(snapshot.prayerTimes.dhuhr))
            setTextViewText(R.id.time_asr, TimeFormatter.formatTime(snapshot.prayerTimes.asr))
            setTextViewText(R.id.time_maghrib, TimeFormatter.formatTime(snapshot.prayerTimes.maghrib))
            setTextViewText(R.id.time_isha, TimeFormatter.formatTime(snapshot.prayerTimes.isha))
        }
    }

    /** Növbəti namazdan 1 saniyə sonra exact refresh; disabled prayer-lərdən asılı deyil. */
    private fun scheduleBoundaryRefresh(triggerAtMillis: Long) {
        val pendingIntent = boundaryRefreshPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun cancelBoundaryRefresh() {
        val pendingIntent = boundaryRefreshPendingIntent(PendingIntent.FLAG_NO_CREATE)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun boundaryRefreshPendingIntent(extraFlag: Int): PendingIntent {
        val intent = Intent(this, OngoingNotificationService::class.java).apply {
            action = ACTION_REFRESH
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or extraFlag
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, REFRESH_REQUEST_CODE, intent, flags)
        } else {
            PendingIntent.getService(this, REFRESH_REQUEST_CODE, intent, flags)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daimi Namaz Vaxtları",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status və kilid ekranında canlı namaz vaxtları"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
