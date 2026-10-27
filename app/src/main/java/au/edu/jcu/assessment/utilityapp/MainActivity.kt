package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.ui.AirQualityViewModel
import au.edu.jcu.assessment.utilityapp.ui.screens.SettingsScreen
import au.edu.jcu.assessment.utilityapp.ui.screens.UtilityScreen
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileTech_UtilityAppTheme {
                UtilityApp()
            }
        }
    }
}

/** Two tabs from the starter template. Both screens share one ViewModel. */
@Composable
fun UtilityApp(
    viewModel: AirQualityViewModel = viewModel(factory = AirQualityViewModel.Factory)
) {
    var selectedTab by rememberSaveable { mutableStateOf("Utility") }
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Air quality") },
                    label = { Text("Air quality") },
                    selected = selectedTab == "Utility",
                    onClick = { selectedTab = "Utility" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "Utility" -> UtilityScreen(
                    uiState = uiState,
                    settings = settings,
                    onRefresh = viewModel::refresh
                )
                "Settings" -> SettingsScreen(
                    settings = settings,
                    onLocationSelected = viewModel::selectLocation,
                    onSearch = viewModel::searchLocation,
                    onScaleChanged = viewModel::setScale,
                    onShowAdviceChanged = viewModel::setShowAdvice,
                    onPollutantToggled = viewModel::setPollutantVisible
                )
            }
        }
    }
}
