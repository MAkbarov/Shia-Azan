package az.shia.azan.notification

import android.content.Context
import az.shia.azan.calculator.PrayerTimesCalculator
import az.shia.azan.data.PrayerType
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.ShiaCities
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.TimeZone

/**
 * Namaz (azan) alarmlarını saxlanılan məkan və hesablama metoduna görə yenidən qurur.
 *
 * Android reboot zamanı bütün alarmları silir; bu planlayıcı boot-da və hər namaz
 * alarmı işlədikdən sonra çağırılaraq azanın heç vaxt kəsilməməsini və gün
 * keçidində (bugünkü sonuncu namazdan sonra sabaha) davam etməsini təmin edir.
 * Per-namaz aktiv/deaktiv seçimi alarm işləyəndə AzanReceiver-də yoxlanılır.
 */
object PrayerAlarmPlanner {

    suspend fun reschedule(context: Context) {
        val appContext = context.applicationContext
        val preferences = PreferencesManager(appContext)
        val settings = preferences.settingsFlow.first()
        val location = preferences.getLastLocation().first() ?: ShiaCities.getDefaultCity()

        val calculator = PrayerTimesCalculator()
        val scheduler = AlarmScheduler(appContext)

        val now = Calendar.getInstance(TimeZone.getTimeZone(location.timeZone))
        val today = calculator.calculatePrayerTimes(now, location, settings.calculationMethod)

        val hasFutureToday = today.getAllPrayers().any {
            it.type != PrayerType.SUNRISE && it.time.timeInMillis > now.timeInMillis
        }

        if (hasFutureToday) {
            scheduler.schedulePrayerAlarms(today)
        } else {
            // Bugünkü namazlar bitib — sabahkı vaxtları planla (rollover).
            val tomorrow = (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            val tomorrowTimes = calculator.calculatePrayerTimes(
                date = tomorrow,
                location = location,
                method = settings.calculationMethod
            )
            scheduler.schedulePrayerAlarms(tomorrowTimes)
        }
    }
}
