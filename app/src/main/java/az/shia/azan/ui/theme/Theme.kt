package az.shia.azan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimaryDarkMode,
    onPrimary = Color(0xFF052B30),
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PurplePrimaryDarkMode,
    secondary = PurpleSecondaryDarkMode,
    onSecondary = Color(0xFF052B30),
    secondaryContainer = Color(0xFF10444B),
    onSecondaryContainer = PurpleSecondaryDarkMode,
    tertiary = PurpleTertiaryDarkMode,
    onTertiary = Color(0xFF071D22),
    background = DarkBackground,
    onBackground = Color(0xFFD8F5F3),
    surface = DarkSurface,
    onSurface = Color(0xFFD8F5F3),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB5D7D5),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF6F918F),
    outlineVariant = Color(0xFF355B5D)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = PurplePrimaryDark,
    secondary = PurpleSecondary,
    onSecondary = Color.White,
    secondaryContainer = PurpleSecondaryLight,
    onSecondaryContainer = Color(0xFF07545E),
    tertiary = PurpleTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF102A2E),
    surface = LightSurface,
    onSurface = Color(0xFF102A2E),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF3F5C60),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF6B8589),
    outlineVariant = Color(0xFFB8D6D4)
)

/** Loqoya uyğun firuzəyi XIV Azan teması; sistem dark mode-u avtomatik izlənir. */
@Composable
fun ShiaAzanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
