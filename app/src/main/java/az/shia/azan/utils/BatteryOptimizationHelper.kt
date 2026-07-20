package az.shia.azan.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 🔋 Battery Optimization Helper
 * Android'in enerji optimallaşdırmasını disable edir
 */
class BatteryOptimizationHelper(private val activity: ComponentActivity) {
    
    private var batteryOptimizationLauncher: ActivityResultLauncher<Intent>? = null
    
    /**
     * Launcher-i register et (onCreate-də çağrılmalıdır)
     */
    fun registerLauncher(onResult: (Boolean) -> Unit) {
        batteryOptimizationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            onResult(isIgnoringBatteryOptimizations(activity))
        }
    }
    
    /**
     * Battery optimization ignore olunurmu?
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }
    
    /**
     * Battery optimization disable etmək üçün dialog göstər
     */
    fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isIgnoringBatteryOptimizations(activity)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${activity.packageName}")
                    }
                    batteryOptimizationLauncher?.launch(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Əgər xüsusi intent işləməsə, ümumi parametrlərə göndər
                    openBatteryOptimizationSettings()
                }
            }
        }
    }
    
    /**
     * Battery optimization parametrlərinə göndər
     */
    fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * İstifadəçiyə izah mesajı
     */
    fun getExplanationMessage(): String {
        return """
            🔋 Arxa Fon İstifadəsi
            
            Azan səslərinin düzgün vaxtda oxunması üçün, tətbiqin arxa fonda işləməsinə icazə verin.
            
            ⚙️ Parametrlərdə:
            • "Batareya optimizasiyası"nı tapın
            • "Şiə Azan"ı seçin
            • "Optimizasiya etmə" seçin
            
            Bu, azanların vaxtında oxunmasını təmin edəcək.
        """.trimIndent()
    }
}
