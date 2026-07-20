package az.shia.azan.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore ilə parametrlərin saxlanması
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shia_azan_settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        // Namaz bildirişləri
        val FAJR_NOTIFICATION = booleanPreferencesKey("fajr_notification")
        val DHUHR_NOTIFICATION = booleanPreferencesKey("dhuhr_notification")
        val ASR_NOTIFICATION = booleanPreferencesKey("asr_notification")
        val MAGHRIB_NOTIFICATION = booleanPreferencesKey("maghrib_notification")
        val ISHA_NOTIFICATION = booleanPreferencesKey("isha_notification")
        
        // Azan səsləri
        val AZAN_SOUND = stringPreferencesKey("azan_sound")
        val AZAN_VOLUME = floatPreferencesKey("azan_volume")
        
        // Xatırlatma
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
        
        // Görünüş
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_HIJRI = booleanPreferencesKey("show_hijri")
        
        // Arxa fon
        val BATTERY_OPT_DISABLED = booleanPreferencesKey("battery_opt_disabled")
    }
    
    /**
     * Parametrləri oxu
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            fajrNotificationEnabled = preferences[FAJR_NOTIFICATION] ?: true,
            dhuhrNotificationEnabled = preferences[DHUHR_NOTIFICATION] ?: true,
            asrNotificationEnabled = preferences[ASR_NOTIFICATION] ?: true,
            maghribNotificationEnabled = preferences[MAGHRIB_NOTIFICATION] ?: true,
            ishaNotificationEnabled = preferences[ISHA_NOTIFICATION] ?: true,
            
            selectedAzanSound = try {
                AzanSound.valueOf(preferences[AZAN_SOUND] ?: "DEFAULT")
            } catch (e: Exception) {
                AzanSound.DEFAULT
            },
            azanVolume = preferences[AZAN_VOLUME] ?: 1.0f,
            
            reminderEnabled = preferences[REMINDER_ENABLED] ?: false,
            reminderMinutesBefore = preferences[REMINDER_MINUTES] ?: 10,
            
            use24HourFormat = preferences[USE_24_HOUR] ?: true,
            showHijriDate = preferences[SHOW_HIJRI] ?: true,
            
            batteryOptimizationDisabled = preferences[BATTERY_OPT_DISABLED] ?: false
        )
    }
    
    /**
     * Namaz bildirişini dəyiş
     */
    suspend fun setPrayerNotification(prayerType: PrayerType, enabled: Boolean) {
        val key = when (prayerType) {
            PrayerType.FAJR -> FAJR_NOTIFICATION
            PrayerType.DHUHR -> DHUHR_NOTIFICATION
            PrayerType.ASR -> ASR_NOTIFICATION
            PrayerType.MAGHRIB -> MAGHRIB_NOTIFICATION
            PrayerType.ISHA -> ISHA_NOTIFICATION
            else -> return
        }
        
        context.dataStore.edit { preferences ->
            preferences[key] = enabled
        }
    }
    
    /**
     * Azan səsini dəyiş
     */
    suspend fun setAzanSound(sound: AzanSound) {
        context.dataStore.edit { preferences ->
            preferences[AZAN_SOUND] = sound.name
        }
    }
    
    /**
     * Azan səsinin səviyyəsini dəyiş
     */
    suspend fun setAzanVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[AZAN_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }
    
    /**
     * Xatırlatmanı aktivləşdir/söndür
     */
    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_ENABLED] = enabled
        }
    }
    
    /**
     * Xatırlatma vaxtını dəyiş
     */
    suspend fun setReminderMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[REMINDER_MINUTES] = minutes.coerceIn(5, 60)
        }
    }
    
    /**
     * 24 saat formatını dəyiş
     */
    suspend fun set24HourFormat(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_24_HOUR] = enabled
        }
    }
    
    /**
     * Hicri tarix göstərişini dəyiş
     */
    suspend fun setShowHijriDate(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_HIJRI] = enabled
        }
    }
    
    /**
     * Battery optimization statusunu saxla
     */
    suspend fun setBatteryOptimizationDisabled(disabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BATTERY_OPT_DISABLED] = disabled
        }
    }
    
    /**
     * Bütün parametrləri sıfırla
     */
    suspend fun resetAllSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
