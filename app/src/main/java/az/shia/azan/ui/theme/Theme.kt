package az.shia.azan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 🌙 Qaranlıq Rejim - Bənövşəyi Tema
 */
private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDarkMode,
    onPrimary = Color(0xFF1A0B1F),
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PurplePrimaryDarkMode,
    
    secondary = PurpleSecondaryDarkMode,
    onSecondary = Color(0xFF1A0B1F),
    secondaryContainer = Color(0xFF4A148C),
    onSecondaryContainer = PurpleSecondaryDarkMode,
    
    tertiary = PurpleTertiaryDarkMode,
    onTertiary = Color.White,
    
    background = DarkBackground,
    onBackground = Color(0xFFE8DEF8),
    
    surface = DarkSurface,
    onSurface = Color(0xFFE8DEF8),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = Color(0xFF7B6F7F),
    outlineVariant = Color(0xFF4A4458)
)

/**
 * ☀️ İşıqlı Rejim - Bənövşəyi Tema
 */
private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = PurplePrimaryDark,
    
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = PurpleSecondaryLight,
    onSecondaryContainer = PurplePrimaryDark,
    
    tertiary = PurpleTertiary,
    onTertiary = Color.White,
    
    background = LightBackground,
    onBackground = Color(0xFF1C1B1F),
    
    surface = LightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

/**
 * 🎨 Premium Şiə Azan Teması
 * Sistem dark mode-nu avtomatik izləyir
 */
@Composable
fun ShiaAzanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Android 12+ dinamik rənglər
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) {
                androidx.compose.material3.dynamicDarkColorScheme(context)
            } else {
                androidx.compose.material3.dynamicLightColorScheme(context)
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ShiaAzanShapes,
        content = content
    )
}
