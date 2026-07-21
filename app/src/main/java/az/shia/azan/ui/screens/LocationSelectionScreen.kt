package az.shia.azan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import az.shia.azan.data.LocationData
import az.shia.azan.data.ShiaCities

/** Şəhər seçimi ekranı. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectionScreen(
    currentLocation: LocationData,
    isLocating: Boolean,
    locationError: String?,
    onLocationSelected: (LocationData) -> Unit,
    onRequestGPS: () -> Unit,
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val cities = remember(searchQuery) { ShiaCities.searchCities(searchQuery) }
    val categorizedCities = remember(searchQuery) {
        if (searchQuery.isEmpty()) ShiaCities.categories else emptyMap()
    }

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
            Box(modifier = Modifier.background(appBarGradient)) {
                TopAppBar(
                    title = { Text("Şəhər Seçimi") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(enabled = !isLocating, onClick = onRequestGPS),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLocating) {
                                "Dəqiq məkan müəyyən edilir..."
                            } else {
                                "Hazırkı Yerimi İstifadə Et"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLocating) {
                                "GPS siqnalı gözlənilir, bir neçə saniyə çəkə bilər"
                            } else {
                                "Yeni yüksək dəqiqlikli GPS nəticəsi al və seç"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        locationError?.let { error ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Şəhər axtar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Axtar") },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    categorizedCities.forEach { (category, cityList) ->
                        item { CategoryHeader(category) }
                        items(cityList) { city ->
                            CityListItem(
                                city = city,
                                isSelected = city.cityName == currentLocation.cityName &&
                                    city.countryName == currentLocation.countryName,
                                onSelect = { onLocationSelected(city) }
                            )
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Axtarış Nəticələri (${cities.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(cities) { city ->
                        CityListItem(
                            city = city,
                            isSelected = city.cityName == currentLocation.cityName &&
                                city.countryName == currentLocation.countryName,
                            onSelect = { onLocationSelected(city) }
                        )
                    }
                }

                if (cities.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Heç bir şəhər tapılmadı",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityListItem(
    city: LocationData,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = "${city.latitude}°, ${city.longitude}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçilib",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
