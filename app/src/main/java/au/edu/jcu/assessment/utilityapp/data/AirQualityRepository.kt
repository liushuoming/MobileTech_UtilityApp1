package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.model.AirQualityReport
import au.edu.jcu.assessment.utilityapp.model.Location
import au.edu.jcu.assessment.utilityapp.network.AirQualityApiService
import au.edu.jcu.assessment.utilityapp.network.GeocodingApiService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** Single source of air quality data for the rest of the app. */
interface AirQualityRepository {
    /** Returns the current air quality for [location]. */
    suspend fun getAirQuality(location: Location): AirQualityReport

    /** Looks up a place by name. Returns null when nothing matches. */
    suspend fun findLocation(query: String): Location?
}

/** Repository implementation backed by the Open-Meteo web APIs. */
class NetworkAirQualityRepository(
    private val airQualityApi: AirQualityApiService,
    private val geocodingApi: GeocodingApiService
) : AirQualityRepository {

    override suspend fun getAirQuality(location: Location): AirQualityReport {
        val current = airQualityApi
            .getCurrentAirQuality(location.latitude, location.longitude)
            .current

        // Converts the API's DTO into the app's own model.
        return AirQualityReport(
            location = location,
            updatedAt = formatTime(current.time),
            usAqi = current.usAqi?.roundToInt(),
            europeanAqi = current.europeanAqi?.roundToInt(),
            pm25 = current.pm25,
            pm10 = current.pm10,
            ozone = current.ozone,
            nitrogenDioxide = current.nitrogenDioxide
        )
    }

    override suspend fun findLocation(query: String): Location? {
        val result = geocodingApi.searchLocation(query.trim()).results.firstOrNull()
            ?: return null
        return Location(result.name, result.country, result.latitude, result.longitude)
    }

    /** "2026-10-27T14:00" (local time at the location) -> "2:00 PM, 27 Oct". */
    private fun formatTime(isoTime: String): String = try {
        LocalDateTime.parse(isoTime)
            .format(DateTimeFormatter.ofPattern("h:mm a, d MMM", Locale.getDefault()))
    } catch (e: Exception) {
        isoTime
    }
}
