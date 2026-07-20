package az.shia.azan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.shia.azan.calculator.PrayerTimesCalculator
import az.shia.azan.data.ShiaCities
import az.shia.azan.data.DailyPrayerTimes
import az.shia.azan.data.LocationData
import az.shia.azan.data.PrayerTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Namaz vaxtları üçün ViewModel
 */
class PrayerTimesViewModel : ViewModel() {
    
    private val calculator = PrayerTimesCalculator()
    
    private val _prayerTimes = MutableStateFlow<DailyPrayerTimes?>(null)
    val prayerTimes: StateFlow<DailyPrayerTimes?> = _prayerTimes.asStateFlow()
    
    private val _selectedLocation = MutableStateFlow(ShiaCities.getDefaultCity())
    val selectedLocation: StateFlow<LocationData> = _selectedLocation.asStateFlow()
    
    private val _nextPrayer = MutableStateFlow<PrayerTime?>(null)
    val nextPrayer: StateFlow<PrayerTime?> = _nextPrayer.asStateFlow()
    
    private val _currentTime = MutableStateFlow(Calendar.getInstance())
    val currentTime: StateFlow<Calendar> = _currentTime.asStateFlow()
    
    init {
        loadPrayerTimes()
    }
    
    /**
     * Namaz vaxtlarını yüklə
     */
    fun loadPrayerTimes() {
        viewModelScope.launch {
            try {
                val times = calculator.calculatePrayerTimes(
                    date = Calendar.getInstance(),
                    location = _selectedLocation.value
                )
                _prayerTimes.value = times
                updateNextPrayer()
                
                // Callback çağır (alarm qurma üçün)
                onPrayerTimesLoaded?.invoke(times)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Şəhəri dəyiş
     */
    fun selectLocation(location: LocationData) {
        _selectedLocation.value = location
        loadPrayerTimes()
    }
    
    /**
     * AlarmScheduler üçün callback
     */
    var onPrayerTimesLoaded: ((DailyPrayerTimes) -> Unit)? = null
    
    /**
     * Növbəti namazı yenilə
     */
    fun updateNextPrayer() {
        _currentTime.value = Calendar.getInstance()
        _nextPrayer.value = _prayerTimes.value?.getNextPrayer(_currentTime.value)
    }
    
    /**
     * Hazırkı vaxtı yenilə
     */
    fun updateCurrentTime() {
        _currentTime.value = Calendar.getInstance()
        updateNextPrayer()
    }
}
