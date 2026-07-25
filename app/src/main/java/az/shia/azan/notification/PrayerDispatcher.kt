package az.shia.azan.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import az.shia.azan.calculator.PrayerTimesCalculator
import az.shia.azan.data.PrayerTime
import az.shia.azan.data.PrayerType
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.ShiaCities
import az.shia.azan.service.AzanForegroundService
import az.shia.azan.utils.TimeFormatter
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Azanın bir dəfə və vaxtında oxunmasını təmin edən mərkəzi nöqtə.
 *
 * Aqressiv OEM enerji idarəsi exact alarm-ı gecikdirə və ya tamamilə ata bilər.
 * Buna görə:
 *  - hər namaz üçün "yyyyMMdd-TYPE" markeri saxlanılır (təkrar oxumanın qarşısı);
 *  - alarm çox gecikibsə azan oxunmur, yalnız bildiriş göstərilir (20 dəqiqə
 *    sonra qəfil azan oxunmasın);
 *  - watchdog qaçırılmış namazı tapıb qısa gecikmə həddi daxilində oxudur.
 */
object PrayerDispatcher {

    private const val TAG = "PrayerDispatcher"

    /** Bu həddən sonra azan oxunmur, yalnız bildiriş göstərilir. */
    private const val AZAN_GRACE_MILLIS = 10 * 60 * 1000L

    /** Watchdog bu qədər geriyə baxaraq qaçırılmış namazı axtarır. */
    private const val CATCH_UP_WINDOW_MILLIS = 30 * 60 * 1000L

    /**
     * Alarm işlədikdə çağırılır. Bildirişi göstərir və gecikmə həddi daxilində
     * azanı oxudur. Təkrar çağırışlarda heç nə etmir.
     */
    suspend fun onPrayerAlarm(
        context: Context,
        prayerType: PrayerType,
        prayerName: String,
        prayerTimeText: String,
        scheduledAtMillis: Long?
    ) {
        val appContext = context.applicationContext
        val preferences = PreferencesManager(appContext)
        val settings = preferences.settingsFlow.first()

        if (!settings.isNotificationEnabled(prayerType)) {
            Log.d(TAG, "Skipped disabled prayer: $prayerName")
            return
        }

        val marker = marker(prayerType, scheduledAtMillis ?: System.currentTimeMillis())
        if (preferences.getLastHandledPrayer() == marker) {
            Log.d(TAG, "Already handled: $marker")
            return
        }
        preferences.setLastHandledPrayer(marker)

        NotificationHelper(appContext)
            .showPrayerNotification(prayerType, prayerName, prayerTimeText)

        val lateness = scheduledAtMillis?.let { System.currentTimeMillis() - it } ?: 0L
        if (lateness <= AZAN_GRACE_MILLIS) {
            startAzan(appContext, prayerType.name, prayerName)
            Log.d(TAG, "Azan started for $prayerName (lateness=${lateness}ms)")
        } else {
            Log.d(TAG, "Azan skipped, too late for $prayerName (lateness=${lateness}ms)")
        }
    }

    /**
     * Watchdog/tətbiq açılışı: alarm işləməyibsə, son qısa müddətdə keçmiş namazı
     * tapıb emal edir. Beləliklə OEM alarmı atsa da azan gecikmə həddi daxilində
     * oxunur.
     */
    suspend fun catchUpMissedPrayer(context: Context) {
        val appContext = context.applicationContext
        val preferences = PreferencesManager(appContext)
        val settings = preferences.settingsFlow.first()
        val location = preferences.getLastLocation().first() ?: ShiaCities.getDefaultCity()

        val now = Calendar.getInstance(TimeZone.getTimeZone(location.timeZone))
        val today = PrayerTimesCalculator().calculatePrayerTimes(
            date = now,
            location = location,
            method = settings.calculationMethod
        )

        val nowMillis = now.timeInMillis
        val missed: PrayerTime = today.getAllPrayers()
            .filter { it.type != PrayerType.SUNRISE }
            .filter { settings.isNotificationEnabled(it.type) }
            .filter { prayer ->
                val elapsed = nowMillis - prayer.time.timeInMillis
                elapsed in 0..CATCH_UP_WINDOW_MILLIS
            }
            .maxByOrNull { it.time.timeInMillis }
            ?: return

        onPrayerAlarm(
            context = appContext,
            prayerType = missed.type,
            prayerName = missed.name,
            prayerTimeText = TimeFormatter.formatTime(missed.time),
            scheduledAtMillis = missed.time.timeInMillis
        )
    }

    private fun marker(prayerType: PrayerType, scheduledAtMillis: Long): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = scheduledAtMillis }
        val day = String.format(
            Locale.US,
            "%04d%02d%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        return "$day-${prayerType.name}"
    }

    private fun startAzan(context: Context, prayerTypeName: String, prayerName: String) {
        try {
            val serviceIntent = Intent(context, AzanForegroundService::class.java).apply {
                action = AzanForegroundService.ACTION_START_AZAN
                putExtra(AzanForegroundService.EXTRA_PRAYER_TYPE, prayerTypeName)
                putExtra(AzanForegroundService.EXTRA_PRAYER_NAME, prayerName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting azan foreground service", e)
        }
    }
}
