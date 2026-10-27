package au.edu.jcu.assessment.utilityapp.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service for the Open-Meteo Air Quality API (no API key needed).
 * Docs: https://open-meteo.com/en/docs/air-quality-api
 */
interface AirQualityApiService {
    @GET("v1/air-quality")
    suspend fun getCurrentAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") fields: String = CURRENT_FIELDS,
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse

    companion object {
        const val BASE_URL = "https://air-quality-api.open-meteo.com/"
        const val CURRENT_FIELDS = "us_aqi,european_aqi,pm2_5,pm10,ozone,nitrogen_dioxide"
    }
}

/**
 * Retrofit service for the Open-Meteo Geocoding API, used to turn a
 * city name typed in settings into coordinates.
 * Docs: https://open-meteo.com/en/docs/geocoding-api
 */
interface GeocodingApiService {
    @GET("v1/search")
    suspend fun searchLocation(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "en"
    ): GeocodingResponse

    companion object {
        const val BASE_URL = "https://geocoding-api.open-meteo.com/"
    }
}

// ---- JSON data transfer objects (only the fields the app uses) ----

@Serializable
data class AirQualityResponse(
    val current: CurrentAirQuality
)

@Serializable
data class CurrentAirQuality(
    val time: String,
    // Parsed as Double so both "42" and "42.0" in the JSON work.
    @SerialName("us_aqi") val usAqi: Double? = null,
    @SerialName("european_aqi") val europeanAqi: Double? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    val pm10: Double? = null,
    val ozone: Double? = null,
    @SerialName("nitrogen_dioxide") val nitrogenDioxide: Double? = null
)

@Serializable
data class GeocodingResponse(
    // "results" is missing from the JSON when nothing matches.
    val results: List<GeocodingResult> = emptyList()
)

@Serializable
data class GeocodingResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = ""
)
