package az.shia.azan.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import az.shia.azan.data.PreferencesManager
import az.shia.azan.service.OngoingNotificationService
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Aqressiv OEM enerji idarəsi alarmları ata və foreground service-i öldürə bilir.
 * Watchdog periodik olaraq:
 *  - namaz alarmlarını yenidən qurur;
 *  - daimi bildiriş aktivdirsə vidceti bərpa edir;
 *  - alarm işləməyibsə qaçırılmış namazı qısa gecikmə həddi daxilində oxudur.
 */
object PrayerWatchdog {

    private const val PERIODIC_NAME = "prayer_watchdog_periodic_v1"
    private const val IMMEDIATE_NAME = "prayer_watchdog_now_v1"
    const val TAG = "prayer_watchdog_v1"

    fun schedule(context: Context) {
        val appContext = context.applicationContext
        val periodic = PeriodicWorkRequestBuilder<PrayerWatchdogWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().build())
            .addTag(TAG)
            .build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic
        )
        runNow(appContext)
    }

    /** Tətbiq açılışı və boot kimi hallarda dərhal bir dəfə işlət. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<PrayerWatchdogWorker>()
            .addTag(TAG)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            IMMEDIATE_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

class PrayerWatchdogWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Alarmlar itibsə yenidən qur (azan heç vaxt dayanmasın).
        runCatching { PrayerAlarmPlanner.reschedule(applicationContext) }

        // Daimi bildiriş aktivdirsə, öldürülmüş vidceti bərpa et.
        runCatching {
            val settings = PreferencesManager(applicationContext).settingsFlow.first()
            if (settings.ongoingNotificationEnabled) {
                OngoingNotificationService.startService(applicationContext)
            }
        }

        // Alarm işləməyibsə qaçırılmış namazı emal et.
        runCatching { PrayerDispatcher.catchUpMissedPrayer(applicationContext) }

        // Avtomatik yeniləmə aktivdirsə, yoxlamanı da bu dövrədə tetiklə:
        // OEM cihazlarda periodik update işi tək-tək tormozlana bilir.
        runCatching {
            val settings = PreferencesManager(applicationContext).settingsFlow.first()
            if (settings.automaticUpdatesEnabled) {
                az.shia.azan.update.UpdateScheduler.checkNow(applicationContext)
            }
        }

        return Result.success()
    }
}
