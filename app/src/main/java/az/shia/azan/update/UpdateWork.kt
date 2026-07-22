package az.shia.azan.update

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import az.shia.azan.BuildConfig
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

internal object UpdateWorkNames {
    const val CHECK_NOW = "app_update_check_now_v1"
    const val CHECK_PERIODIC = "app_update_check_periodic_v1"
    const val DOWNLOAD_PREFIX = "app_update_download_v1_"
    const val TAG = "app_updates_v1"
}

object UpdateScheduler {
    fun schedule(context: Context) {
        val appContext = context.applicationContext
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val immediate = OneTimeWorkRequestBuilder<UpdateCheckWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(UpdateWorkNames.TAG)
            .build()
        val periodic = PeriodicWorkRequestBuilder<UpdateCheckWorker>(12, TimeUnit.HOURS)
            .setInitialDelay(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(UpdateWorkNames.TAG)
            .build()

        WorkManager.getInstance(appContext).apply {
            enqueueUniqueWork(
                UpdateWorkNames.CHECK_NOW,
                ExistingWorkPolicy.REPLACE,
                immediate
            )
            enqueueUniquePeriodicWork(
                UpdateWorkNames.CHECK_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodic
            )
        }
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext)
            .cancelAllWorkByTag(UpdateWorkNames.TAG)
    }
}

class UpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!automaticUpdatesEnabled(applicationContext)) return Result.success()

        return try {
            when (val result = UpdateRepository().check(BuildConfig.VERSION_NAME)) {
                is UpdateCheckResult.Available -> {
                    UpdateNotificationHelper.showUpdateAvailable(applicationContext, result.info)
                    UpdateDownloader.enqueue(applicationContext, result.info)
                }

                is UpdateCheckResult.UpToDate -> Unit
                is UpdateCheckResult.Failure -> {
                    // Keçici şəbəkə/timeout xətasında backoff ilə təkrar cəhd et.
                    if (UpdateFailureMessages.isTransient(result.message) &&
                        runAttemptCount < MAX_CHECK_ATTEMPTS
                    ) {
                        return Result.retry()
                    }
                    if (UpdateFailureMessages.recommendsReleaseFallback(result.message)) {
                        UpdateNotificationHelper.showReleaseFallback(
                            applicationContext,
                            result.message,
                            GitHubUrlPolicy.RELEASES_PAGE
                        )
                    }
                }
            }
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Periodic work will check again at the next interval; never create a retry storm.
            Result.success()
        }
    }

    private companion object {
        const val MAX_CHECK_ATTEMPTS = 5
    }
}
