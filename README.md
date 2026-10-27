# Air Check – Air Quality Utility App

CP3406 / CP5307 Assessment 1: Utility App

Air Check shows the air quality where you are at a glance: one big colour-coded number,
what it means for your health, and the main pollutant readings. You can check it before
going for a run, walking to class or opening the windows.

---

## Core features

| Feature | Where |
|---|---|
| Large, colour-coded AQI number with a health category (Good → Hazardous) | `UtilityScreen.kt` → `AqiCard` |
| Short health advice for the current category | `AqiCategory` in `model/AirQuality.kt` |
| Pollutant readings: PM2.5, PM10, ozone, nitrogen dioxide | `UtilityScreen.kt` → `PollutantList` |
| Local "updated at" time for the location | `NetworkAirQualityRepository.formatTime` |
| Refresh button, loading spinner and error screen with retry | `UtilityScreen.kt`, `AirQualityUiState` |
| **Settings:** quick-pick cities or search any city by name | `SettingsScreen.kt`, Open-Meteo Geocoding API |
| **Settings:** switch between US AQI and European AQI | `AqiScale` |
| **Settings:** choose which pollutants and whether advice is shown | `SettingsState` |

Settings are kept in memory only, as the assessment allows.

---

## Architecture

```
UI (Compose)              ViewModel                  Data                       Network
UtilityScreen  ─┐                                                                 
                ├─►  AirQualityViewModel  ─►  AirQualityRepository  ─►  AirQualityApiService (Retrofit)
SettingsScreen ─┘    (StateFlow: uiState,     (NetworkAirQuality-      GeocodingApiService  (Retrofit)
                      settings)                Repository)
                                   ▲
                    AppContainer (manual dependency injection), created in UtilityApplication
```

- **ViewModel** (`ui/AirQualityViewModel.kt`) holds the screen state as `StateFlow`s and survives
  configuration changes such as rotation. Both tabs share the same ViewModel, so changing a setting
  updates the main screen immediately.
- **Repository pattern** (`data/AirQualityRepository.kt`): the ViewModel depends on the
  `AirQualityRepository` interface, not on Retrofit, and network DTOs are converted to the app's own
  `AirQualityReport` model.
- **Dependency injection** (`data/AppContainer.kt`, `UtilityApplication.kt`): one container builds
  the Retrofit services and repository, and `AirQualityViewModel.Factory` injects the repository.
- **Networking** (`network/OpenMeteoApi.kt`): Retrofit with `suspend` functions and the
  kotlinx.serialization converter. Errors (no internet, HTTP errors, bad data) are turned into a
  friendly error state instead of crashing.
- **UI** (`ui/screens/`): stateless composables that receive state and callbacks (state hoisting),
  built with Material 3 components (`Card`, `NavigationBar`, `Switch`, `RadioButton`,
  `OutlinedTextField`). Each screen has `@Preview`s with sample data.

### Project structure

```
app/src/main/java/au/edu/jcu/assessment/utilityapp/
├── MainActivity.kt            # Scaffold + bottom navigation (from starter template)
├── UtilityApplication.kt      # Creates the AppContainer
├── data/
│   ├── AppContainer.kt        # Dependency injection
│   └── AirQualityRepository.kt
├── model/AirQuality.kt        # Location, AirQualityReport, AqiScale, AqiCategory
├── network/OpenMeteoApi.kt    # Retrofit services + JSON DTOs
└── ui/
    ├── AirQualityViewModel.kt
    ├── screens/UtilityScreen.kt
    ├── screens/SettingsScreen.kt
    └── theme/
```

---

## APIs used

Both are free and need no API key.

- [Open-Meteo Air Quality API](https://open-meteo.com/en/docs/air-quality-api): `GET /v1/air-quality?latitude=&longitude=&current=us_aqi,european_aqi,pm2_5,pm10,ozone,nitrogen_dioxide&timezone=auto`
- [Open-Meteo Geocoding API](https://open-meteo.com/en/docs/geocoding-api): `GET /v1/search?name=&count=1`

AQI bands follow the US EPA AQI and the European Environment Agency's European AQI.

---

## How to run

1. Clone the repository and open it in Android Studio.
2. Let Gradle sync.
3. Run on an emulator or device (API 26+) with internet access.

Unit tests for the AQI bands: `app/src/test/.../AqiCategoryTest.kt`.

---

## Libraries

Jetpack Compose + Material 3, AndroidX Lifecycle ViewModel (Compose), Retrofit 2,
OkHttp, kotlinx.serialization.
