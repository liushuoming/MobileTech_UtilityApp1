package au.edu.jcu.assessment.utilityapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import au.edu.jcu.assessment.utilityapp.UtilityApplication
import au.edu.jcu.assessment.utilityapp.data.AirQualityRepository
import au.edu.jcu.assessment.utilityapp.model.AirQualityReport
import au.edu.jcu.assessment.utilityapp.model.AqiScale
import au.edu.jcu.assessment.utilityapp.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** What the main screen is currently showing. */
sealed interface AirQualityUiState {
    data object Loading : AirQualityUiState
    data class Success(val report: AirQualityReport) : AirQualityUiState
    data class Error(val message: String) : AirQualityUiState
}

/** User preferences from the settings screen (kept in memory only). */
data class SettingsState(
    val location: Location = Location.Default,
    val scale: AqiScale = AqiScale.US,
    val showAdvice: Boolean = true,
    val showPm25: Boolean = true,
    val showPm10: Boolean = true,
    val showOzone: Boolean = true,
    val showNitrogenDioxide: Boolean = false,
    val isSearching: Boolean = false,
    val searchMessage: String? = null
)

/** The pollutant rows the user can show or hide. */
enum class Pollutant { PM25, PM10, OZONE, NITROGEN_DIOXIDE }

class AirQualityViewModel(
    private val repository: AirQualityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AirQualityUiState>(AirQualityUiState.Loading)
    val uiState: StateFlow<AirQualityUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    init {
        refresh()
    }

    /** Fetches fresh readings for the current location. */
    fun refresh() {
        val location = _settings.value.location
        viewModelScope.launch {
            _uiState.value = AirQualityUiState.Loading
            _uiState.value = try {
                AirQualityUiState.Success(repository.getAirQuality(location))
            } catch (e: IOException) {
                AirQualityUiState.Error("No internet connection. Check your network and try again.")
            } catch (e: HttpException) {
                AirQualityUiState.Error("The air quality service is unavailable (error ${e.code()}).")
            } catch (e: SerializationException) {
                AirQualityUiState.Error("Received unexpected data from the air quality service.")
            }
        }
    }

    fun selectLocation(location: Location) {
        _settings.update { it.copy(location = location, searchMessage = null) }
        refresh()
    }

    /** Looks up a city typed by the user and switches to it if found. */
    fun searchLocation(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _settings.update { it.copy(isSearching = true, searchMessage = null) }
            try {
                val found = repository.findLocation(query)
                if (found == null) {
                    _settings.update { it.copy(searchMessage = "No place called \"$query\" was found.") }
                } else {
                    selectLocation(found)
                    _settings.update { it.copy(searchMessage = "Showing ${found.displayName}.") }
                }
            } catch (e: IOException) {
                _settings.update { it.copy(searchMessage = "Search failed: no internet connection.") }
            } catch (e: HttpException) {
                _settings.update { it.copy(searchMessage = "Search failed (error ${e.code()}).") }
            } finally {
                _settings.update { it.copy(isSearching = false) }
            }
        }
    }

    fun setScale(scale: AqiScale) = _settings.update { it.copy(scale = scale) }

    fun setShowAdvice(show: Boolean) = _settings.update { it.copy(showAdvice = show) }

    fun setPollutantVisible(pollutant: Pollutant, visible: Boolean) = _settings.update {
        when (pollutant) {
            Pollutant.PM25 -> it.copy(showPm25 = visible)
            Pollutant.PM10 -> it.copy(showPm10 = visible)
            Pollutant.OZONE -> it.copy(showOzone = visible)
            Pollutant.NITROGEN_DIOXIDE -> it.copy(showNitrogenDioxide = visible)
        }
    }

    companion object {
        /** Builds the ViewModel with the repository from the app's container. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as UtilityApplication
                AirQualityViewModel(application.container.airQualityRepository)
            }
        }
    }
}
