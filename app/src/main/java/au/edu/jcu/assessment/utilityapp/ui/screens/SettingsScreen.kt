package au.edu.jcu.assessment.utilityapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.utilityapp.model.AqiScale
import au.edu.jcu.assessment.utilityapp.model.Location
import au.edu.jcu.assessment.utilityapp.ui.Pollutant
import au.edu.jcu.assessment.utilityapp.ui.SettingsState
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme

/** Settings screen: controls what the main screen shows. */
@Composable
fun SettingsScreen(
    settings: SettingsState,
    onLocationSelected: (Location) -> Unit,
    onSearch: (String) -> Unit,
    onScaleChanged: (AqiScale) -> Unit,
    onShowAdviceChanged: (Boolean) -> Unit,
    onPollutantToggled: (Pollutant, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SectionTitle("Location")
        Text("Current: ${settings.location.displayName}", style = MaterialTheme.typography.bodyMedium)
        PresetLocations(settings.location, onLocationSelected)
        LocationSearch(settings.isSearching, settings.searchMessage, onSearch)

        HorizontalDivider()
        SectionTitle("AQI standard")
        AqiScale.entries.forEach { scale ->
            RadioRow(
                label = scale.label,
                selected = settings.scale == scale,
                onClick = { onScaleChanged(scale) }
            )
        }

        HorizontalDivider()
        SectionTitle("Show on main screen")
        SwitchRow("Health advice", settings.showAdvice, onShowAdviceChanged)
        SwitchRow("PM2.5", settings.showPm25) { onPollutantToggled(Pollutant.PM25, it) }
        SwitchRow("PM10", settings.showPm10) { onPollutantToggled(Pollutant.PM10, it) }
        SwitchRow("Ozone", settings.showOzone) { onPollutantToggled(Pollutant.OZONE, it) }
        SwitchRow("Nitrogen dioxide", settings.showNitrogenDioxide) {
            onPollutantToggled(Pollutant.NITROGEN_DIOXIDE, it)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun PresetLocations(current: Location, onLocationSelected: (Location) -> Unit) {
    // Two buttons per row keeps the presets readable on narrow phones.
    Location.Presets.chunked(2).forEach { pair ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pair.forEach { location ->
                val isCurrent = location.name == current.name
                val buttonModifier = Modifier.weight(1f)
                if (isCurrent) {
                    Button(onClick = {}, modifier = buttonModifier) { Text(location.name) }
                } else {
                    OutlinedButton(onClick = { onLocationSelected(location) }, modifier = buttonModifier) {
                        Text(location.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSearch(isSearching: Boolean, message: String?, onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search a city") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            modifier = Modifier.weight(1f)
        )
        if (isSearching) {
            CircularProgressIndicator(Modifier.size(32.dp))
        } else {
            Button(onClick = { onSearch(query) }, enabled = query.isNotBlank()) { Text("Go") }
        }
    }
    if (message != null) {
        Text(message, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MobileTech_UtilityAppTheme {
        SettingsScreen(
            settings = SettingsState(searchMessage = "Showing Townsville, Australia."),
            onLocationSelected = {},
            onSearch = {},
            onScaleChanged = {},
            onShowAdviceChanged = {},
            onPollutantToggled = { _, _ -> }
        )
    }
}
