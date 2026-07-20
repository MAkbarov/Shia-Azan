package az.shia.azan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.shia.azan.data.PrayerTime
import androidx.compose.foundation.isSystemInDarkTheme
import az.shia.azan.ui.theme.NextPrayerLightBg
import az.shia.azan.ui.theme.NextPrayerDarkBg
import az.shia.azan.utils.TimeFormatter

/**
 * Namaz vaxtı kartı komponenti
 */
@Composable
fun PrayerTimeCard(
    prayer: PrayerTime,
    isNextPrayer: Boolean = false,
    onPlayAzan: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    
    // 🎨 Smooth animasiyalar
    val elevation by animateDpAsState(
        targetValue = if (isNextPrayer) 6.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "elevation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isNextPrayer) {
            if (isDark) NextPrayerDarkBg else NextPrayerLightBg
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "backgroundColor"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Namaz adı
            Column {
                Text(
                    text = prayer.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Medium,
                    color = if (isNextPrayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                
                if (isNextPrayer) {
                    Text(
                        text = "Növbəti namaz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Vaxt və düymə
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = TimeFormatter.formatTime(prayer.time),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = if (isNextPrayer) FontWeight.Bold else FontWeight.Medium,
                    color = if (isNextPrayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = if (isNextPrayer) 28.sp else 24.sp
                )
                
                // Azan oxutma düyməsi
                IconButton(
                    onClick = onPlayAzan,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Azan oxut",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
