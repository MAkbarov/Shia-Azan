package az.shia.azan.data

/**
 * Yer məlumatı
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
    val countryName: String = "",
    val timeZone: String = "Asia/Baku"
)

/**
 * 🌍 Şiə mərkəzləri və şəhərləri
 */
object ShiaCities {
    
    // Azərbaycan
    val azerbaijanCities = listOf(
        LocationData(40.4093, 49.8671, "Bakı", "Azərbaycan", "Asia/Baku"),
        LocationData(40.3777, 49.8920, "Sumqayıt", "Azərbaycan", "Asia/Baku"),
        LocationData(40.6828, 46.3606, "Gəncə", "Azərbaycan", "Asia/Baku"),
        LocationData(39.2095, 48.8520, "Lənkəran", "Azərbaycan", "Asia/Baku"),
        LocationData(41.1919, 48.8578, "Quba", "Azərbaycan", "Asia/Baku"),
        LocationData(40.5088, 50.0618, "Şamaxı", "Azərbaycan", "Asia/Baku"),
        LocationData(39.8266, 46.7656, "Şuşa", "Azərbaycan", "Asia/Baku")
    )
    
    // İran - Şiə mərkəzləri
    val iranCities = listOf(
        LocationData(34.6416, 50.8746, "Qum", "İran", "Asia/Tehran"),
        LocationData(36.2974, 59.6059, "Məşhəd", "İran", "Asia/Tehran"),
        LocationData(35.6892, 51.3890, "Tehran", "İran", "Asia/Tehran"),
        LocationData(32.6546, 51.6680, "İsfahan", "İran", "Asia/Tehran"),
        LocationData(29.6036, 52.5388, "Şiraz", "İran", "Asia/Tehran"),
        LocationData(38.0792, 46.2978, "Təbriz", "İran", "Asia/Tehran")
    )
    
    // İraq - Müqəddəs şəhərlər
    val iraqCities = listOf(
        LocationData(33.3152, 44.3661, "Bağdad", "İraq", "Asia/Baghdad"),
        LocationData(32.0218, 44.3452, "Nəcəf", "İraq", "Asia/Baghdad"),
        LocationData(32.6160, 44.0246, "Kərbəla", "İraq", "Asia/Baghdad"),
        LocationData(34.3467, 44.3959, "Səmərrə", "İraq", "Asia/Baghdad"),
        LocationData(33.7737, 44.0260, "Kəziməyn", "İraq", "Asia/Baghdad")
    )
    
    // Livan
    val lebanonCities = listOf(
        LocationData(33.8886, 35.4955, "Beyrut", "Livan", "Asia/Beirut"),
        LocationData(33.5500, 36.4000, "Bəəlbək", "Livan", "Asia/Beirut")
    )
    
    // Səudiyyə Ərəbistanı
    val saudiCities = listOf(
        LocationData(21.4225, 39.8262, "Məkkə", "Səudiyyə", "Asia/Riyadh"),
        LocationData(24.4672, 39.6122, "Mədinə", "Səudiyyə", "Asia/Riyadh")
    )
    
    // Bütün şəhərlər
    val allCities: List<LocationData> = 
        azerbaijanCities + iranCities + iraqCities + lebanonCities + saudiCities
    
    // Kategoriyalar
    val categories = mapOf(
        "🇦🇿 Azərbaycan" to azerbaijanCities,
        "🇮🇷 İran" to iranCities,
        "🇮🇶 İraq (Müqəddəs Şəhərlər)" to iraqCities,
        "🇱🇧 Livan" to lebanonCities,
        "🇸🇦 Səudiyyə Ərəbistanı" to saudiCities
    )
    
    fun getDefaultCity(): LocationData = azerbaijanCities.first()
    
    fun searchCities(query: String): List<LocationData> {
        if (query.isBlank()) return allCities
        return allCities.filter {
            it.cityName.contains(query, ignoreCase = true) ||
            it.countryName.contains(query, ignoreCase = true)
        }
    }
}
