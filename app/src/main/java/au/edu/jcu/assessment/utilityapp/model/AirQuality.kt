package au.edu.jcu.assessment.utilityapp.model

import androidx.compose.ui.graphics.Color

/** A place the user wants air quality for. */
data class Location(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = if (country.isBlank()) name else "$name, $country"

    companion object {
        /** Default location shown when the app first opens. */
        val Default = Location("Townsville", "Australia", -19.2664, 146.8057)

        /** Quick-pick locations offered on the settings screen. */
        val Presets = listOf(
            Default,
            Location("Cairns", "Australia", -16.9203, 145.7710),
            Location("Brisbane", "Australia", -27.4679, 153.0281),
            Location("Singapore", "Singapore", 1.2897, 103.8501)
        )
    }
}

/** Air quality readings for one location, as shown on the main screen. */
data class AirQualityReport(
    val location: Location,
    val updatedAt: String,
    val usAqi: Int?,
    val europeanAqi: Int?,
    val pm25: Double?,
    val pm10: Double?,
    val ozone: Double?,
    val nitrogenDioxide: Double?
) {
    /** Returns the AQI value for the scale the user picked in settings. */
    fun aqiFor(scale: AqiScale): Int? = when (scale) {
        AqiScale.US -> usAqi
        AqiScale.EUROPEAN -> europeanAqi
    }
}

/** The two AQI standards supported by the Open-Meteo API. */
enum class AqiScale(val label: String) {
    US("US AQI"),
    EUROPEAN("European AQI")
}

/**
 * Health category for an AQI value, with a colour and short advice.
 * Thresholds follow the US EPA and European Environment Agency bands.
 */
enum class AqiCategory(val label: String, val color: Color, val advice: String) {
    GOOD("Good", Color(0xFF4CAF50), "Air quality is great. Enjoy outdoor activities."),
    FAIR("Fair", Color(0xFF8BC34A), "Air quality is acceptable for everyone."),
    MODERATE("Moderate", Color(0xFFFFEB3B), "Unusually sensitive people should limit long outdoor exertion."),
    SENSITIVE("Unhealthy for sensitive groups", Color(0xFFFF9800), "Children, older adults and people with asthma should take it easy outside."),
    UNHEALTHY("Unhealthy", Color(0xFFF44336), "Everyone should reduce long or heavy outdoor exertion."),
    VERY_UNHEALTHY("Very unhealthy", Color(0xFF9C27B0), "Avoid outdoor exercise. Keep windows closed."),
    HAZARDOUS("Hazardous", Color(0xFF7E0023), "Stay indoors and avoid all outdoor activity.");

    companion object {
        fun from(aqi: Int, scale: AqiScale): AqiCategory = when (scale) {
            AqiScale.US -> when {
                aqi <= 50 -> GOOD
                aqi <= 100 -> MODERATE
                aqi <= 150 -> SENSITIVE
                aqi <= 200 -> UNHEALTHY
                aqi <= 300 -> VERY_UNHEALTHY
                else -> HAZARDOUS
            }
            AqiScale.EUROPEAN -> when {
                aqi <= 20 -> GOOD
                aqi <= 40 -> FAIR
                aqi <= 60 -> MODERATE
                aqi <= 80 -> UNHEALTHY
                aqi <= 100 -> VERY_UNHEALTHY
                else -> HAZARDOUS
            }
        }
    }
}
