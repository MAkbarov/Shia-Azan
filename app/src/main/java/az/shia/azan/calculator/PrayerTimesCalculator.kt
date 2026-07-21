package az.shia.azan.calculator

import az.shia.azan.data.CalculationMethod
import az.shia.azan.data.DailyPrayerTimes
import az.shia.azan.data.LocationData
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/**
 * Şiə məzhəbi üçün astronomik namaz vaxtı kalkulyatoru.
 *
 * Günəş mövqeyi U.S. Naval Observatory yaxınlaşdırmasına, namaz bucaqları isə
 * PrayTimes Jafari (Leva, Qum) və Tehran metodlarına əsaslanır. Vaxtlar sabit
 * cədvəldən götürülmür; tarix, koordinat və IANA timezone əsasında hesablanır.
 */
class PrayerTimesCalculator {

    companion object {
        private const val ASR_SHADOW_FACTOR = 1.0
        private const val SUNRISE_SUNSET_ANGLE = 0.833
        private const val JULIAN_2000 = 2451545.0
        private const val ITERATION_COUNT = 2
        private const val MINUTES_PER_DAY = 24 * 60
    }

    private data class SolarPosition(
        val declination: Double,
        val equationOfTime: Double
    )

    private data class DecimalTimes(
        val fajr: Double,
        val sunrise: Double,
        val dhuhr: Double,
        val asr: Double,
        val maghrib: Double,
        val isha: Double
    )

    fun calculatePrayerTimes(
        date: Calendar = Calendar.getInstance(),
        location: LocationData,
        method: CalculationMethod = CalculationMethod.LEVA_QUM
    ): DailyPrayerTimes {
        val localDate = (date.clone() as Calendar).apply {
            timeZone = resolveTimeZone(location.timeZone)
        }
        val calculationDate = (localDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // PrayTimes yanaşması: yerli uzunluğu Julian gününə də tətbiq edərək
        // iterasiya zamanı Günəş mövqeyini hadisənin təxmini anında hesabla.
        val julianDate = calculateJulianDate(calculationDate) -
            location.longitude / (15.0 * 24.0)

        var times = DecimalTimes(
            fajr = 5.0,
            sunrise = 6.0,
            dhuhr = 12.0,
            asr = 13.0,
            maghrib = 18.0,
            isha = 18.0
        )

        repeat(ITERATION_COUNT) {
            times = computeTimes(
                estimates = times,
                julianDate = julianDate,
                latitude = location.latitude,
                method = method
            )
        }

        val clockAdjustment = getTimeZoneOffset(calculationDate) - location.longitude / 15.0
        val adjusted = DecimalTimes(
            fajr = times.fajr + clockAdjustment,
            sunrise = times.sunrise + clockAdjustment,
            dhuhr = times.dhuhr + clockAdjustment,
            asr = times.asr + clockAdjustment,
            maghrib = times.maghrib + clockAdjustment,
            isha = times.isha + clockAdjustment
        )

        return DailyPrayerTimes(
            date = localDate,
            fajr = createCalendar(localDate, adjusted.fajr),
            sunrise = createCalendar(localDate, adjusted.sunrise),
            dhuhr = createCalendar(localDate, adjusted.dhuhr),
            asr = createCalendar(localDate, adjusted.asr),
            maghrib = createCalendar(localDate, adjusted.maghrib),
            isha = createCalendar(localDate, adjusted.isha),
            locationName = location.cityName
        )
    }

    private fun computeTimes(
        estimates: DecimalTimes,
        julianDate: Double,
        latitude: Double,
        method: CalculationMethod
    ): DecimalTimes {
        return DecimalTimes(
            fajr = timeAtSolarAltitude(
                julianDate,
                latitude,
                -method.fajrAngle,
                estimates.fajr / 24.0,
                beforeNoon = true
            ),
            sunrise = timeAtSolarAltitude(
                julianDate,
                latitude,
                -SUNRISE_SUNSET_ANGLE,
                estimates.sunrise / 24.0,
                beforeNoon = true
            ),
            dhuhr = midday(julianDate, estimates.dhuhr / 24.0),
            asr = calculateAsr(
                julianDate,
                latitude,
                estimates.asr / 24.0,
                ASR_SHADOW_FACTOR
            ),
            maghrib = timeAtSolarAltitude(
                julianDate,
                latitude,
                -method.maghribAngle,
                estimates.maghrib / 24.0,
                beforeNoon = false
            ),
            isha = timeAtSolarAltitude(
                julianDate,
                latitude,
                -method.ishaAngle,
                estimates.isha / 24.0,
                beforeNoon = false
            )
        )
    }

    private fun timeAtSolarAltitude(
        julianDate: Double,
        latitude: Double,
        altitude: Double,
        dayPortion: Double,
        beforeNoon: Boolean
    ): Double {
        val position = sunPosition(julianDate + dayPortion)
        val numerator = sinDegrees(altitude) -
            sinDegrees(latitude) * sinDegrees(position.declination)
        val denominator = cosDegrees(latitude) * cosDegrees(position.declination)
        val cosineHourAngle = numerator / denominator

        // Normal enliklərdə dəyər həmişə [-1, 1]-dədir. Kiçik floating-point
        // sapmalarını sıxmaq NaN və yanlış Calendar yaranmasının qarşısını alır.
        val hourAngle = Math.toDegrees(acos(cosineHourAngle.coerceIn(-1.0, 1.0))) / 15.0
        val noon = midday(julianDate, dayPortion)
        return if (beforeNoon) noon - hourAngle else noon + hourAngle
    }

    private fun calculateAsr(
        julianDate: Double,
        latitude: Double,
        dayPortion: Double,
        shadowFactor: Double
    ): Double {
        val declination = sunPosition(julianDate + dayPortion).declination
        val altitude = Math.toDegrees(
            atan(1.0 / (shadowFactor + tanDegrees(abs(latitude - declination))))
        )
        return timeAtSolarAltitude(
            julianDate,
            latitude,
            altitude,
            dayPortion,
            beforeNoon = false
        )
    }

    private fun midday(julianDate: Double, dayPortion: Double): Double {
        val equationOfTime = sunPosition(julianDate + dayPortion).equationOfTime
        return fixHour(12.0 - equationOfTime)
    }

    /**
     * U.S. Naval Observatory yaxınlaşdırması. EqT işarəli saat dəyəridir və
     * son clock vaxtı hesablanana qədər 0..24 aralığına bükülmür.
     */
    private fun sunPosition(julianDate: Double): SolarPosition {
        val daysFromEpoch = julianDate - JULIAN_2000
        val meanAnomaly = fixAngle(357.529 + 0.98560028 * daysFromEpoch)
        val meanLongitude = fixAngle(280.459 + 0.98564736 * daysFromEpoch)
        val eclipticLongitude = fixAngle(
            meanLongitude +
                1.915 * sinDegrees(meanAnomaly) +
                0.020 * sinDegrees(2.0 * meanAnomaly)
        )
        val obliquity = 23.439 - 0.00000036 * daysFromEpoch
        val rightAscension = fixHour(
            Math.toDegrees(
                atan2(
                    cosDegrees(obliquity) * sinDegrees(eclipticLongitude),
                    cosDegrees(eclipticLongitude)
                )
            ) / 15.0
        )
        val equationOfTime = signedHour(meanLongitude / 15.0 - rightAscension)
        val declination = Math.toDegrees(
            asin(sinDegrees(obliquity) * sinDegrees(eclipticLongitude))
        )

        return SolarPosition(declination, equationOfTime)
    }

    /** Julian gününü UTC gecəyarısı semantikası ilə (.5) hesablayır. */
    private fun calculateJulianDate(date: Calendar): Double {
        var year = date.get(Calendar.YEAR)
        var month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)

        if (month <= 2) {
            year -= 1
            month += 12
        }

        val century = floor(year / 100.0)
        val correction = 2.0 - century + floor(century / 4.0)
        return floor(365.25 * (year + 4716)) +
            floor(30.6001 * (month + 1)) +
            day + correction - 1524.5
    }

    private fun resolveTimeZone(timeZoneId: String): TimeZone {
        val timeZone = TimeZone.getTimeZone(timeZoneId)
        return if (timeZone.id == "GMT" && timeZoneId != "GMT") {
            TimeZone.getDefault()
        } else {
            timeZone
        }
    }

    private fun getTimeZoneOffset(date: Calendar): Double {
        return date.timeZone.getOffset(date.timeInMillis) / (1000.0 * 60.0 * 60.0)
    }

    private fun createCalendar(baseDate: Calendar, decimalHour: Double): Calendar {
        val roundedMinutes = (decimalHour * 60.0).roundToInt()
        val dayOffset = Math.floorDiv(roundedMinutes, MINUTES_PER_DAY)
        val minuteOfDay = Math.floorMod(roundedMinutes, MINUTES_PER_DAY)

        return (baseDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
            set(Calendar.MINUTE, minuteOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, dayOffset)
        }
    }

    private fun fixAngle(angle: Double): Double = ((angle % 360.0) + 360.0) % 360.0

    private fun fixHour(hour: Double): Double = ((hour % 24.0) + 24.0) % 24.0

    private fun signedHour(hour: Double): Double {
        val normalized = fixHour(hour)
        return if (normalized > 12.0) normalized - 24.0 else normalized
    }

    private fun sinDegrees(value: Double): Double = sin(Math.toRadians(value))

    private fun cosDegrees(value: Double): Double = cos(Math.toRadians(value))

    private fun tanDegrees(value: Double): Double = tan(Math.toRadians(value))
}
