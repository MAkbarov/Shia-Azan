package az.shia.azan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import az.shia.azan.calculator.PrayerTimesCalculator
import az.shia.azan.data.CalculationMethod
import az.shia.azan.data.DailyPrayerTimes
import az.shia.azan.data.LocationData
import az.shia.azan.data.PrayerTime
import az.shia.azan.data.PrayerType
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.ShiaCities
import az.shia.azan.utils.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/** Namaz vaxtları və cari növbəti namaz üçün ViewModel. */
class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

    private val calculator = PrayerTimesCalculator()
    private val preferencesManager = PreferencesManager(application)
    private var calculationJob: Job? = null
    private var activeMethod = CalculationMethod.LEVA_QUM

    private val _prayerTimes = MutableStateFlow<DailyPrayerTimes?>(null)
    val prayerTimes: StateFlow<DailyPrayerTimes?> = _prayerTimes.asStateFlow()

    private val _selectedLocation = MutableStateFlow(ShiaCities.getDefaultCity())
    val selectedLocation: StateFlow<LocationData> = _selectedLocation.asStateFlow()

    private val _nextPrayer = MutableStateFlow<PrayerTime?>(null)
    val nextPrayer: StateFlow<PrayerTime?> = _nextPrayer.asStateFlow()

    private val _currentTime = MutableStateFlow(Calendar.getInstance())
    val currentTime: StateFlow<Calendar> = _currentTime.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.getLastLocation().firstOrNull()?.let {
                _selectedLocation.value = it
            }
            launch {
                preferencesManager.settingsFlow.collectLatest { settings ->
                    loadPrayerTimes(settings.calculationMethod)
                }
            }
        }
    }

    fun loadPrayerTimes(method: CalculationMethod? = null) {
        calculationJob?.cancel()
        calculationJob = viewModelScope.launch {
            try {
                activeMethod = method
                    ?: preferencesManager.settingsFlow.first().calculationMethod
                val locationSnapshot = _selectedLocation.value
                val now = currentLocationTime(locationSnapshot)
                val times = calculator.calculatePrayerTimes(
                    date = now,
                    location = locationSnapshot,
                    method = activeMethod
                )
                _prayerTimes.value = times
                updateNextPrayer(now)

                // Tətbiq İşadan sonra açılıbsa alarm planı bugünün keçmiş
                // vaxtlarında qalmasın; sabahın real hesabını planlaşdır.
                val alarmDay = if (times.isha.timeInMillis <= now.timeInMillis) {
                    calculateTomorrowTimes(now, locationSnapshot, activeMethod)
                } else {
                    times
                }
                onPrayerTimesLoaded?.invoke(alarmDay)
            } catch (exception: kotlinx.coroutines.CancellationException) {
                throw exception
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun selectLocation(location: LocationData) {
        _selectedLocation.value = location
        viewModelScope.launch { preferencesManager.setLastLocation(location) }
        loadPrayerTimes()
    }

    var onPrayerTimesLoaded: ((DailyPrayerTimes) -> Unit)? = null

    /**
     * Cari gün bitibsə real sabah tarixini kalkulyatora verib Sübhü qaytarır.
     * Beləliklə İşadan sonra nextPrayer null və ya bugünkü keçmiş Sübh olmur.
     */
    fun updateNextPrayer(now: Calendar = currentLocationTime()) {
        _currentTime.value = now
        val todayTimes = _prayerTimes.value
        _nextPrayer.value = todayTimes?.getNextPrayer(now) ?: run {
            val tomorrowTimes = calculateTomorrowTimes(
                now = now,
                location = _selectedLocation.value,
                method = activeMethod
            )
            tomorrowTimes.getAllPrayers().first { it.type == PrayerType.FAJR }
        }
    }

    /** Vaxtı yenilə; yerli tarix dəyişibsə bütün günlük hesabı təzələ. */
    fun updateCurrentTime() {
        val now = currentLocationTime()
        _currentTime.value = now
        val loadedDay = _prayerTimes.value?.date
        if (loadedDay == null || !TimeFormatter.isSameDay(loadedDay, now)) {
            loadPrayerTimes(activeMethod)
        } else {
            updateNextPrayer(now)
        }
    }

    private fun calculateTomorrowTimes(
        now: Calendar,
        location: LocationData,
        method: CalculationMethod
    ): DailyPrayerTimes {
        val tomorrow = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        return calculator.calculatePrayerTimes(tomorrow, location, method)
    }

    private fun currentLocationTime(
        location: LocationData = _selectedLocation.value
    ): Calendar = Calendar.getInstance(TimeZone.getTimeZone(location.timeZone))
}
