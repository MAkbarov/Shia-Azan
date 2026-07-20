package az.shia.azan.data

/**
 * Tətbiq parametrləri data class
 */
data class AppSettings(
    // Namaz bildirişləri
    val fajrNotificationEnabled: Boolean = true,
    val dhuhrNotificationEnabled: Boolean = true,
    val asrNotificationEnabled: Boolean = true,
    val maghribNotificationEnabled: Boolean = true,
    val ishaNotificationEnabled: Boolean = true,
    
    // Azan səsləri
    val selectedAzanSound: AzanSound = AzanSound.DEFAULT,
    val azanVolume: Float = 1.0f,
    
    // Xatırlatma
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 10,
    
    // Görünüş
    val use24HourFormat: Boolean = true,
    val showHijriDate: Boolean = true,
    
    // Arxa fon
    val batteryOptimizationDisabled: Boolean = false
)

/**
 * Azan səsi növləri
 */
enum class AzanSound(val displayName: String, val fileName: String) {
    DEFAULT("Standart Şiə Azan", "azan_default"),
    MAKKAH("Məkkə Azanı", "azan_makkah"),
    MADINAH("Mədinə Azanı", "azan_madinah"),
    SHIA_1("Şiə Azan 1", "azan_shia_1"),
    SHIA_2("Şiə Azan 2", "azan_shia_2"),
    SHIA_3("Şiə Azan 3", "azan_shia_3");
    
    fun getResourceName(isFajr: Boolean): String {
        return if (isFajr && this == DEFAULT) {
            "azan_fajr"
        } else {
            fileName
        }
    }
}
