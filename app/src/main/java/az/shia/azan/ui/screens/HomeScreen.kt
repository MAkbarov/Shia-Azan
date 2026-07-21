package az.shia.azan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.shia.azan.R
import az.shia.azan.data.PrayerTime
import az.shia.azan.ui.components.NextPrayerCard
import az.shia.azan.ui.components.PrayerTimeCard
import az.shia.azan.ui.theme.CardShape
import az.shia.azan.ui.theme.GradientDarkEnd
import az.shia.azan.ui.theme.GradientDarkStart
import az.shia.azan.ui.theme.GradientEnd
import az.shia.azan.ui.theme.GradientStart
import az.shia.azan.ui.theme.PillShape
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
    
    val isDark = isSystemInDarkTheme()
    val appBarGradient = Brush.horizontalGradient(
        colors = if (isDark) {
            listOf(GradientDarkStart, GradientDarkEnd)
        } else {
            listOf(GradientStart, GradientEnd)
        }
    )

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(appBarGradient)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "XIV Azan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = TimeFormatter.formatDate(Calendar.getInstance()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
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
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
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
                    // Şəhər məlumatı - premium "pill" görünüş
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = PillShape
                                    )
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedLocation.cityName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(22.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Günün Namaz Vaxtları",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
