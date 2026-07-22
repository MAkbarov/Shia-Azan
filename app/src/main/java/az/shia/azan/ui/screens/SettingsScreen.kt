package az.shia.azan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.shia.azan.BuildConfig
import az.shia.azan.data.AppSettings
import az.shia.azan.data.AzanSound
import az.shia.azan.data.CalculationMethod
import az.shia.azan.data.PrayerType
import az.shia.azan.ui.components.AzanSoundSelectionDialog
import az.shia.azan.ui.components.CalculationMethodDialog
import az.shia.azan.ui.components.ReminderTimeDialog
import az.shia.azan.ui.components.VolumeSliderItem
import az.shia.azan.utils.HijriDateFormatter
import az.shia.azan.viewmodel.UpdateUiState
import java.util.Calendar

/**
 * ⚙️ Parametrlər Səhifəsi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    currentTime: Calendar,
    updateState: UpdateUiState,
    onPrayerNotificationToggle: (PrayerType, Boolean) -> Unit,
    onAzanSoundChange: (AzanSound) -> Unit,
    previewingSound: AzanSound?,
    onPreviewAzan: (AzanSound) -> Unit,
    onStopPreview: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeChange: (Int) -> Unit,
    on24HourToggle: (Boolean) -> Unit,
    onHijriToggle: (Boolean) -> Unit,
    onHijriOffsetChange: (Int) -> Unit,
    onOngoingNotificationToggle: (Boolean) -> Unit,
    onAutomaticUpdatesToggle: (Boolean) -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onOpenReleases: () -> Unit,
    onCalculationMethodChange: (CalculationMethod) -> Unit,
    onBatteryOptimizationClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showAzanSoundDialog by remember { mutableStateOf(false) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    var showCalcMethodDialog by remember { mutableStateOf(false) }
    
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val appBarGradient = remember(isDark) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = if (isDark) {
                listOf(az.shia.azan.ui.theme.GradientDarkStart, az.shia.azan.ui.theme.GradientDarkEnd)
            } else {
                listOf(az.shia.azan.ui.theme.GradientStart, az.shia.azan.ui.theme.GradientEnd)
            }
        )
    }
    // Hicri hesablaması ICU calendar yaradır; scroll zamanı təkrar allokasiyanın
    // qarşısını almaq üçün yalnız tarix/offset dəyişəndə hesabla.
    val hijriFormatted = remember(currentTime, settings.hijriOffsetDays) {
        HijriDateFormatter.format(currentTime, settings.hijriOffsetDays)
    }

    Scaffold(
        topBar = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.background(appBarGradient)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Parametrlər",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                        navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                        actionIconContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Bildirişlər bölməsi
            item {
                SectionHeader(Icons.Default.Notifications, "Namaz Bildirişləri")
            }
            
            item {
                PrayerNotificationItem(
                    prayerName = "Sübh",
                    enabled = settings.fajrNotificationEnabled,
                    onToggle = { onPrayerNotificationToggle(PrayerType.FAJR, it) }
                )
            }
            
            item {
                PrayerNotificationItem(
                    prayerName = "Zöhr",
                    enabled = settings.dhuhrNotificationEnabled,
                    onToggle = { onPrayerNotificationToggle(PrayerType.DHUHR, it) }
                )
            }
            
            item {
                PrayerNotificationItem(
                    prayerName = "Əsr",
                    enabled = settings.asrNotificationEnabled,
                    onToggle = { onPrayerNotificationToggle(PrayerType.ASR, it) }
                )
            }
            
            item {
                PrayerNotificationItem(
                    prayerName = "Məğrib",
                    enabled = settings.maghribNotificationEnabled,
                    onToggle = { onPrayerNotificationToggle(PrayerType.MAGHRIB, it) }
                )
            }
            
            item {
                PrayerNotificationItem(
                    prayerName = "İşa",
                    enabled = settings.ishaNotificationEnabled,
                    onToggle = { onPrayerNotificationToggle(PrayerType.ISHA, it) }
                )
            }
            
            // Azan səsi bölməsi
            item {
                SectionHeader(Icons.Default.MusicNote, "Azan Səsi")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.MusicNote,
                    title = "Azan Səsi",
                    subtitle = settings.selectedAzanSound.displayName,
                    onClick = { showAzanSoundDialog = true }
                )
            }
            
            item {
                VolumeSliderItem(
                    volume = settings.azanVolume,
                    onVolumeChange = onVolumeChange
                )
            }
            
            // Xatırlatma bölməsi
            item {
                SectionHeader(Icons.Default.Timer, "Xatırlatma")
            }
            
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Xatırlatma",
                    subtitle = "Namaz vaxtından əvvəl xəbərdar et",
                    checked = settings.reminderEnabled,
                    onCheckedChange = onReminderToggle
                )
            }
            
            if (settings.reminderEnabled) {
                item {
                    SettingsItem(
                        icon = Icons.Default.Timer,
                        title = "Xatırlatma Vaxtı",
                        subtitle = "${settings.reminderMinutesBefore} dəqiqə əvvəl",
                        onClick = { showReminderTimeDialog = true }
                    )
                }
            }
            
            // Görünüş bölməsi
            item {
                SectionHeader(Icons.Default.Schedule, "Görünüş")
            }
            
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Schedule,
                    title = "24 Saat Formatı",
                    subtitle = "Vaxtları 24 saat formatında göstər",
                    checked = settings.use24HourFormat,
                    onCheckedChange = on24HourToggle
                )
            }
            
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.CalendarToday,
                    title = "Hicri Tarix",
                    subtitle = "Hicri-qəməri tarixi Miladi tarixlə yanaşı göstər",
                    checked = settings.showHijriDate,
                    onCheckedChange = onHijriToggle
                )
            }

            if (settings.showHijriDate) {
                item {
                    HijriDateAdjustmentItem(
                        formattedDate = hijriFormatted,
                        offsetDays = settings.hijriOffsetDays,
                        onDecrease = {
                            onHijriOffsetChange(settings.hijriOffsetDays - 1)
                        },
                        onReset = { onHijriOffsetChange(0) },
                        onIncrease = {
                            onHijriOffsetChange(settings.hijriOffsetDays + 1)
                        }
                    )
                }
            }
            
            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Daimi Bildiriş",
                    subtitle = "İkon və namaz vaxtlarını yuxarıda sabitlə",
                    checked = settings.ongoingNotificationEnabled,
                    onCheckedChange = onOngoingNotificationToggle
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = "Hesablama Metodu",
                    subtitle = settings.calculationMethod.displayName,
                    onClick = { showCalcMethodDialog = true }
                )
            }
            
            // Arxa fon bölməsi
            item {
                SectionHeader(Icons.Default.BatteryChargingFull, "Arxa Fon")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Batareya Optimizasiyası",
                    subtitle = if (settings.batteryOptimizationDisabled) {
                        "Deaktiv ✓"
                    } else {
                        "Azanlar üçün deaktiv edin"
                    },
                    onClick = onBatteryOptimizationClick
                )
            }

            item {
                SectionHeader(Icons.Default.SystemUpdate, "Tətbiq Yeniləməsi")
            }

            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Download,
                    title = "Avtomatik yeniləmə",
                    subtitle = "Yeni buraxılışı avtomatik yoxla və təhlükəsiz endir",
                    checked = settings.automaticUpdatesEnabled,
                    onCheckedChange = onAutomaticUpdatesToggle
                )
            }

            item {
                UpdateSettingsCard(
                    currentVersion = BuildConfig.VERSION_NAME,
                    state = updateState,
                    onCheck = onCheckForUpdates,
                    onDownload = onDownloadUpdate,
                    onOpenReleases = onOpenReleases
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Azan səsi seçimi dialogu
    if (showAzanSoundDialog) {
        AzanSoundSelectionDialog(
            currentSound = settings.selectedAzanSound,
            previewingSound = previewingSound,
            onSelect = {
                onStopPreview()
                onAzanSoundChange(it)
                showAzanSoundDialog = false
            },
            onPreview = onPreviewAzan,
            onStopPreview = onStopPreview,
            onDismiss = {
                onStopPreview()
                showAzanSoundDialog = false
            }
        )
    }
    
    // Xatırlatma vaxtı dialogu
    if (showReminderTimeDialog) {
        ReminderTimeDialog(
            currentMinutes = settings.reminderMinutesBefore,
            onSelect = {
                onReminderTimeChange(it)
                showReminderTimeDialog = false
            },
            onDismiss = { showReminderTimeDialog = false }
        )
    }
    
    // Hesablama metodu dialogu
    if (showCalcMethodDialog) {
        CalculationMethodDialog(
            currentMethod = settings.calculationMethod,
            onSelect = { method ->
                onCalculationMethodChange(method)
                showCalcMethodDialog = false
            },
            onDismiss = { showCalcMethodDialog = false }
        )
    }
}

@Composable
fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}



@Composable
fun PrayerNotificationItem(
    prayerName: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prayerName,
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun HijriDateAdjustmentItem(
    formattedDate: String,
    offsetDays: Int,
    onDecrease: () -> Unit,
    onReset: () -> Unit,
    onIncrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ay müşahidəsi düzəlişi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (offsetDays == 0) {
                    "Standart hesab"
                } else {
                    "${if (offsetDays > 0) "+" else ""}$offsetDays gün"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FilledTonalIconButton(
                    onClick = onDecrease,
                    enabled = offsetDays > HijriDateFormatter.MIN_OFFSET_DAYS
                ) {
                    Icon(Icons.Default.Remove, "Hicri günü geri çək")
                }
                TextButton(
                    onClick = onReset,
                    enabled = offsetDays != 0,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text("Sıfırla")
                }
                FilledTonalIconButton(
                    onClick = onIncrease,
                    enabled = offsetDays < HijriDateFormatter.MAX_OFFSET_DAYS
                ) {
                    Icon(Icons.Default.Add, "Hicri günü irəli çək")
                }
            }
            Text(
                text = "Yerli Şiə ay müşahidəsi elanına uyğun −7…+7 gün tənzimləyin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UpdateSettingsCard(
    currentVersion: String,
    state: UpdateUiState,
    onCheck: () -> Unit,
    onDownload: () -> Unit,
    onOpenReleases: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "XIV Azan v$currentVersion",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = if (state.updateAvailable) onDownload else onCheck,
                    enabled = !state.isChecking,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (state.updateAvailable) {
                            Icons.Default.Download
                        } else {
                            Icons.Default.Refresh
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (state.updateAvailable) "Yenilə" else "Yoxla")
                }
                OutlinedButton(
                    onClick = onOpenReleases,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GitHub")
                }
            }
        }
    }
}
