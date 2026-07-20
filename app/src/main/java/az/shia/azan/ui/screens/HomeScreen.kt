package az.shia.azan.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.shia.azan.data.PrayerTime
import az.shia.azan.ui.components.NextPrayerCard
import az.shia.azan.ui.components.PrayerTimeCard
import az.shia.azan.utils.TimeFormatter
import az.shia.azan.viewmodel.PrayerTimesViewModel
import java.util.Calendar

/**
 * Ana səhifə ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PrayerTimesViewModel,
    onPlayAzan: (PrayerTime) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLocationClick: () -> Unit = {}
) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    
    // Vaxtı hər dəqiqə yenilə
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000) // 1 dəqiqə
            viewModel.updateCurrentTime()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Şiə Azan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = TimeFormatter.formatDate(Calendar.getInstance()),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLocationClick) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Yer"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Parametrlər"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (prayerTimes == null) {
                // Yükləmə göstəricisi
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Şəhər məlumatı
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedLocation.cityName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Növbəti namaz kartı
                    item {
                        NextPrayerCard(
                            nextPrayer = nextPrayer,
                            currentTime = currentTime
                        )
                    }
                    
                    // Namaz vaxtları siyahısı
                    item {
                        Text(
                            text = "Günün Namaz Vaxtları",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }
                    
                    prayerTimes?.let { times ->
                        items(times.getAllPrayers()) { prayer ->
                            PrayerTimeCard(
                                prayer = prayer,
                                isNextPrayer = prayer.type == nextPrayer?.type,
                                onPlayAzan = { onPlayAzan(prayer) }
                            )
                        }
                    }
                    
                    // Alt boşluq
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
