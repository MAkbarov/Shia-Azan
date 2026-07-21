package az.shia.azan.utils

import android.icu.util.IslamicCalendar
import android.icu.util.TimeZone
import java.util.Calendar
import java.util.Locale

/**
 * Hicri-qəməri tarixi deterministik civil baza ilə hesablayır.
 * Ayın görünməsi bölgə və mərcə elanına görə fərqlənə bildiyi üçün istifadəçi
 * offset-i ayrıca tətbiq olunur.
 */
object HijriDateFormatter {

    const val MIN_OFFSET_DAYS = -7
    const val MAX_OFFSET_DAYS = 7

    private val monthNames = listOf(
        "Məhərrəm",
        "Səfər",
        "Rəbiüləvvəl",
        "Rəbiülaxır",
        "Cəmadiyələvvəl",
        "Cəmadiyəlaxır",
        "Rəcəb",
        "Şaban",
        "Ramazan",
        "Şəvval",
        "Zilqədə",
        "Zilhiccə"
    )

    data class HijriDate(
        val day: Int,
        val monthName: String,
        val year: Int
    )

    fun calculate(calendar: Calendar, offsetDays: Int = 0): HijriDate {
        val adjusted = (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, offsetDays.coerceIn(MIN_OFFSET_DAYS, MAX_OFFSET_DAYS))
        }
        val islamicCalendar = IslamicCalendar(
            TimeZone.getTimeZone(adjusted.timeZone.id),
            Locale("az")
        ).apply {
            calculationType = IslamicCalendar.CalculationType.ISLAMIC_CIVIL
            timeInMillis = adjusted.timeInMillis
        }
        val monthIndex = islamicCalendar.get(IslamicCalendar.MONTH)
            .coerceIn(monthNames.indices)
        return HijriDate(
            day = islamicCalendar.get(IslamicCalendar.DAY_OF_MONTH),
            monthName = monthNames[monthIndex],
            year = islamicCalendar.get(IslamicCalendar.YEAR)
        )
    }

    fun format(calendar: Calendar, offsetDays: Int = 0): String {
        val date = calculate(calendar, offsetDays)
        return "${date.day} ${date.monthName} ${date.year}"
    }
}
