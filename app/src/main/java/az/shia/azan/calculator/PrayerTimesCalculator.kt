package az.shia.azan.calculator

import az.shia.azan.data.DailyPrayerTimes
import az.shia.azan.data.LocationData
import java.util.Calendar
import kotlin.math.*

/**
 * Şiə məzhəbi üçün namaz vaxtlarını hesablayan klass
 * İslami astronomiya hesablamaları əsasında
 */
class PrayerTimesCalculator {
    
    companion object {
        // Şiə hesablama metodları
        private const val FAJR_ANGLE = 16.0        // Sübh üçün günəş bucağı (Şiə: 16°)
        private const val ISHA_ANGLE = 14.0        // İşa üçün günəş bucağı (Şiə: 14°)
        private const val ASR_SHADOW_FACTOR = 1.0  // Əsr üçün kölgə faktoru (Şafi/Şiə: 1)
    }
    
    /**
     * Verilmiş tarix və yer üçün namaz vaxtlarını hesabla
     */
    fun calculatePrayerTimes(
        date: Calendar = Calendar.getInstance(),
        location: LocationData
    ): DailyPrayerTimes {
        val latitude = location.latitude
        val longitude = location.longitude
        val timeZone = getTimeZoneOffset(date)
        
        // Julian tarixi hesabla
        val julianDate = calculateJulianDate(date)
        
        // Günəş tənliyi və deklinasiya
        val equation = calculateEquationOfTime(julianDate)
        val declination = calculateSunDeclination(julianDate)
        
        // Zöhr vaxtı (günorta)
        val dhuhrTime = calculateDhuhr(longitude, timeZone, equation)
        
        // Digər namaz vaxtları
        val fajrTime = calculateTimeByAngle(
            latitude, declination, FAJR_ANGLE, 
            dhuhrTime, timeZone, false
        )
        
        val sunriseTime = calculateTimeByAngle(
            latitude, declination, 0.833, 
            dhuhrTime, timeZone, false
        )
        
        val asrTime = calculateAsr(
            latitude, declination, dhuhrTime, 
            timeZone, ASR_SHADOW_FACTOR
        )
        
        val maghribTime = calculateTimeByAngle(
            latitude, declination, 0.833, 
            dhuhrTime, timeZone, true
        )
        
        val ishaTime = calculateTimeByAngle(
            latitude, declination, ISHA_ANGLE, 
            dhuhrTime, timeZone, true
        )
        
        return DailyPrayerTimes(
            date = date,
            fajr = createCalendar(date, fajrTime),
            sunrise = createCalendar(date, sunriseTime),
            dhuhr = createCalendar(date, dhuhrTime),
            asr = createCalendar(date, asrTime),
            maghrib = createCalendar(date, maghribTime),
            isha = createCalendar(date, ishaTime),
            locationName = location.cityName
        )
    }
    
    /**
     * Julian tarixini hesabla
     */
    private fun calculateJulianDate(date: Calendar): Double {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045.0
    }
    
    /**
     * Günəş tənliyini hesabla
     */
    private fun calculateEquationOfTime(jd: Double): Double {
        val d = jd - 2451545.0
        val g = 357.529 + 0.98560028 * d
        val q = 280.459 + 0.98564736 * d
        val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))
        
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))
        
        val eqt = q - fixAngle(ra)
        return fixHour(eqt / 15.0)
    }
    
    /**
     * Günəş deklinasiyasını hesabla
     */
    private fun calculateSunDeclination(jd: Double): Double {
        val d = jd - 2451545.0
        val g = 357.529 + 0.98560028 * d
        val q = 280.459 + 0.98564736 * d
        val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))
        
        val e = 23.439 - 0.00000036 * d
        return Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
    }
    
    /**
     * Zöhr vaxtını hesabla (günorta)
     */
    private fun calculateDhuhr(longitude: Double, timeZone: Double, equation: Double): Double {
        return fixHour(12 - longitude / 15.0 + timeZone + equation)
    }
    
    /**
     * Bucağa görə vaxtı hesabla (Sübh, Günəş, Məğrib, İşa)
     */
    private fun calculateTimeByAngle(
        latitude: Double,
        declination: Double,
        angle: Double,
        midday: Double,
        timeZone: Double,
        isEvening: Boolean
    ): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val angleRad = Math.toRadians(angle)
        
        val cosH = (-sin(angleRad) - sin(latRad) * sin(decRad)) / 
                   (cos(latRad) * cos(decRad))
        
        if (cosH > 1 || cosH < -1) {
            return if (isEvening) midday + 3.0 else midday - 3.0
        }
        
        val hourAngle = Math.toDegrees(acos(cosH)) / 15.0
        
        return if (isEvening) {
            midday + hourAngle
        } else {
            midday - hourAngle
        }
    }
    
    /**
     * Əsr vaxtını hesabla
     */
    private fun calculateAsr(
        latitude: Double,
        declination: Double,
        midday: Double,
        timeZone: Double,
        shadowFactor: Double
    ): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        
        val angle = -atan(
            1.0 / (shadowFactor + tan(abs(latRad - decRad)))
        )
        
        val cosH = (sin(angle) - sin(latRad) * sin(decRad)) / 
                   (cos(latRad) * cos(decRad))
        
        if (cosH > 1 || cosH < -1) {
            return midday + 3.0
        }
        
        val hourAngle = Math.toDegrees(acos(cosH)) / 15.0
        return midday + hourAngle
    }
    
    /**
     * Vaxt zonası offsetini al
     */
    private fun getTimeZoneOffset(date: Calendar): Double {
        return date.timeZone.getOffset(date.timeInMillis) / (1000.0 * 60.0 * 60.0)
    }
    
    /**
     * Bucağı 0-360 arasına gətir
     */
    private fun fixAngle(angle: Double): Double {
        var a = angle
        while (a < 0) a += 360.0
        while (a >= 360) a -= 360.0
        return a
    }
    
    /**
     * Saatı 0-24 arasına gətir
     */
    private fun fixHour(hour: Double): Double {
        var h = hour
        while (h < 0) h += 24.0
        while (h >= 24) h -= 24.0
        return h
    }
    
    /**
     * Onluq saatı Calendar obyektinə çevir
     */
    private fun createCalendar(baseDate: Calendar, decimalHour: Double): Calendar {
        val cal = baseDate.clone() as Calendar
        val hour = decimalHour.toInt()
        val minute = ((decimalHour - hour) * 60).toInt()
        
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        return cal
    }
}
