package az.shia.azan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import az.shia.azan.audio.AzanPlayer
import az.shia.azan.data.PrayerTime
import az.shia.azan.location.LocationHelper
import az.shia.azan.notification.AlarmScheduler
import az.shia.azan.service.OngoingNotificationService
import az.shia.azan.ui.components.AzanPlayerDialog
import az.shia.azan.ui.screens.HomeScreen
import az.shia.azan.ui.screens.LocationSelectionScreen
import az.shia.azan.ui.screens.SettingsScreen
import az.shia.azan.ui.theme.ShiaAzanTheme
import az.shia.azan.utils.BatteryOptimizationHelper
import az.shia.azan.viewmodel.PrayerTimesViewModel
import az.shia.azan.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

private enum class AppScreen { HOME, LOCATION, SETTINGS }

/** Əsas Activity. */
class MainActivity : ComponentActivity() {

    private val viewModel: PrayerTimesViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var azanPlayer: AzanPlayer
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var locationHelper: LocationHelper
    private lateinit var batteryOptimizationHelper: BatteryOptimizationHelper

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) setupAlarms()
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bəzi OEM sistemləri XML statusBarColor-u öz accent rəngi ilə əvəz edir.
        // Loqonun/toolbar-ın başlanğıc firuzəyi rəngini pəncərəyə birbaşa tətbiq et.
        window.statusBarColor = android.graphics.Color.rgb(33, 196, 195)
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        azanPlayer = AzanPlayer(this)
        alarmScheduler = AlarmScheduler(this)
        locationHelper = LocationHelper(this)
        batteryOptimizationHelper = BatteryOptimizationHelper(this)

        batteryOptimizationHelper.registerLauncher { isIgnoring ->
            settingsViewModel.setBatteryOptimizationDisabled(isIgnoring)
        }
        viewModel.onPrayerTimesLoaded = { prayerTimes ->
            alarmScheduler.schedulePrayerAlarms(prayerTimes)
        }
        checkAndRequestPermissions()

        setContent {
            ShiaAzanTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var showPlayerDialog by remember { mutableStateOf(false) }
                var selectedPrayer by remember { mutableStateOf<PrayerTime?>(null) }
                var isLocating by remember { mutableStateOf(false) }
                var locationError by remember { mutableStateOf<String?>(null) }
                val composeScope = rememberCoroutineScope()
                val isPlaying by azanPlayer.isPlaying.collectAsState()
                val selectedLocation by viewModel.selectedLocation.collectAsState()
                val currentTime by viewModel.currentTime.collectAsState()
                val settings by settingsViewModel.settings.collectAsState()
                val updateState by settingsViewModel.updateState.collectAsState()

                val locateCurrentPosition: () -> Unit = {
                    if (!isLocating) {
                        isLocating = true
                        locationError = null
                        composeScope.launch {
                            val location = locationHelper.getCurrentLocation()
                            isLocating = false
                            if (location != null) {
                                viewModel.selectLocation(location)
                                currentScreen = AppScreen.HOME
                            } else {
                                locationError = "Dəqiq məkan alınmadı. GPS-i açıq saxlayıb yenidən cəhd edin."
                            }
                        }
                    }
                }

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted && locationHelper.hasFineLocationPermission()) {
                        locateCurrentPosition()
                    } else {
                        isLocating = false
                        locationError = "Dəqiq məkan üçün ‘Dəqiq’ yer icazəsini aktiv edin."
                    }
                }

                LaunchedEffect(settings.ongoingNotificationEnabled) {
                    if (settings.ongoingNotificationEnabled) {
                        OngoingNotificationService.startService(this@MainActivity)
                    } else {
                        OngoingNotificationService.stopService(this@MainActivity)
                    }
                }
                LaunchedEffect(settings.azanVolume) {
                    azanPlayer.setVolume(settings.azanVolume)
                }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = AppScreen.HOME
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            if (targetState == AppScreen.HOME) {
                                (slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(220)))
                                    .togetherWith(
                                        slideOutHorizontally(tween(240)) { it / 3 } + fadeOut(tween(180))
                                    )
                            } else {
                                (slideInHorizontally(tween(280)) { it } + fadeIn(tween(220)))
                                    .togetherWith(
                                        slideOutHorizontally(tween(240)) { -it / 3 } + fadeOut(tween(180))
                                    )
                            }
                        },
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.SETTINGS -> SettingsScreen(
                                settings = settings,
                                currentTime = currentTime,
                                updateState = updateState,
                                onPrayerNotificationToggle = settingsViewModel::togglePrayerNotification,
                                onAzanSoundChange = settingsViewModel::changeAzanSound,
                                onVolumeChange = settingsViewModel::changeVolume,
                                onReminderToggle = settingsViewModel::toggleReminder,
                                onReminderTimeChange = settingsViewModel::changeReminderTime,
                                on24HourToggle = settingsViewModel::toggle24HourFormat,
                                onHijriToggle = settingsViewModel::toggleHijriDate,
                                onHijriOffsetChange = settingsViewModel::changeHijriOffset,
                                onOngoingNotificationToggle = settingsViewModel::toggleOngoingNotification,
                                onAutomaticUpdatesToggle = settingsViewModel::toggleAutomaticUpdates,
                                onCheckForUpdates = settingsViewModel::checkForUpdates,
                                onDownloadUpdate = settingsViewModel::downloadLatestUpdate,
                                onOpenReleases = settingsViewModel::openReleasesPage,
                                onCalculationMethodChange = settingsViewModel::changeCalculationMethod,
                                onBatteryOptimizationClick = {
                                    batteryOptimizationHelper.requestIgnoreBatteryOptimizations()
                                },
                                onBackClick = { currentScreen = AppScreen.HOME }
                            )

                            AppScreen.LOCATION -> LocationSelectionScreen(
                                currentLocation = selectedLocation,
                                isLocating = isLocating,
                                locationError = locationError,
                                onLocationSelected = { location ->
                                    viewModel.selectLocation(location)
                                    currentScreen = AppScreen.HOME
                                },
                                onRequestGPS = {
                                    if (locationHelper.hasFineLocationPermission()) {
                                        locateCurrentPosition()
                                    } else {
                                        locationError = null
                                        locationPermissionLauncher.launch(
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    }
                                },
                                onBackClick = { currentScreen = AppScreen.HOME }
                            )

                            AppScreen.HOME -> HomeScreen(
                                viewModel = viewModel,
                                settings = settings,
                                onPlayAzan = { prayer ->
                                    selectedPrayer = prayer
                                    azanPlayer.playAzan(
                                        prayerType = prayer.type,
                                        sound = settings.selectedAzanSound,
                                        volume = settings.azanVolume
                                    )
                                    showPlayerDialog = true
                                },
                                onSettingsClick = { currentScreen = AppScreen.SETTINGS },
                                onLocationClick = {
                                    locationError = null
                                    currentScreen = AppScreen.LOCATION
                                }
                            )
                        }
                    }

                    if (showPlayerDialog && selectedPrayer != null) {
                        AzanPlayerDialog(
                            prayer = selectedPrayer!!,
                            isPlaying = isPlaying,
                            onPlay = {
                                azanPlayer.playAzan(
                                    prayerType = selectedPrayer!!.type,
                                    sound = settings.selectedAzanSound,
                                    volume = settings.azanVolume
                                )
                            },
                            onPause = azanPlayer::togglePause,
                            onStop = {
                                azanPlayer.stop()
                                showPlayerDialog = false
                            },
                            onDismiss = {
                                azanPlayer.stop()
                                showPlayerDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Tətbiq arxa fondan qayıdanda köhnə next-prayer/countdown qalmasın.
        viewModel.updateCurrentTime()
    }

    override fun onDestroy() {
        azanPlayer.release()
        super.onDestroy()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                setupAlarms()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            setupAlarms()
        }
    }

    private fun setupAlarms() {
        viewModel.prayerTimes.value?.let(alarmScheduler::schedulePrayerAlarms)
    }
}
