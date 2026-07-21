package az.shia.azan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.shia.azan.data.AppSettings
import az.shia.azan.data.AzanSound
import az.shia.azan.data.CalculationMethod
import az.shia.azan.data.PrayerType
import az.shia.azan.ui.components.AzanSoundSelectionDialog
import az.shia.azan.ui.components.CalculationMethodDialog
import az.shia.azan.ui.components.ReminderTimeDialog
import az.shia.azan.ui.components.VolumeSliderItem

/**
 * ⚙️ Parametrlər Səhifəsi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onPrayerNotificationToggle: (PrayerType, Boolean) -> Unit,
    onAzanSoundChange: (AzanSound) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeChange: (Int) -> Unit,
    on24HourToggle: (Boolean) -> Unit,
    onHijriToggle: (Boolean) -> Unit,
    onOngoingNotificationToggle: (Boolean) -> Unit,
    onCalculationMethodChange: (CalculationMethod) -> Unit,
    onBatteryOptimizationClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showAzanSoundDialog by remember { mutableStateOf(false) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    var showCalcMethodDialog by remember { mutableStateOf(false) }
    
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val appBarGradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = if (isDark) {
            listOf(az.shia.azan.ui.theme.GradientDarkStart, az.shia.azan.ui.theme.GradientDarkEnd)
        } else {
            listOf(az.shia.azan.ui.theme.GradientStart, az.shia.azan.ui.theme.GradientEnd)
        }
    )

    Scaffold(
        topBar = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.background(appBarGradient)
            ) {
                TopAppBar(
                    title = { Text("Parametrlər") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Geri")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                    subtitle = "Hicri tarixi göstər",
                    checked = settings.showHijriDate,
                    onCheckedChange = onHijriToggle
                )
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Azan səsi seçimi dialogu
    if (showAzanSoundDialog) {
        AzanSoundSelectionDialog(
            currentSound = settings.selectedAzanSound,
            onSelect = {
                onAzanSoundChange(it)
                showAzanSoundDialog = false
            },
            onDismiss = { showAzanSoundDialog = false }
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
