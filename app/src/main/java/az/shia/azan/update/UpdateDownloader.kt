package az.shia.azan.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.system.Os
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

object UpdateDownloader {
    private const val KEY_VERSION = "version"
    private const val KEY_DOWNLOAD_URL = "download_url"
    private const val KEY_RELEASE_URL = "release_url"

    fun enqueue(context: Context, info: UpdateInfo) {
        val version = info.version.trim()
        val downloadUrl = GitHubUrlPolicy.parseAllowed(info.downloadUrl)?.toExternalForm()
        val releaseUrl = GitHubUrlPolicy.parseReleasePage(info.releaseUrl)?.toExternalForm()
            ?: GitHubUrlPolicy.RELEASES_PAGE
        if (version.isEmpty() || downloadUrl == null) {
            UpdateNotificationHelper.showDownloadFailure(
                context.applicationContext,
                "Yeniləmə ünvanı etibarsızdır.",
                releaseUrl
            )
            return
        }

        val input = workDataOf(
            KEY_VERSION to version,
            KEY_DOWNLOAD_URL to downloadUrl,
            KEY_RELEASE_URL to releaseUrl
        )
        val request = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
            .setInputData(input)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30_000L,
                TimeUnit.MILLISECONDS
            )
            .addTag(UpdateWorkNames.TAG)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UpdateWorkNames.DOWNLOAD_PREFIX + stableWorkToken(version),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private fun stableWorkToken(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .take(12)
            .joinToString("") { "%02x".format(it) }

    internal fun readInfo(data: Data): UpdateInfo? {
        val version = data.getString(KEY_VERSION)?.trim().orEmpty()
        val downloadUrl = data.getString(KEY_DOWNLOAD_URL).orEmpty()
        val releaseUrl = data.getString(KEY_RELEASE_URL).orEmpty()
        if (version.isEmpty() || downloadUrl.isEmpty()) return null
        return UpdateInfo(version, downloadUrl, releaseUrl, "")
    }
}

class UpdateDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val info = UpdateDownloader.readInfo(inputData) ?: return Result.failure()
        var temporaryFile: File? = null
        return try {
            val apk = withContext(Dispatchers.IO) {
                downloadAndVerify(info) { temporaryFile = it }
            }
            UpdateNotificationHelper.showVerifiedApkReady(applicationContext, info, apk)
            Result.success()
        } catch (cancelled: CancellationException) {
            temporaryFile?.delete()
            throw cancelled
        } catch (security: SecurityException) {
            // İmza/paket uyğunsuzluğu təkrar cəhdlə düzəlməz; dərhal fallback göstər.
            temporaryFile?.delete()
            UpdateNotificationHelper.showDownloadFailure(
                applicationContext,
                security.message ?: "APK təhlükəsizlik yoxlamasından keçmədi.",
                info.releaseUrl
            )
            Result.failure()
        } catch (error: Exception) {
            temporaryFile?.delete()
            // Şəbəkə/timeout kimi keçici xətalarda backoff ilə təkrar cəhd et ki,
            // yenilənmə nəticədə uğurla tamamlansın.
            if (runAttemptCount < MAX_ATTEMPTS) {
                Result.retry()
            } else {
                UpdateNotificationHelper.showDownloadFailure(
                    applicationContext,
                    "Yeniləmə endirilə bilmədi. GitHub-dan əl ilə endirin.",
                    info.releaseUrl
                )
                Result.failure(
                    workDataOf("error" to (error.message ?: "Update download failed").take(500))
                )
            }
        }
    }

    private fun downloadAndVerify(info: UpdateInfo, onTempFile: (File) -> Unit): File {
        val downloadUrl = GitHubUrlPolicy.parseAllowed(info.downloadUrl)
            ?.toExternalForm()
            ?: throw SecurityException("Yalnız icazəli GitHub HTTPS ünvanları qəbul edilir.")
        val safeVersion = info.version
            .replace(Regex("[^0-9A-Za-z._-]"), "_")
            .trim('.', '_', '-')
            .take(80)
            .takeIf(String::isNotEmpty)
            ?: throw IOException("Versiya fayl adı üçün etibarsızdır.")

        val updatesDirectory = File(applicationContext.filesDir, "updates")
        if ((!updatesDirectory.exists() && !updatesDirectory.mkdirs()) || !updatesDirectory.isDirectory) {
            throw IOException("Yeniləmə qovluğu yaradıla bilmədi.")
        }
        val canonicalDirectory = updatesDirectory.canonicalFile
        val destination = File(canonicalDirectory, "XIV-Azan-$safeVersion.apk").canonicalFile
        if (destination.parentFile != canonicalDirectory) {
            throw SecurityException("Etibarsız yeniləmə fayl yolu.")
        }

        if (destination.exists()) {
            val existing = ApkVerifier.verify(applicationContext, destination)
            if (existing.valid) return destination
            if (!destination.delete() && destination.exists()) {
                throw IOException("Etibarsız köhnə APK faylı silinə bilmədi.")
            }
        }

        // Keep the temporary name APK-parseable for PackageManager verification.
        val temporary = File(canonicalDirectory, ".XIV-Azan-$safeVersion.part.apk")
        onTempFile(temporary)
        temporary.delete()

        try {
            downloadToTemporaryFile(downloadUrl, temporary)
            val verification = ApkVerifier.verify(applicationContext, temporary)
            if (!verification.valid) {
                throw SecurityException(verification.message)
            }
            Os.rename(temporary.absolutePath, destination.absolutePath)
            return destination
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }

    private fun downloadToTemporaryFile(downloadUrl: String, temporary: File) {
        val connection = GitHubNetwork.openFollowingRedirects(
            downloadUrl,
            "application/vnd.android.package-archive, application/octet-stream;q=0.9"
        )
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("APK endirilməsi HTTP ${connection.responseCode} xətası verdi.")
            }
            val declaredLength = connection.contentLengthLong
            if (declaredLength > MAX_APK_BYTES) {
                throw IOException("APK icazə verilən ölçüdən böyükdür.")
            }

            var total = 0L
            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(temporary).use { fileOutput ->
                    val output = BufferedOutputStream(fileOutput)
                    val buffer = ByteArray(32 * 1024)
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_APK_BYTES) {
                            throw IOException("APK icazə verilən ölçüdən böyükdür.")
                        }
                        output.write(buffer, 0, count)
                    }
                    output.flush()
                    fileOutput.fd.sync()
                }
            }
            // Content-Length bəzən yanlış/olmaya bilər; bütövlüyü aşağıdakı
            // PackageManager/imza yoxlaması təmin edir, ona görə burada yalnız
            // tamam boş cavabı rədd edirik.
            if (total <= 0L) {
                throw IOException("APK məzmunu boş endirildi.")
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val MAX_APK_BYTES = 512L * 1024L * 1024L
        const val MAX_ATTEMPTS = 5
    }
}

private data class ApkVerification(val valid: Boolean, val message: String)

private object ApkVerifier {
    @Suppress("DEPRECATION")
    fun verify(context: Context, apk: File): ApkVerification {
        if (!apk.isFile || apk.length() <= 0L) {
            return ApkVerification(false, "APK faylı boş və ya əlçatmazdır.")
        }

        return try {
            val packageManager = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                PackageManager.GET_SIGNATURES
            }
            val archive = packageManager.getPackageArchiveInfo(apk.absolutePath, flags)
                ?: return ApkVerification(false, "APK paket məlumatı oxuna bilmədi.")
            val installed = packageManager.getPackageInfo(context.packageName, flags)

            if (archive.packageName != context.packageName) {
                return ApkVerification(false, "APK paket adı tətbiqlə uyğun deyil.")
            }
            if (longVersionCode(archive) <= longVersionCode(installed)) {
                return ApkVerification(false, "APK versiyası quraşdırılmış versiyadan yeni deyil.")
            }

            val archiveCertificates = certificateSet(archive)
            val installedCertificates = certificateSet(installed)
            if (archiveCertificates.isEmpty() || archiveCertificates != installedCertificates) {
                return ApkVerification(false, "APK imza sertifikatları tətbiqlə uyğun deyil.")
            }
            ApkVerification(true, "OK")
        } catch (_: PackageManager.NameNotFoundException) {
            ApkVerification(false, "Quraşdırılmış tətbiqin paket məlumatı tapılmadı.")
        } catch (_: Exception) {
            ApkVerification(false, "APK təhlükəsizlik yoxlaması uğursuz oldu.")
        }
    }

    @Suppress("DEPRECATION")
    private fun longVersionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            info.versionCode.toLong()
        }

    @Suppress("DEPRECATION")
    private fun certificateSet(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            info.signatures
        }.orEmpty()
        return signatures.map { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { "%02x".format(it) }
        }.toSet()
    }
}
