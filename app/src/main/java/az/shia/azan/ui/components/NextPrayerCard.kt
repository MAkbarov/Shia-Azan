package az.shia.azan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.shia.azan.data.PrayerTime
import androidx.compose.foundation.isSystemInDarkTheme
import az.shia.azan.ui.theme.GradientEnd
import az.shia.azan.ui.theme.GradientMiddle
import az.shia.azan.ui.theme.GradientStart
import az.shia.azan.ui.theme.GradientDarkEnd
import az.shia.azan.ui.theme.GradientDarkMiddle
import az.shia.azan.ui.theme.GradientDarkStart
import androidx.compose.ui.graphics.Brush
import az.shia.azan.utils.TimeFormatter
import java.util.Calendar

/**
 * Növbəti namaz məlumatı üçün böyük kart
 */
@Composable
fun NextPrayerCard(
    nextPrayer: PrayerTime?,
    currentTime: Calendar
) {
    if (nextPrayer == null) {
        return
    }
    
    val isDark = isSystemInDarkTheme()
    
    // 🎨 Premium animasiya
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val gradientBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                GradientDarkStart, 
                GradientDarkMiddle, 
                GradientDarkEnd,
                GradientDarkMiddle,
                GradientDarkStart
            )
        } else {
            listOf(
                GradientStart, 
                GradientMiddle, 
                GradientEnd,
                GradientMiddle,
                GradientStart
            )
        }
    )
    
    // Pulse animasiyası
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Növbəti Namaz",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = nextPrayer.name,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = TimeFormatter.formatTime(nextPrayer.time),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 36.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(
                modifier = Modifier.width(100.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                thickness = 2.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Qalan vaxt: ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                Text(
                    text = TimeFormatter.getTimeRemaining(nextPrayer.time, currentTime),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        }
    }
}
