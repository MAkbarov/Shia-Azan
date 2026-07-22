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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn as toastFadeIn
import androidx.compose.animation.fadeOut as toastFadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import az.shia.azan.audio.AzanPlayer
import az.shia.azan.data.PrayerTime
import az.shia.azan.data.PrayerType
import az.shia.azan.location.LocationHelper
import az.shia.azan.notification.AlarmScheduler
import az.shia.azan.service.OngoingNotificationService
import az.shia.azan.ui.components.AzanPlayerDialog
import az.shia.azan.ui.screens.HomeScreen
import az.shia.azan.ui.screens.LocationSelectionScreen
import az.shia.azan.ui.screens.SettingsScreen
import az.shia.azan.ui.theme.ShiaAzanTheme
import az.shia.azan.update.UpdateInstaller
import az.shia.azan.utils.BatteryOptimizationHelper
import az.shia.azan.viewmodel.ForegroundUpdate
import az.shia.azan.viewmodel.PrayerTimesViewModel
import az.shia.azan.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

private enum class AppScreen { HOME, LOCATION, SETTINGS }

/** Tətbiqdaxili yeniləmə dialoqu: yoxla → endir → quraşdır, GitHub fallback ilə. */
@Composable
private fun UpdateFlowDialog(
    state: ForegroundUpdate,
    onUpdateNow: () -> Unit,
    onInstall: (String) -> Unit,
    onOpenGitHub: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state is ForegroundUpdate.Idle) return

    // APK hazır olan kimi sistem quraşdırıcısını avtomatik aç.
    if (state is ForegroundUpdate.ReadyToInstall) {
        LaunchedEffect(state.apkPath) { onInstall(state.apkPath) }
    }

    val info = when (state) {
        is ForegroundUpdate.Available -> state.info
        is ForegroundUpdate.Downloading -> state.info
        is ForegroundUpdate.ReadyToInstall -> state.info
        is ForegroundUpdate.Failed -> state.info
        ForegroundUpdate.Idle -> return
    }
    val downloading = state is ForegroundUpdate.Downloading

    AlertDialog(
        onDismissRequest = { if (!downloading) onDismiss() },
        title = { Text("Yeni versiya: v${info.version}") },
        text = {
            when (state) {
                is ForegroundUpdate.Downloading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.width(22.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Yeniləmə endirilir…")
                }
                is ForegroundUpdate.ReadyToInstall ->
                    Text("Yeniləmə hazırdır. Quraşdırmanı təsdiqləyin.")
                is ForegroundUpdate.Failed ->
                    Text("${state.message}\n\nGitHub-dan əl ilə endirib köhnənin üstünə quraşdıra bilərsiniz.")
                else ->
                    Text("Tətbiqin yeni versiyası mövcuddur. İndi yeniləmək istəyirsiniz?")
            }
        },
        confirmButton = {
            when (state) {
                is ForegroundUpdate.Available -> Button(onClick = onUpdateNow) { Text("İndi yenilə") }
                is ForegroundUpdate.ReadyToInstall ->
                    Button(onClick = { onInstall(state.apkPath) }) { Text("Quraşdır") }
                is ForegroundUpdate.Failed -> Button(onClick = onOpenGitHub) { Text("GitHub-da aç") }
                else -> {}
            }
        },
        dismissButton = {
            if (!downloading) {
                TextButton(onClick = onDismiss) {
                    Text(if (state is ForegroundUpdate.Failed) "Bağla" else "Sonra")
                }
            }
        }
    )
}

/** Yenilənmədən sonra 4 saniyəlik firuzəyi toast (mətn ağ). */
@Composable
private fun UpdatedToast(
    version: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(version) {
        if (version != null) {
            kotlinx.coroutines.delay(4000L)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = version != null,
        enter = toastFadeIn(tween(220)),
        exit = toastFadeOut(tween(300)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF21C4C3))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Tətbiq v${version.orEmpty()} versiyasına yeniləndi",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

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

                val justUpdatedVersion by settingsViewModel.justUpdatedVersion.collectAsState()
                val foregroundUpdate by settingsViewModel.foregroundUpdate.collectAsState()
                var previewingSound by remember { mutableStateOf<az.shia.azan.data.AzanSound?>(null) }

                // Açılışda bir dəfə tətbiqdaxili yeniləmə yoxlaması (WorkManager-dən asılı deyil).
                LaunchedEffect(Unit) { settingsViewModel.autoCheckOnLaunch() }

                // Preview bitəndə (və ya dayandırılanda) play ikonlarını sıfırla.
                LaunchedEffect(isPlaying) {
                    if (!isPlaying) previewingSound = null
                }
                // Parametrlər ekranından çıxdıqda dinlənən azanı dayandır.
                LaunchedEffect(currentScreen) {
                    if (currentScreen != AppScreen.SETTINGS && previewingSound != null) {
                        azanPlayer.stop()
                        previewingSound = null
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                  Box(modifier = Modifier.fillMaxSize()) {
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
                                previewingSound = previewingSound,
                                onPreviewAzan = { sound ->
                                    azanPlayer.playAzan(
                                        prayerType = PrayerType.DHUHR,
                                        sound = sound,
                                        volume = settings.azanVolume
                                    )
                                    previewingSound = sound
                                },
                                onStopPreview = {
                                    azanPlayer.stop()
                                    previewingSound = null
                                },
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

                    UpdatedToast(
                        version = justUpdatedVersion,
                        onDismiss = settingsViewModel::clearUpdateToast,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    UpdateFlowDialog(
                        state = foregroundUpdate,
                        onUpdateNow = {
                            if (UpdateInstaller.canRequestInstall(this@MainActivity)) {
                                settingsViewModel.startForegroundDownload()
                            } else {
                                UpdateInstaller.requestInstallPermission(this@MainActivity)
                            }
                        },
                        onInstall = { path ->
                            val launched = UpdateInstaller.launchInstaller(
                                this@MainActivity,
                                java.io.File(path)
                            )
                            if (!launched) settingsViewModel.openUpdateReleasePage()
                        },
                        onOpenGitHub = settingsViewModel::openUpdateReleasePage,
                        onDismiss = settingsViewModel::dismissForegroundUpdate
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
