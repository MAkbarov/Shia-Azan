package az.shia.azan.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

/** Yoxlanılmış APK-ni sistem quraşdırıcısı ilə açan köməkçi. */
object UpdateInstaller {

    /** Android 8+ üçün "naməlum mənbədən quraşdırma" icazəsi verilibmi. */
    fun canRequestInstall(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatching { context.packageManager.canRequestPackageInstalls() }.getOrDefault(false)
        } else {
            true
        }
    }

    /** İstifadəçini bu tətbiq üçün "naməlum mənbə" icazəsi ekranına yönləndirir. */
    fun requestInstallPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        return runCatching {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    /**
     * Yoxlanılmış APK üçün sistem quraşdırıcısını açır. Uğur olduqda true.
     * Fayl yalnız tətbiqin öz updates/ qovluğundan qəbul edilir.
     */
    fun launchInstaller(context: Context, apk: File): Boolean {
        return runCatching {
            val updatesDir = File(context.applicationContext.filesDir, "updates").canonicalFile
            val canonical = apk.canonicalFile
            if (canonical.parentFile != updatesDir || !canonical.isFile) return false

            val uri = FileProvider.getUriForFile(
                context.applicationContext,
                "${context.packageName}.fileprovider",
                canonical
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
