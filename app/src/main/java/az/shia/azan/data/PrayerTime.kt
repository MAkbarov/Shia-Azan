package az.shia.azan.data

import java.util.Calendar

/**
 * Namaz vaxtı növləri
 */
enum class PrayerType {
    FAJR,      // Sübh
    SUNRISE,   // Günəş
    DHUHR,     // Zöhr
    ASR,       // Əsr
    MAGHRIB,   // Məğrib
    ISHA       // İşa
}

/**
 * Namaz vaxtı data class
 */
data class PrayerTime(
    val type: PrayerType,
    val time: Calendar,
    val name: String,
    val isNextPrayer: Boolean = false
)

/**
 * Günün bütün namaz vaxtları
 */
data class DailyPrayerTimes(
    val date: Calendar,
    val fajr: Calendar,
    val sunrise: Calendar,
    val dhuhr: Calendar,
    val asr: Calendar,
    val maghrib: Calendar,
    val isha: Calendar,
    val locationName: String = ""
) {
    fun getAllPrayers(): List<PrayerTime> {
        return listOf(
            PrayerTime(PrayerType.FAJR, fajr, "Sübh"),
            PrayerTime(PrayerType.SUNRISE, sunrise, "Günəş"),
            PrayerTime(PrayerType.DHUHR, dhuhr, "Zöhr"),
            PrayerTime(PrayerType.ASR, asr, "Əsr"),
            PrayerTime(PrayerType.MAGHRIB, maghrib, "Məğrib"),
            PrayerTime(PrayerType.ISHA, isha, "İşa")
        )
    }
    
    fun getNextPrayer(currentTime: Calendar = Calendar.getInstance()): PrayerTime? {
        return getAllPrayers()
            .asSequence()
            .filter { it.type != PrayerType.SUNRISE }
            .firstOrNull { it.time.timeInMillis > currentTime.timeInMillis }
    }
}
