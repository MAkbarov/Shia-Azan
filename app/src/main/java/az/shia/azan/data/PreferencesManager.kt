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
        
        // Daimi Bildiriş
        val ONGOING_NOTIFICATION = booleanPreferencesKey("ongoing_notification")
        
        // Hesablama Metodu
        val CALC_METHOD = stringPreferencesKey("calculation_method")
        
        // Yer məlumatı
        val LAST_CITY = stringPreferencesKey("last_city")
        val LAST_LAT = floatPreferencesKey("last_lat")
        val LAST_LNG = floatPreferencesKey("last_lng")
        val LAST_COUNTRY = stringPreferencesKey("last_country")
        val LAST_TIME_ZONE = stringPreferencesKey("last_time_zone")
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
            
            batteryOptimizationDisabled = preferences[BATTERY_OPT_DISABLED] ?: false,
            ongoingNotificationEnabled = preferences[ONGOING_NOTIFICATION] ?: false,
            calculationMethod = try {
                CalculationMethod.valueOf(preferences[CALC_METHOD] ?: "LEVA_QUM")
            } catch (e: Exception) {
                CalculationMethod.LEVA_QUM
            }
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
     * Daimi bildirişi aktivləşdir/söndür
     */
    suspend fun setOngoingNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONGOING_NOTIFICATION] = enabled
        }
    }
    
    /**
     * Son seçilmiş şəhəri onun timezone-u ilə birlikdə saxla.
     */
    suspend fun setLastLocation(location: LocationData) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CITY] = location.cityName
            preferences[LAST_LAT] = location.latitude.toFloat()
            preferences[LAST_LNG] = location.longitude.toFloat()
            preferences[LAST_COUNTRY] = location.countryName
            preferences[LAST_TIME_ZONE] = location.timeZone
        }
    }
    
    /**
     * Son seçilmiş şəhəri al. Köhnə saxlanmış qeydlərdə timezone olmadığı halda,
     * tanınan Şiə şəhərlərinin siyahısından düzgün məlumatı bərpa et.
     */
    fun getLastLocation(): Flow<LocationData?> = context.dataStore.data.map { preferences ->
        val city = preferences[LAST_CITY] ?: return@map null
        val lat = preferences[LAST_LAT] ?: 0f
        val lng = preferences[LAST_LNG] ?: 0f
        val savedCountry = preferences[LAST_COUNTRY]
        val knownCity = ShiaCities.allCities.firstOrNull {
            it.cityName == city && (savedCountry.isNullOrBlank() || it.countryName == savedCountry)
        }

        LocationData(
            latitude = knownCity?.latitude ?: lat.toDouble(),
            longitude = knownCity?.longitude ?: lng.toDouble(),
            cityName = city,
            countryName = savedCountry ?: knownCity?.countryName.orEmpty(),
            timeZone = preferences[LAST_TIME_ZONE] ?: knownCity?.timeZone ?: "Asia/Baku"
        )
    }
    
    /**
     * Hesablama metodunu dəyiş
     */
    suspend fun setCalculationMethod(method: CalculationMethod) {
        context.dataStore.edit { preferences ->
            preferences[CALC_METHOD] = method.name
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
