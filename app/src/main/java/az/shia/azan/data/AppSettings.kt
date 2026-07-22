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
    val hijriOffsetDays: Int = 0,
    
    // Arxa fon
    val batteryOptimizationDisabled: Boolean = false,
    val automaticUpdatesEnabled: Boolean = true,
    
    // Daimi Bildiriş
    val ongoingNotificationEnabled: Boolean = false,
    
    // Hesablama Metodu
    val calculationMethod: CalculationMethod = CalculationMethod.LEVA_QUM
) {
    /** Seçilmiş namaz üçün bildiriş/azan aktivdirmi. */
    fun isNotificationEnabled(type: PrayerType): Boolean = when (type) {
        PrayerType.FAJR -> fajrNotificationEnabled
        PrayerType.DHUHR -> dhuhrNotificationEnabled
        PrayerType.ASR -> asrNotificationEnabled
        PrayerType.MAGHRIB -> maghribNotificationEnabled
        PrayerType.ISHA -> ishaNotificationEnabled
        PrayerType.SUNRISE -> false
    }
}

/**
 * Hesablama metodları
 */
enum class CalculationMethod(
    val displayName: String,
    val fajrAngle: Double,
    val maghribAngle: Double,
    val ishaAngle: Double
) {
    LEVA_QUM("Qum (Leva İnstitutu)", 16.0, 4.0, 14.0),
    TEHRAN("Tehran (Geofizika İnstitutu)", 17.7, 4.5, 14.0)
}

/**
 * Azan səsi növləri
 */
enum class AzanSound(
    val displayName: String,
    val fileName: String,
    val fajrFileName: String? = null
) {
    DEFAULT("Standart Şiə Azanı", "azan_default", "azan_fajr"),
    ALI_FANI("Azan - Ali Fani", "azan_ali_fani"),
    RAHIM_MUEZZINZADE("Azan - Rəhim Müəzzinzadə", "azan_rahim_muezzinzade"),
    TEYMUR_SHIRVANLI("Azan - Teymur Şirvanlı", "azan_teymur_shirvanli");

    fun getResourceName(isFajr: Boolean): String {
        return if (isFajr) fajrFileName ?: fileName else fileName
    }
}
