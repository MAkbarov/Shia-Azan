package az.shia.azan.ui.theme

import androidx.compose.ui.graphics.Color

// Loqodakı firuzəyi, mavi və nanə tonlarından qurulan əsas palitra.
val PurplePrimary = Color(0xFF149DB5)
val PurplePrimaryLight = Color(0xFF21C4C3)
val PurplePrimaryDark = Color(0xFF0B7285)
val PurpleSecondary = Color(0xFF22BFC1)
val PurpleSecondaryLight = Color(0xFF7CCFD0)
val PurpleTertiary = Color(0xFF173D8D)

// İşıqlı rejim səthləri.
val LightBackground = Color(0xFFF4FBFB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFDDF5F0)

// Qaranlıq rejim üçün firuzəyi-qara səthlər.
val PurplePrimaryDarkMode = Color(0xFF69DBD6)
val PurpleSecondaryDarkMode = Color(0xFF7CCFD0)
val PurpleTertiaryDarkMode = Color(0xFF8FB8FF)
val DarkBackground = Color(0xFF071D22)
val DarkSurface = Color(0xFF0D2C33)
val DarkSurfaceVariant = Color(0xFF123E47)

val GoldAccent = Color(0xFFF5C85B)
val GreenSuccess = Color(0xFF35B779)
val ErrorRed = Color(0xFFE5484D)

val NextPrayerLightBg = Color(0xFFE6F8F4)
val NextPrayerDarkBg = Color(0xFF123E47)

// Loqonun açıq firuzəyisindən tünd mavi tonuna keçid.
val GradientStart = Color(0xFF21C4C3)
val GradientMiddle = Color(0xFF149DB5)
val GradientEnd = Color(0xFF173D8D)

val GradientDarkStart = Color(0xFF071D22)
val GradientDarkMiddle = Color(0xFF0B7285)
val GradientDarkEnd = Color(0xFF149DB5)

/** Bir vurğu rəngi üçün tam palitra. */
data class AccentPalette(
    val primaryLight: Color,
    val primary: Color,
    val primaryDark: Color,
    val secondary: Color,
    val tertiary: Color,
    val primaryOnDark: Color,
    val secondaryOnDark: Color,
    val tertiaryOnDark: Color,
    val lightBackground: Color,
    val lightSurfaceVariant: Color,
    val darkBackground: Color,
    val darkSurface: Color,
    val darkSurfaceVariant: Color
)

val TurquoisePalette = AccentPalette(
    primaryLight = Color(0xFF21C4C3),
    primary = Color(0xFF149DB5),
    primaryDark = Color(0xFF0B7285),
    secondary = Color(0xFF22BFC1),
    tertiary = Color(0xFF173D8D),
    primaryOnDark = Color(0xFF69DBD6),
    secondaryOnDark = Color(0xFF7CCFD0),
    tertiaryOnDark = Color(0xFF8FB8FF),
    lightBackground = Color(0xFFF4FBFB),
    lightSurfaceVariant = Color(0xFFDDF5F0),
    darkBackground = Color(0xFF071D22),
    darkSurface = Color(0xFF0D2C33),
    darkSurfaceVariant = Color(0xFF123E47)
)

val OceanPalette = AccentPalette(
    primaryLight = Color(0xFF4F9BFF),
    primary = Color(0xFF2C7BE5),
    primaryDark = Color(0xFF14508F),
    secondary = Color(0xFF3E8EF7),
    tertiary = Color(0xFF14307A),
    primaryOnDark = Color(0xFF93C2FF),
    secondaryOnDark = Color(0xFFA8CDFF),
    tertiaryOnDark = Color(0xFFB6C8FF),
    lightBackground = Color(0xFFF3F7FE),
    lightSurfaceVariant = Color(0xFFDCE8FB),
    darkBackground = Color(0xFF08131F),
    darkSurface = Color(0xFF0E2135),
    darkSurfaceVariant = Color(0xFF152F4C)
)

val EmeraldGreenPalette = AccentPalette(
    primaryLight = Color(0xFF19C685),
    primary = Color(0xFF00A86B),
    primaryDark = Color(0xFF046B46),
    secondary = Color(0xFF12B879),
    tertiary = Color(0xFF0A5C3C),
    primaryOnDark = Color(0xFF6BE0AE),
    secondaryOnDark = Color(0xFF88E7C0),
    tertiaryOnDark = Color(0xFF9FE9C9),
    lightBackground = Color(0xFFF1FBF6),
    lightSurfaceVariant = Color(0xFFD5F2E4),
    darkBackground = Color(0xFF041A12),
    darkSurface = Color(0xFF092A1F),
    darkSurfaceVariant = Color(0xFF0F3D2D)
)

val RoyalPalette = AccentPalette(
    primaryLight = Color(0xFF9B84FF),
    primary = Color(0xFF7A5AF8),
    primaryDark = Color(0xFF4A2FBF),
    secondary = Color(0xFF8C6BFF),
    tertiary = Color(0xFF3A2A8C),
    primaryOnDark = Color(0xFFC3B2FF),
    secondaryOnDark = Color(0xFFCFC0FF),
    tertiaryOnDark = Color(0xFFD5CBFF),
    lightBackground = Color(0xFFF7F5FF),
    lightSurfaceVariant = Color(0xFFE6E0FB),
    darkBackground = Color(0xFF120F26),
    darkSurface = Color(0xFF1B1738),
    darkSurfaceVariant = Color(0xFF272050)
)

val AmberPalette = AccentPalette(
    primaryLight = Color(0xFFF5B95C),
    primary = Color(0xFFE8A33D),
    primaryDark = Color(0xFFA96C12),
    secondary = Color(0xFFEFAF4A),
    tertiary = Color(0xFF7E4E12),
    primaryOnDark = Color(0xFFFFD79A),
    secondaryOnDark = Color(0xFFFFE0AE),
    tertiaryOnDark = Color(0xFFFFD9A8),
    lightBackground = Color(0xFFFFFAF1),
    lightSurfaceVariant = Color(0xFFF9EAD2),
    darkBackground = Color(0xFF1D1405),
    darkSurface = Color(0xFF2C200B),
    darkSurfaceVariant = Color(0xFF3F2E11)
)

val RosePalette = AccentPalette(
    primaryLight = Color(0xFFFF7690),
    primary = Color(0xFFE8506E),
    primaryDark = Color(0xFFAA2543),
    secondary = Color(0xFFF2637F),
    tertiary = Color(0xFF7E1B33),
    primaryOnDark = Color(0xFFFFA9BA),
    secondaryOnDark = Color(0xFFFFBCC8),
    tertiaryOnDark = Color(0xFFFFB7C6),
    lightBackground = Color(0xFFFFF5F7),
    lightSurfaceVariant = Color(0xFFFBE0E6),
    darkBackground = Color(0xFF1F0A10),
    darkSurface = Color(0xFF2F1119),
    darkSurfaceVariant = Color(0xFF451923)
)

fun paletteFor(accent: az.shia.azan.data.ThemeAccent): AccentPalette = when (accent) {
    az.shia.azan.data.ThemeAccent.TURQUOISE -> TurquoisePalette
    az.shia.azan.data.ThemeAccent.OCEAN -> OceanPalette
    az.shia.azan.data.ThemeAccent.EMERALD_GREEN -> EmeraldGreenPalette
    az.shia.azan.data.ThemeAccent.ROYAL -> RoyalPalette
    az.shia.azan.data.ThemeAccent.AMBER -> AmberPalette
    az.shia.azan.data.ThemeAccent.ROSE -> RosePalette
}
