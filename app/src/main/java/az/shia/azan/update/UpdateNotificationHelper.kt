package az.shia.azan.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import az.shia.azan.R
import java.io.File

object UpdateNotificationHelper {
    const val CHANNEL_ID = "app_updates_v1"

    private const val NOTIFICATION_AVAILABLE = 71_001
    private const val NOTIFICATION_FAILURE = 71_002
    private const val NOTIFICATION_READY = 71_003
    private const val REQUEST_RELEASE_AVAILABLE = 71_101
    private const val REQUEST_RELEASE_FAILURE = 71_102
    private const val REQUEST_INSTALL = 71_103
    private const val REQUEST_RELEASE_READY = 71_104
    private const val APK_MIME_TYPE = "application/vnd.android.package-archive"

    fun showUpdateAvailable(context: Context, info: UpdateInfo) {
        val appContext = context.applicationContext
        if (!canNotify(appContext)) return
        ensureChannel(appContext)
        val releaseIntent = releasePendingIntent(
            appContext,
            info.releaseUrl,
            REQUEST_RELEASE_AVAILABLE
        )
        val notes = info.releaseNotes.trim().take(2_000)
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Yeni versiya mövcuddur: ${info.version}")
            .setContentText("Buraxılış səhifəsini açmaq üçün toxunun.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    notes.ifEmpty { "Yeniləmə avtomatik endiriləcək." }
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(releaseIntent)
            .build()
        notifySafely(appContext, NOTIFICATION_AVAILABLE, notification)
    }

    fun showDownloadFailure(context: Context, message: String, releaseUrl: String) {
        val appContext = context.applicationContext
        if (!canNotify(appContext)) return
        ensureChannel(appContext)
        val releaseIntent = releasePendingIntent(
            appContext,
            releaseUrl,
            REQUEST_RELEASE_FAILURE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Yeniləmə endirilə bilmədi")
            .setContentText(message.take(250))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.take(2_000)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setContentIntent(releaseIntent)
            .addAction(R.drawable.ic_notification, "GitHub-da aç", releaseIntent)
            .build()
        notifySafely(appContext, NOTIFICATION_FAILURE, notification)
    }

    fun showReleaseFallback(context: Context, message: String, releaseUrl: String) {
        val appContext = context.applicationContext
        if (!canNotify(appContext)) return
        ensureChannel(appContext)
        val releaseIntent = releasePendingIntent(
            appContext,
            releaseUrl,
            REQUEST_RELEASE_FAILURE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Yeniləməni GitHub-da yoxlayın")
            .setContentText(message.take(250))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.take(2_000)))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(releaseIntent)
            .addAction(R.drawable.ic_notification, "Buraxılışlar", releaseIntent)
            .build()
        notifySafely(appContext, NOTIFICATION_FAILURE, notification)
    }

    fun showVerifiedApkReady(context: Context, info: UpdateInfo, apk: File) {
        val appContext = context.applicationContext
        if (!canNotify(appContext)) return
        ensureChannel(appContext)

        val updatesDirectory = runCatching {
            File(appContext.filesDir, "updates").canonicalFile
        }.getOrNull()
        val canonicalApk = runCatching { apk.canonicalFile }.getOrNull()
        if (updatesDirectory == null ||
            canonicalApk == null ||
            canonicalApk.parentFile != updatesDirectory ||
            !canonicalApk.isFile
        ) {
            showDownloadFailure(appContext, "Yoxlanılmış APK faylı əlçatan deyil.", info.releaseUrl)
            return
        }

        val contentUri = runCatching {
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                canonicalApk
            )
        }.getOrElse {
            showDownloadFailure(
                appContext,
                "APK quraşdırıcısı hazırlana bilmədi. GitHub buraxılış səhifəsini açın.",
                info.releaseUrl
            )
            return
        }
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val installPendingIntent = PendingIntent.getActivity(
            appContext,
            REQUEST_INSTALL,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val releaseIntent = releasePendingIntent(
            appContext,
            info.releaseUrl,
            REQUEST_RELEASE_READY
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Yeniləmə quraşdırılmağa hazırdır")
            .setContentText("XIV-Azan ${info.version} APK-si yoxlanıldı.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Paket adı, daha yeni versiya və tətbiqin imza sertifikatları yoxlanıldı. " +
                        "Quraşdırmaq üçün toxunun."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(installPendingIntent)
            .addAction(R.drawable.ic_notification, "Quraşdır", installPendingIntent)
            .addAction(R.drawable.ic_notification, "GitHub", releaseIntent)
            .build()

        NotificationManagerCompat.from(appContext).apply {
            cancel(NOTIFICATION_AVAILABLE)
            cancel(NOTIFICATION_FAILURE)
        }
        notifySafely(appContext, NOTIFICATION_READY, notification)
    }

    private fun releasePendingIntent(
        context: Context,
        rawReleaseUrl: String,
        requestCode: Int
    ): PendingIntent {
        val releaseUrl = GitHubUrlPolicy.parseReleasePage(rawReleaseUrl)
            ?: requireNotNull(GitHubUrlPolicy.parseReleasePage(GitHubUrlPolicy.RELEASES_PAGE))
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(releaseUrl.toExternalForm())).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Tətbiq yeniləmələri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Yeni GitHub buraxılışları və yoxlanılmış APK bildirişləri"
            }
        )
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return runCatching {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }.getOrDefault(false)
    }

    private fun notifySafely(context: Context, id: Int, notification: android.app.Notification) {
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }
}
