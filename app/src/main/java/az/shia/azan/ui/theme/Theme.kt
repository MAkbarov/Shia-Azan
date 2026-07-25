package az.shia.azan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import az.shia.azan.data.ThemeAccent

private fun darkSchemeFor(palette: AccentPalette) = darkColorScheme(
    primary = palette.primaryOnDark,
    onPrimary = Color(0xFF04222A),
    primaryContainer = palette.darkSurfaceVariant,
    onPrimaryContainer = palette.primaryOnDark,
    secondary = palette.secondaryOnDark,
    onSecondary = Color(0xFF04222A),
    secondaryContainer = palette.darkSurfaceVariant,
    onSecondaryContainer = palette.secondaryOnDark,
    tertiary = palette.tertiaryOnDark,
    onTertiary = palette.darkBackground,
    background = palette.darkBackground,
    onBackground = Color(0xFFEAF4F4),
    surface = palette.darkSurface,
    onSurface = Color(0xFFEAF4F4),
    surfaceVariant = palette.darkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFD3D4),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF7B9698),
    outlineVariant = Color(0xFF3A5A5E)
)

private fun lightSchemeFor(palette: AccentPalette) = lightColorScheme(
    primary = palette.primary,
    onPrimary = Color.White,
    primaryContainer = palette.lightSurfaceVariant,
    onPrimaryContainer = palette.primaryDark,
    secondary = palette.secondary,
    onSecondary = Color.White,
    secondaryContainer = palette.lightSurfaceVariant,
    onSecondaryContainer = palette.primaryDark,
    tertiary = palette.tertiary,
    onTertiary = Color.White,
    background = palette.lightBackground,
    onBackground = Color(0xFF14262A),
    surface = Color.White,
    onSurface = Color(0xFF14262A),
    surfaceVariant = palette.lightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A6266),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF7A9296),
    outlineVariant = Color(0xFFC7DEDD)
)

/** Seçilmiş vurğu rənginə uyğun XIV Azan teması; sistem dark mode-u izlənir. */
@Composable
fun ShiaAzanTheme(
    accent: ThemeAccent = ThemeAccent.TURQUOISE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = remember(accent) { paletteFor(accent) }
    val colorScheme = remember(accent, darkTheme) {
        if (darkTheme) darkSchemeFor(palette) else lightSchemeFor(palette)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ShiaAzanShapes,
        content = content
    )
}

/** Bütün ekranlarda istifadə olunan premium başlıq qradiyenti. */
@Composable
fun rememberAppBarBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    return remember(scheme.primary, scheme.tertiary, dark) {
        Brush.linearGradient(
            colors = if (dark) {
                listOf(scheme.background, scheme.surfaceVariant, scheme.primary.copy(alpha = 0.55f))
            } else {
                listOf(scheme.primary, scheme.secondary, scheme.tertiary)
            }
        )
    }
}

/** Kartlar üçün yumşaq səth qradiyenti. */
@Composable
fun rememberSurfaceBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme.surface, scheme.surfaceVariant) {
        Brush.verticalGradient(
            colors = listOf(scheme.surface, scheme.surfaceVariant.copy(alpha = 0.55f))
        )
    }
}
