package az.shia.azan.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Aqressiv OEM enerji idarəsi tətbiqi öldürüb alarmları ata bilir.
 * Bu köməkçi istehsalçının "Avtomatik başlatma (Autostart)" ekranını açır;
 * tapılmasa tətbiq parametrlərinə yönləndirir.
 */
object AutoStartHelper {

    private val autoStartComponents = listOf(
        // Xiaomi / Redmi / POCO
        "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
        // Huawei / Honor
        "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        "com.huawei.systemmanager" to "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity",
        "com.hihonor.systemmanager" to "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        // Oppo / Realme
        "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
        "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
        "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
        // Vivo
        "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
        "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
        // Samsung
        "com.samsung.android.lool" to "com.samsung.android.sm.ui.battery.BatteryActivity",
        // Asus / Letv / Meizu / OnePlus
        "com.asus.mobilemanager" to "com.asus.mobilemanager.autostart.AutoStartActivity",
        "com.letv.android.letvsafe" to "com.letv.android.letvsafe.AutobootManageActivity",
        "com.meizu.safe" to "com.meizu.safe.security.SHOW_APPSEC",
        "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
    )

    /** Cihazda tanınan autostart ekranı varmı. */
    fun isSupported(context: Context): Boolean = resolveIntent(context) != null

    /**
     * Autostart ekranını açır. Tanınan ekran yoxdursa tətbiq parametrlərini açır.
     * Uğur olduqda true qaytarır.
     */
    fun open(context: Context): Boolean {
        resolveIntent(context)?.let { intent ->
            return runCatching {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            }.getOrDefault(false)
        }
        return openAppSettings(context)
    }

    /** Tətbiqin sistem parametrləri (autostart tapılmadıqda ehtiyat yol). */
    fun openAppSettings(context: Context): Boolean = runCatching {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
    }.getOrDefault(false)

    private fun resolveIntent(context: Context): Intent? {
        val packageManager = context.packageManager
        for ((pkg, cls) in autoStartComponents) {
            val intent = Intent().apply { component = ComponentName(pkg, cls) }
            val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    android.content.pm.PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, 0)
            }
            if (resolved.isNotEmpty()) return intent
        }
        return null
    }
}
