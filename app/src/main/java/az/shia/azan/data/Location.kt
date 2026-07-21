package az.shia.azan.data

/** Yer məlumatı. */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
    val countryName: String = "",
    val timeZone: String = "Asia/Baku"
)

/** Şiə mərkəzləri və əvvəlcədən seçilə bilən şəhər/rayonlar. */
object ShiaCities {

    /**
     * Azərbaycan inzibati vahidləri və əsas şəhərlər.
     * Mənbə: Open Admin Data (CC-BY-4.0)
     * https://github.com/open-admin-data/azerbaijan-administrative-divisions
     *
     * Koordinatlar namaz vaxtı deyil; seçilmiş nöqtə üçün astronomik hesablamanın
     * girişidir. Azərbaycan 2016-dan daimi UTC+4 istifadə edir.
     */
    val azerbaijanCities = listOf(
        LocationData(40.4093, 49.8671, "Bakı", "Azərbaycan"),
        LocationData(40.4930, 49.7250, "Abşeron rayonu", "Azərbaycan"),
        LocationData(40.4481, 49.7550, "Xırdalan", "Azərbaycan"),
        LocationData(40.5940, 49.6410, "Sumqayıt", "Azərbaycan"),

        // Bakı şəhərinin inzibati rayonları
        LocationData(40.4440, 49.7730, "Binəqədi", "Azərbaycan"),
        LocationData(40.1740, 49.5270, "Qaradağ", "Azərbaycan"),
        LocationData(40.4510, 50.1700, "Xəzər", "Azərbaycan"),
        LocationData(40.3730, 49.9570, "Xətai", "Azərbaycan"),
        LocationData(40.4105, 49.8679, "Nərimanov", "Azərbaycan"),
        LocationData(40.4167, 49.8333, "Nəsimi", "Azərbaycan"),
        LocationData(40.3960, 49.8960, "Nizami", "Azərbaycan"),
        LocationData(40.3560, 50.5260, "Pirallahı", "Azərbaycan"),
        LocationData(40.5090, 49.9580, "Sabunçu", "Azərbaycan"),
        LocationData(40.3210, 49.8180, "Səbail", "Azərbaycan"),
        LocationData(40.4110, 50.0080, "Suraxanı", "Azərbaycan"),
        LocationData(40.3837, 49.8050, "Yasamal", "Azərbaycan"),

        // Naxçıvan Muxtar Respublikası
        LocationData(39.1920, 45.4000, "Naxçıvan", "Azərbaycan"),
        LocationData(39.2800, 45.4640, "Babək", "Azərbaycan"),
        LocationData(39.1470, 45.7010, "Culfa", "Azərbaycan"),
        LocationData(39.3860, 45.1940, "Kəngərli", "Azərbaycan"),
        LocationData(38.9960, 45.9310, "Ordubad", "Azərbaycan"),
        LocationData(39.7110, 44.9040, "Sədərək", "Azərbaycan"),
        LocationData(39.4360, 45.6100, "Şahbuz", "Azərbaycan"),
        LocationData(39.5430, 45.0150, "Şərur", "Azərbaycan"),

        // Respublikanın digər şəhər və rayonları
        LocationData(40.0380, 47.2770, "Ağcabədi", "Azərbaycan"),
        LocationData(40.0750, 47.0040, "Ağdam", "Azərbaycan"),
        LocationData(40.6080, 47.4410, "Ağdaş", "Azərbaycan"),
        LocationData(40.1670, 46.6790, "Ağdərə", "Azərbaycan"),
        LocationData(41.1820, 45.3950, "Ağstafa", "Azərbaycan"),
        LocationData(40.5480, 48.3670, "Ağsu", "Azərbaycan"),
        LocationData(38.5510, 48.7410, "Astara", "Azərbaycan"),
        LocationData(41.6850, 46.3520, "Balakən", "Azərbaycan"),
        LocationData(39.7600, 47.6920, "Beyləqan", "Azərbaycan"),
        LocationData(39.4620, 48.5280, "Biləsuvar", "Azərbaycan"),
        LocationData(40.3630, 47.1700, "Bərdə", "Azərbaycan"),
        LocationData(39.3540, 47.0120, "Cəbrayıl", "Azərbaycan"),
        LocationData(39.1990, 48.3980, "Cəlilabad", "Azərbaycan"),
        LocationData(40.4830, 46.0610, "Daşkəsən", "Azərbaycan"),
        LocationData(39.5730, 47.2790, "Füzuli", "Azərbaycan"),
        LocationData(40.6080, 45.6540, "Gədəbəy", "Azərbaycan"),
        LocationData(40.6828, 46.3606, "Gəncə", "Azərbaycan"),
        LocationData(40.6000, 46.6680, "Goranboy", "Azərbaycan"),
        LocationData(40.5830, 47.7980, "Göyçay", "Azərbaycan"),
        LocationData(40.5800, 46.3090, "Göygöl", "Azərbaycan"),
        LocationData(40.1010, 48.8930, "Hacıqabul", "Azərbaycan"),
        LocationData(39.8580, 48.0720, "İmişli", "Azərbaycan"),
        LocationData(40.7730, 48.2130, "İsmayıllı", "Azərbaycan"),
        LocationData(40.1000, 46.1220, "Kəlbəcər", "Azərbaycan"),
        LocationData(40.2560, 48.2030, "Kürdəmir", "Azərbaycan"),
        LocationData(39.7120, 46.4640, "Laçın", "Azərbaycan"),
        LocationData(38.7460, 48.4610, "Lerik", "Azərbaycan"),
        LocationData(38.7570, 48.7600, "Lənkəran", "Azərbaycan"),
        LocationData(39.0150, 48.6740, "Masallı", "Azərbaycan"),
        LocationData(40.7730, 46.9870, "Mingəçevir", "Azərbaycan"),
        LocationData(40.5000, 46.8150, "Naftalan", "Azərbaycan"),
        LocationData(39.3830, 49.0690, "Neftçala", "Azərbaycan"),
        LocationData(40.9900, 47.5140, "Oğuz", "Azərbaycan"),
        LocationData(41.3860, 46.8440, "Qax", "Azərbaycan"),
        LocationData(41.1150, 45.2280, "Qazax", "Azərbaycan"),
        LocationData(40.9030, 47.7760, "Qəbələ", "Azərbaycan"),
        LocationData(40.5340, 48.9090, "Qobustan", "Azərbaycan"),
        LocationData(41.2640, 48.5640, "Quba", "Azərbaycan"),
        LocationData(39.3350, 46.6130, "Qubadlı", "Azərbaycan"),
        LocationData(41.5110, 48.3680, "Qusar", "Azərbaycan"),
        LocationData(39.9020, 48.4360, "Saatlı", "Azərbaycan"),
        LocationData(39.9610, 48.6560, "Sabirabad", "Azərbaycan"),
        LocationData(39.6670, 48.9830, "Salyan", "Azərbaycan"),
        LocationData(40.8390, 46.3830, "Samux", "Azərbaycan"),
        LocationData(41.0390, 49.0540, "Siyəzən", "Azərbaycan"),
        LocationData(41.2030, 48.8860, "Şabran", "Azərbaycan"),
        LocationData(40.6440, 48.6290, "Şamaxı", "Azərbaycan"),
        LocationData(41.1110, 47.1650, "Şəki", "Azərbaycan"),
        LocationData(40.8130, 46.0390, "Şəmkir", "Azərbaycan"),
        LocationData(39.9200, 48.9170, "Şirvan", "Azərbaycan"),
        LocationData(39.7540, 46.6590, "Şuşa", "Azərbaycan"),
        LocationData(40.3540, 46.9850, "Tərtər", "Azərbaycan"),
        LocationData(40.8150, 45.6270, "Tovuz", "Azərbaycan"),
        LocationData(40.4600, 47.7060, "Ucar", "Azərbaycan"),
        LocationData(41.5410, 48.7510, "Xaçmaz", "Azərbaycan"),
        LocationData(39.8080, 46.7440, "Xankəndi", "Azərbaycan"),
        LocationData(40.8410, 49.1300, "Xızı", "Azərbaycan"),
        LocationData(39.8640, 46.7790, "Xocalı", "Azərbaycan"),
        LocationData(39.6470, 46.9870, "Xocavənd", "Azərbaycan"),
        LocationData(38.9230, 48.2830, "Yardımlı", "Azərbaycan"),
        LocationData(40.6370, 47.1050, "Yevlax", "Azərbaycan"),
        LocationData(41.5640, 46.6030, "Zaqatala", "Azərbaycan"),
        LocationData(39.1100, 46.6390, "Zəngilan", "Azərbaycan"),
        LocationData(40.2130, 47.6840, "Zərdab", "Azərbaycan")
    )

    val iranCities = listOf(
        LocationData(34.6416, 50.8746, "Qum", "İran", "Asia/Tehran"),
        LocationData(36.2974, 59.6059, "Məşhəd", "İran", "Asia/Tehran"),
        LocationData(35.6892, 51.3890, "Tehran", "İran", "Asia/Tehran"),
        LocationData(32.6546, 51.6680, "İsfahan", "İran", "Asia/Tehran"),
        LocationData(29.6036, 52.5388, "Şiraz", "İran", "Asia/Tehran"),
        LocationData(38.0792, 46.2978, "Təbriz", "İran", "Asia/Tehran")
    )

    val iraqCities = listOf(
        LocationData(33.3152, 44.3661, "Bağdad", "İraq", "Asia/Baghdad"),
        LocationData(32.0218, 44.3452, "Nəcəf", "İraq", "Asia/Baghdad"),
        LocationData(32.6160, 44.0246, "Kərbəla", "İraq", "Asia/Baghdad"),
        LocationData(34.3467, 44.3959, "Səmərrə", "İraq", "Asia/Baghdad"),
        LocationData(33.7737, 44.0260, "Kəziməyn", "İraq", "Asia/Baghdad")
    )

    val lebanonCities = listOf(
        LocationData(33.8886, 35.4955, "Beyrut", "Livan", "Asia/Beirut"),
        LocationData(33.5500, 36.4000, "Bəəlbək", "Livan", "Asia/Beirut")
    )

    val saudiCities = listOf(
        LocationData(21.4225, 39.8262, "Məkkə", "Səudiyyə", "Asia/Riyadh"),
        LocationData(24.4672, 39.6122, "Mədinə", "Səudiyyə", "Asia/Riyadh")
    )

    val allCities: List<LocationData> =
        azerbaijanCities + iranCities + iraqCities + lebanonCities + saudiCities

    val categories = linkedMapOf(
        "🇦🇿 Azərbaycan (şəhər və rayonlar)" to azerbaijanCities,
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
