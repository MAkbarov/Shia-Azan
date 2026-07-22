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
import az.shia.azan.update.downloadVerifiedApk
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

/** Tətbiqdaxili (foreground) yeniləmə axını — WorkManager/bildirişdən asılı deyil. */
sealed class ForegroundUpdate {
    object Idle : ForegroundUpdate()
    data class Available(val info: UpdateInfo) : ForegroundUpdate()
    data class Downloading(val info: UpdateInfo) : ForegroundUpdate()
    data class ReadyToInstall(val apkPath: String, val info: UpdateInfo) : ForegroundUpdate()
    data class Failed(val message: String, val info: UpdateInfo) : ForegroundUpdate()
}

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

    private val _foregroundUpdate = MutableStateFlow<ForegroundUpdate>(ForegroundUpdate.Idle)
    val foregroundUpdate: StateFlow<ForegroundUpdate> = _foregroundUpdate.asStateFlow()
    private var launchCheckStarted = false

    /** Tətbiq açılışında bir dəfə yoxla; yeni versiya varsa tətbiqdaxili dialoq göstər. */
    fun autoCheckOnLaunch() {
        if (launchCheckStarted) return
        launchCheckStarted = true
        viewModelScope.launch {
            val enabled = runCatching {
                preferencesManager.settingsFlow.first().automaticUpdatesEnabled
            }.getOrDefault(true)
            if (!enabled) return@launch
            if (_foregroundUpdate.value != ForegroundUpdate.Idle) return@launch
            val result = updateRepository.check(BuildConfig.VERSION_NAME)
            if (result is UpdateCheckResult.Available) {
                _foregroundUpdate.value = ForegroundUpdate.Available(result.info)
            }
        }
    }

    /** Dialoqda "İndi yenilə" — APK-ni endirib yoxlayır, hazır olduqda quraşdırmağa ötürür. */
    fun startForegroundDownload() {
        val info = when (val state = _foregroundUpdate.value) {
            is ForegroundUpdate.Available -> state.info
            is ForegroundUpdate.Failed -> state.info
            else -> return
        }
        _foregroundUpdate.value = ForegroundUpdate.Downloading(info)
        viewModelScope.launch {
            _foregroundUpdate.value = try {
                val apk = downloadVerifiedApk(getApplication(), info)
                ForegroundUpdate.ReadyToInstall(apk.absolutePath, info)
            } catch (e: Exception) {
                ForegroundUpdate.Failed(
                    e.message ?: "Yeniləmə endirilə bilmədi. GitHub-dan əl ilə endirin.",
                    info
                )
            }
        }
    }

    fun dismissForegroundUpdate() {
        _foregroundUpdate.value = ForegroundUpdate.Idle
    }

    fun openUpdateReleasePage() {
        val info = when (val state = _foregroundUpdate.value) {
            is ForegroundUpdate.Available -> state.info
            is ForegroundUpdate.Downloading -> state.info
            is ForegroundUpdate.ReadyToInstall -> state.info
            is ForegroundUpdate.Failed -> state.info
            else -> null
        }
        val url = info?.releaseUrl ?: "https://github.com/MAkbarov/Shia-Azan/releases/latest"
        getApplication<Application>().startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
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
