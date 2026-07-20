package az.shia.azan.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import az.shia.azan.data.PrayerType

/**
 * 🌙☀️ Hər namaz vaxtı üçün premium ikon və emoji
 */
object PrayerIcons {

    fun getIcon(type: PrayerType): ImageVector {
        return when (type) {
            PrayerType.FAJR -> Icons.Default.Brightness4
            PrayerType.SUNRISE -> Icons.Default.WbTwilight
            PrayerType.DHUHR -> Icons.Default.WbSunny
            PrayerType.ASR -> Icons.Default.Brightness7
            PrayerType.MAGHRIB -> Icons.Default.DarkMode
            PrayerType.ISHA -> Icons.Default.NightsStay
        }
    }

    fun getEmoji(type: PrayerType): String {
        return when (type) {
            PrayerType.FAJR -> "🌅"
            PrayerType.SUNRISE -> "🌇"
            PrayerType.DHUHR -> "☀️"
            PrayerType.ASR -> "🌤️"
            PrayerType.MAGHRIB -> "🌆"
            PrayerType.ISHA -> "🌙"
        }
    }
}
