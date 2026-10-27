package au.edu.jcu.assessment.utilityapp.data

import au.edu.jcu.assessment.utilityapp.network.AirQualityApiService
import au.edu.jcu.assessment.utilityapp.network.GeocodingApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Manual dependency injection container, shared app-wide. */
interface AppContainer {
    val airQualityRepository: AirQualityRepository
}

class DefaultAppContainer : AppContainer {

    // Ignore JSON fields we don't model (the API returns many extras).
    private val json = Json { ignoreUnknownKeys = true }

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val airQualityApi: AirQualityApiService by lazy {
        retrofit(AirQualityApiService.BASE_URL).create(AirQualityApiService::class.java)
    }

    private val geocodingApi: GeocodingApiService by lazy {
        retrofit(GeocodingApiService.BASE_URL).create(GeocodingApiService::class.java)
    }

    override val airQualityRepository: AirQualityRepository by lazy {
        NetworkAirQualityRepository(airQualityApi, geocodingApi)
    }
}
