package az.shia.azan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import az.shia.azan.audio.AzanPlayer
import az.shia.azan.data.PrayerTime
import az.shia.azan.location.LocationHelper
import az.shia.azan.notification.AlarmScheduler
import az.shia.azan.service.AzanService
import az.shia.azan.service.OngoingNotificationService
import az.shia.azan.ui.components.AzanPlayerDialog
import az.shia.azan.ui.screens.HomeScreen
import az.shia.azan.ui.screens.LocationSelectionScreen
import az.shia.azan.ui.screens.SettingsScreen
import az.shia.azan.viewmodel.SettingsViewModel
import az.shia.azan.ui.theme.ShiaAzanTheme
import az.shia.azan.viewmodel.PrayerTimesViewModel
import az.shia.azan.utils.BatteryOptimizationHelper
import kotlinx.coroutines.launch

/**
 * Əsas Activity
 */
class MainActivity : ComponentActivity() {
    
    private val viewModel: PrayerTimesViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private lateinit var azanPlayer: AzanPlayer
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var locationHelper: LocationHelper
    private lateinit var batteryOptimizationHelper: BatteryOptimizationHelper
    
    // İcazə sorğuları
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupAlarms()
        }
    }
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestGPSLocation()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        azanPlayer = AzanPlayer(this)
        alarmScheduler = AlarmScheduler(this)
        locationHelper = LocationHelper(this)
        batteryOptimizationHelper = BatteryOptimizationHelper(this)
        
        // Battery optimization launcher-i register et
        batteryOptimizationHelper.registerLauncher { isIgnoring ->
            settingsViewModel.setBatteryOptimizationDisabled(isIgnoring)
        }
        
        // ViewModel callback
        viewModel.onPrayerTimesLoaded = { prayerTimes ->
            alarmScheduler.schedulePrayerAlarms(prayerTimes)
        }
        
        // İcazələri yoxla və tələb et
        checkAndRequestPermissions()
        
        setContent {
            ShiaAzanTheme {
                var showPlayerDialog by remember { mutableStateOf(false) }
                var selectedPrayer by remember { mutableStateOf<PrayerTime?>(null) }
                var showLocationScreen by remember { mutableStateOf(false) }
                var showSettingsScreen by remember { mutableStateOf(false) }
                val isPlaying by azanPlayer.isPlaying.collectAsState()
                val selectedLocation by viewModel.selectedLocation.collectAsState()
                val settings by settingsViewModel.settings.collectAsState()
                
                // Daimi bildiriş servisini idarə et
                LaunchedEffect(settings.ongoingNotificationEnabled) {
                    if (settings.ongoingNotificationEnabled) {
                        OngoingNotificationService.startService(this@MainActivity)
                    } else {
                        OngoingNotificationService.stopService(this@MainActivity)
                    }
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        showSettingsScreen -> {
                            SettingsScreen(
                                settings = settings,
                                onPrayerNotificationToggle = { prayer, enabled ->
                                    settingsViewModel.togglePrayerNotification(prayer, enabled)
                                },
                                onAzanSoundChange = { sound ->
                                    settingsViewModel.changeAzanSound(sound)
                                },
                                onVolumeChange = { volume ->
                                    settingsViewModel.changeVolume(volume)
                                },
                                onReminderToggle = { enabled ->
                                    settingsViewModel.toggleReminder(enabled)
                                },
                                onReminderTimeChange = { minutes ->
                                    settingsViewModel.changeReminderTime(minutes)
                                },
                                on24HourToggle = { enabled ->
                                    settingsViewModel.toggle24HourFormat(enabled)
                                },
                                onHijriToggle = { enabled ->
                                    settingsViewModel.toggleHijriDate(enabled)
                                },
                                onOngoingNotificationToggle = { enabled ->
                                    settingsViewModel.toggleOngoingNotification(enabled)
                                },
                                onCalculationMethodChange = { method ->
                                    settingsViewModel.changeCalculationMethod(method)
                                },
                                onBatteryOptimizationClick = {
                                    batteryOptimizationHelper.requestIgnoreBatteryOptimizations()
                                },
                                onBackClick = {
                                    showSettingsScreen = false
                                }
                            )
                        }
                        showLocationScreen -> {
                            LocationSelectionScreen(
                                currentLocation = selectedLocation,
                                onLocationSelected = { location ->
                                    viewModel.selectLocation(location)
                                    showLocationScreen = false
                                },
                                onRequestGPS = {
                                    if (locationHelper.hasLocationPermission()) {
                                        requestGPSLocation()
                                        showLocationScreen = false
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                onBackClick = {
                                    showLocationScreen = false
                                }
                            )
                        }
                        else -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onPlayAzan = { prayer ->
                                    selectedPrayer = prayer
                                    azanPlayer.playAzan(prayer.type)
                                    showPlayerDialog = true
                                },
                                onSettingsClick = {
                                    showSettingsScreen = true
                                },
                                onLocationClick = {
                                    showLocationScreen = true
                                }
                            )
                        }
                    }
                    
                    // Azan player dialoqu
                    if (showPlayerDialog && selectedPrayer != null) {
                        AzanPlayerDialog(
                            prayer = selectedPrayer!!,
                            isPlaying = isPlaying,
                            onPlay = { azanPlayer.playAzan(selectedPrayer!!.type) },
                            onPause = { azanPlayer.togglePause() },
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
    
    /**
     * GPS-dən yer məlumatı al
     */
    private fun requestGPSLocation() {
        lifecycleScope.launch {
            val location = locationHelper.getCurrentLocation()
            location?.let {
                viewModel.selectLocation(it)
            }
        }
    }
    
    override fun onDestroy() {
        azanPlayer.release()
        super.onDestroy()
    }
    
    /**
     * İcazələri yoxla və tələb et
     */
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    setupAlarms()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            setupAlarms()
        }
    }
    
    /**
     * Namaz vaxtları üçün alarmları qur
     */
    private fun setupAlarms() {
        viewModel.prayerTimes.value?.let { prayerTimes ->
            alarmScheduler.schedulePrayerAlarms(prayerTimes)
        }
    }
}
