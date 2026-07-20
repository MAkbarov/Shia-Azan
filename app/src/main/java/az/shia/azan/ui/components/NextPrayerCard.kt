package az.shia.azan.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import az.shia.azan.data.PrayerTime
import az.shia.azan.ui.theme.GradientDarkEnd
import az.shia.azan.ui.theme.GradientDarkMiddle
import az.shia.azan.ui.theme.GradientDarkStart
import az.shia.azan.ui.theme.GradientEnd
import az.shia.azan.ui.theme.GradientMiddle
import az.shia.azan.ui.theme.GradientStart
import az.shia.azan.ui.theme.HeroCardShape
import az.shia.azan.ui.theme.PillShape
import az.shia.azan.ui.theme.PrayerIcons
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
        shape = HeroCardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
        ) {
        // ✨ Dekorativ şəffaf dairələr - dərinlik hissi üçün
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 220.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = (-30).dp, y = 140.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Namaz ikonu - şüşə (glass) badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PrayerIcons.getIcon(nextPrayer.type),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "NÖVBƏTİ NAMAZ",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                letterSpacing = 2.sp
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
            
            Spacer(modifier = Modifier.height(18.dp))

            // Şüşə (glass) pill - qalan vaxt göstəricisi
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⏳ Qalan vaxt: ",
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
}
