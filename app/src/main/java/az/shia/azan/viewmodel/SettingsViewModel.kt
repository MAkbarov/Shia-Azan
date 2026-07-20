package az.shia.azan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import az.shia.azan.data.AppSettings
import az.shia.azan.data.AzanSound
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.PrayerType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Parametrlər ViewModel
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferencesManager = PreferencesManager(application)
    
    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )
    
    /**
     * Namaz bildirişini dəyiş
     */
    fun togglePrayerNotification(prayerType: PrayerType, enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPrayerNotification(prayerType, enabled)
        }
    }
    
    /**
     * Azan səsini dəyiş
     */
    fun changeAzanSound(sound: AzanSound) {
        viewModelScope.launch {
            preferencesManager.setAzanSound(sound)
        }
    }
    
    /**
     * Səs səviyyəsini dəyiş
     */
    fun changeVolume(volume: Float) {
        viewModelScope.launch {
            preferencesManager.setAzanVolume(volume)
        }
    }
    
    /**
     * Xatırlatmanı dəyiş
     */
    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReminderEnabled(enabled)
        }
    }
    
    /**
     * Xatırlatma vaxtını dəyiş
     */
    fun changeReminderTime(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.setReminderMinutes(minutes)
        }
    }
    
    /**
     * 24 saat formatını dəyiş
     */
    fun toggle24HourFormat(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.set24HourFormat(enabled)
        }
    }
    
    /**
     * Hicri tarixi dəyiş
     */
    fun toggleHijriDate(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowHijriDate(enabled)
        }
    }
    
    /**
     * Battery optimization statusunu saxla
     */
    fun setBatteryOptimizationDisabled(disabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBatteryOptimizationDisabled(disabled)
        }
    }
    
    /**
     * Parametrləri sıfırla
     */
    fun resetSettings() {
        viewModelScope.launch {
            preferencesManager.resetAllSettings()
        }
    }
}
