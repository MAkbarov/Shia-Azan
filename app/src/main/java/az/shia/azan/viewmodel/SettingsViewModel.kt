package az.shia.azan.viewmodel

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import az.shia.azan.BuildConfig
import az.shia.azan.data.AppSettings
import az.shia.azan.data.AzanSound
import az.shia.azan.data.CalculationMethod
import az.shia.azan.data.PreferencesManager
import az.shia.azan.data.PrayerType
import az.shia.azan.update.UpdateCheckResult
import az.shia.azan.update.UpdateDownloader
import az.shia.azan.update.UpdateInfo
import az.shia.azan.update.UpdateRepository
import az.shia.azan.update.UpdateScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UpdateUiState(
    val isChecking: Boolean = false,
    val latestVersion: String? = null,
    val updateAvailable: Boolean = false,
    val message: String = "Yeniləməni yoxlamaq üçün toxunun",
    val updateInfo: UpdateInfo? = null
)

/** Parametrlər və tətbiq yeniləməsi state-i. */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val updateRepository = UpdateRepository()

    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    private val _updateState = MutableStateFlow(UpdateUiState())
    val updateState: StateFlow<UpdateUiState> = _updateState.asStateFlow()

    // Yenilənmədən sonra bir dəfəlik "tətbiq yeniləndi" toast-ı üçün versiya adı.
    private val _justUpdatedVersion = MutableStateFlow<String?>(null)
    val justUpdatedVersion: StateFlow<String?> = _justUpdatedVersion.asStateFlow()

    init {
        viewModelScope.launch {
            val storedVersionCode = preferencesManager.getLastSeenVersionCode().first()
            val currentVersionCode = BuildConfig.VERSION_CODE
            if (storedVersionCode != null && storedVersionCode < currentVersionCode) {
                _justUpdatedVersion.value = BuildConfig.VERSION_NAME
            }
            if (storedVersionCode != currentVersionCode) {
                preferencesManager.setLastSeenVersionCode(currentVersionCode)
            }
        }
    }

    fun clearUpdateToast() {
        _justUpdatedVersion.value = null
    }

    fun togglePrayerNotification(prayerType: PrayerType, enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setPrayerNotification(prayerType, enabled) }
    }

    fun changeAzanSound(sound: AzanSound) {
        viewModelScope.launch { preferencesManager.setAzanSound(sound) }
    }

    fun changeVolume(volume: Float) {
        viewModelScope.launch { preferencesManager.setAzanVolume(volume) }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setReminderEnabled(enabled) }
    }

    fun changeReminderTime(minutes: Int) {
        viewModelScope.launch { preferencesManager.setReminderMinutes(minutes) }
    }

    fun toggle24HourFormat(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.set24HourFormat(enabled) }
    }

    fun toggleHijriDate(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setShowHijriDate(enabled) }
    }

    fun changeHijriOffset(offsetDays: Int) {
        viewModelScope.launch { preferencesManager.setHijriOffsetDays(offsetDays) }
    }

    fun setBatteryOptimizationDisabled(disabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBatteryOptimizationDisabled(disabled) }
    }

    fun toggleOngoingNotification(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setOngoingNotificationEnabled(enabled) }
    }

    fun toggleAutomaticUpdates(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutomaticUpdatesEnabled(enabled)
            if (enabled) {
                UpdateScheduler.schedule(getApplication())
            } else {
                UpdateScheduler.cancel(getApplication())
            }
        }
    }

    fun checkForUpdates() {
        if (_updateState.value.isChecking) return
        viewModelScope.launch {
            _updateState.value = _updateState.value.copy(
                isChecking = true,
                message = "GitHub buraxılışları yoxlanılır…"
            )
            _updateState.value = when (val result = updateRepository.check(BuildConfig.VERSION_NAME)) {
                is UpdateCheckResult.Available -> UpdateUiState(
                    latestVersion = result.info.version,
                    updateAvailable = true,
                    message = "Yeni ${result.info.version} versiyası mövcuddur",
                    updateInfo = result.info
                )

                is UpdateCheckResult.UpToDate -> UpdateUiState(
                    latestVersion = result.latestVersion,
                    message = "Tətbiq yenidir — v${BuildConfig.VERSION_NAME}"
                )

                is UpdateCheckResult.Failure -> UpdateUiState(message = result.message)
            }
        }
    }

    fun downloadLatestUpdate() {
        val info = _updateState.value.updateInfo ?: return
        val application = getApplication<Application>()
        val notificationPermissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        val notificationsEnabled = NotificationManagerCompat.from(application)
            .areNotificationsEnabled()

        if (!notificationPermissionGranted || !notificationsEnabled) {
            _updateState.value = _updateState.value.copy(
                message = "Bildirişlər bağlıdır. APK-ni GitHub-dan əl ilə endirin."
            )
            openReleasesPage()
            return
        }

        UpdateDownloader.enqueue(application, info)
        _updateState.value = _updateState.value.copy(
            message = "v${info.version} arxa fonda endirilir. Hazır olduqda bildiriş gələcək."
        )
    }

    fun openReleasesPage() {
        val url = _updateState.value.updateInfo?.releaseUrl
            ?: "https://github.com/MAkbarov/Shia-Azan/releases/latest"
        getApplication<Application>().startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun changeCalculationMethod(method: CalculationMethod) {
        viewModelScope.launch { preferencesManager.setCalculationMethod(method) }
    }

    fun resetSettings() {
        viewModelScope.launch { preferencesManager.resetAllSettings() }
    }
}
