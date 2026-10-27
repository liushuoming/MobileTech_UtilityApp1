package au.edu.jcu.assessment.utilityapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import au.edu.jcu.assessment.utilityapp.model.AirQualityReport
import au.edu.jcu.assessment.utilityapp.model.AqiCategory
import au.edu.jcu.assessment.utilityapp.model.AqiScale
import au.edu.jcu.assessment.utilityapp.model.Location
import au.edu.jcu.assessment.utilityapp.ui.AirQualityUiState
import au.edu.jcu.assessment.utilityapp.ui.SettingsState
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import java.util.Locale

/** Main screen: shows the air quality for the chosen location at a glance. */
@Composable
fun UtilityScreen(
    uiState: AirQualityUiState,
    settings: SettingsState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is AirQualityUiState.Loading -> LoadingContent(modifier)
        is AirQualityUiState.Error -> ErrorContent(uiState.message, onRefresh, modifier)
        is AirQualityUiState.Success -> ReportContent(uiState.report, settings, onRefresh, modifier)
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.size(16.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun ReportContent(
    report: AirQualityReport,
    settings: SettingsState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LocationHeader(report.location, report.updatedAt)

        AqiCard(
            aqi = report.aqiFor(settings.scale),
            scale = settings.scale,
            showAdvice = settings.showAdvice
        )

        PollutantList(report, settings)

        FilledTonalButton(onClick = onRefresh, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Refresh")
        }
    }
}

@Composable
private fun LocationHeader(location: Location, updatedAt: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(location.displayName, style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "Updated $updatedAt",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** The large coloured card: the "at a glance" part of the app. */
@Composable
private fun AqiCard(aqi: Int?, scale: AqiScale, showAdvice: Boolean) {
    val category = aqi?.let { AqiCategory.from(it, scale) }
    val background = category?.color ?: MaterialTheme.colorScheme.surfaceVariant
    // Dark text on light colours (e.g. yellow), white text on dark ones.
    val content = if (background.luminance() > 0.5f) Color.Black else Color.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background, contentColor = content)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(scale.label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = aqi?.toString() ?: "–",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = category?.label ?: "No data",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            if (showAdvice && category != null) {
                Spacer(Modifier.size(8.dp))
                Text(category.advice, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun PollutantList(report: AirQualityReport, settings: SettingsState) {
    // Only the pollutants switched on in settings are listed.
    val rows = buildList {
        if (settings.showPm25) add("PM2.5 (fine particles)" to report.pm25)
        if (settings.showPm10) add("PM10 (coarse particles)" to report.pm10)
        if (settings.showOzone) add("Ozone (O₃)" to report.ozone)
        if (settings.showNitrogenDioxide) add("Nitrogen dioxide (NO₂)" to report.nitrogenDioxide)
    }
    if (rows.isEmpty()) return

    OutlinedCard(Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, (name, value) ->
            if (index > 0) HorizontalDivider()
            PollutantRow(name, value)
        }
    }
}

@Composable
private fun PollutantRow(name: String, value: Double?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value?.let { String.format(Locale.getDefault(), "%.1f µg/m³", it) } ?: "–",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---- Previews use sample data, so they work without the network ----

private val sampleReport = AirQualityReport(
    location = Location.Default,
    updatedAt = "2:00 PM, 27 Oct",
    usAqi = 42,
    europeanAqi = 18,
    pm25 = 6.3,
    pm10 = 14.8,
    ozone = 61.0,
    nitrogenDioxide = 3.2
)

@Preview(showBackground = true)
@Composable
fun UtilityScreenPreview() {
    MobileTech_UtilityAppTheme {
        UtilityScreen(AirQualityUiState.Success(sampleReport), SettingsState(), onRefresh = {})
    }
}

@Preview(showBackground = true)
@Composable
fun UtilityScreenModeratePreview() {
    MobileTech_UtilityAppTheme {
        UtilityScreen(
            AirQualityUiState.Success(sampleReport.copy(usAqi = 87)),
            SettingsState(showNitrogenDioxide = true),
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UtilityScreenErrorPreview() {
    MobileTech_UtilityAppTheme {
        UtilityScreen(AirQualityUiState.Error("No internet connection."), SettingsState(), onRefresh = {})
    }
}
