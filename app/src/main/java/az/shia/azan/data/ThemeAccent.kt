package az.shia.azan.data

/**
 * İstifadəçinin Parametrlərdən seçdiyi vurğu rəngi.
 * Rəng dəyərləri UI qatında palitraya çevrilir.
 */
enum class ThemeAccent(val displayName: String, val previewColor: Long) {
    TURQUOISE("Firuzəyi", 0xFF21C4C3),
    OCEAN("Dəniz mavisi", 0xFF2C7BE5),
    EMERALD_GREEN("Zümrüd yaşılı", 0xFF00A86B),
    ROYAL("Kral bənövşəyi", 0xFF7A5AF8),
    AMBER("Qızılı", 0xFFE8A33D),
    ROSE("Qızılgül", 0xFFE8506E)
}
