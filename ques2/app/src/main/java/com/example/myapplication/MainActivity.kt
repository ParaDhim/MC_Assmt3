// MainActivity.kt
package com.example.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.max

class MainActivity : ComponentActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var viewModelInstance: WifiViewModel

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                startWifiScan()
            } else {
                Toast.makeText(this, "Location permissions required to scan WiFi", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        viewModelInstance = WifiViewModel()

        setContent {
            MaterialTheme {
                val viewModel: WifiViewModel = remember { viewModelInstance }

                // This is the key change for handling notch/cutout areas
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Use the system window insets to ensure content doesn't overlap with notch
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        WifiSignalMapperApp(
                            viewModel = viewModel,
                            onScanRequest = { checkPermissionsAndScan() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(wifiScanReceiver)
    }

    private fun checkPermissionsAndScan() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startWifiScan()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun startWifiScan() {
        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        }
    }

    private fun scanSuccess() {
        val scanResults = wifiManager.scanResults
        viewModelInstance.updateScanResults(scanResults)
    }

    private fun scanFailure() {
        Toast.makeText(this, "WiFi scan failed", Toast.LENGTH_LONG).show()
    }
}

data class Location(
    val name: String,
    val signals: MutableList<Int> = MutableList(100) { -100 }, // Default weak signal
    val accessPoints: List<String> = emptyList()
)

class WifiViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    fun updateScanResults(scanResults: List<ScanResult>) {
        // Extract signal strengths
        val signals = scanResults.map { it.level }
        val accessPoints = scanResults.map { it.SSID }

        // Ensure we have exactly 100 values by padding or truncating
        val normalizedSignals = normalizeSignals(signals)

        _uiState.update { currentState ->
            currentState.copy(
                currentScanResults = normalizedSignals,
                currentAccessPoints = accessPoints
            )
        }
    }

    private fun normalizeSignals(signals: List<Int>): List<Int> {
        return when {
            signals.size >= 100 -> signals.take(100)
            else -> signals + List(100 - signals.size) { -100 }
        }
    }

    fun saveCurrentLocation(locationName: String) {
        val currentSignals = _uiState.value.currentScanResults
        val currentAPs = _uiState.value.currentAccessPoints

        if (currentSignals.isEmpty()) return

        val newLocation = Location(
            name = locationName,
            signals = currentSignals.take(100).toMutableList(),
            accessPoints = currentAPs
        )

        val updatedLocations = _uiState.value.savedLocations.toMutableList()

        // Check if location with same name exists
        val existingIndex = updatedLocations.indexOfFirst { it.name == locationName }
        if (existingIndex >= 0) {
            updatedLocations[existingIndex] = newLocation
        } else {
            updatedLocations.add(newLocation)
        }

        _uiState.update { it.copy(savedLocations = updatedLocations) }
    }

    fun selectLocation(location: Location?) {
        _uiState.update { it.copy(selectedLocation = location) }
    }
}

data class WifiUiState(
    val currentScanResults: List<Int> = emptyList(),
    val currentAccessPoints: List<String> = emptyList(),
    val savedLocations: List<Location> = listOf(
        // Pre-populated example locations with dummy data for demonstration
        Location(
            "Living Room",
            MutableList(100) { i -> -30 - (i % 20) },
            listOf("Home_WiFi", "Neighbor1", "Neighbor2")
        ),
        Location(
            "Bedroom",
            MutableList(100) { i -> -40 - (i % 15) },
            listOf("Home_WiFi", "Neighbor3")
        ),
        Location(
            "Kitchen",
            MutableList(100) { i -> -50 - (i % 10) },
            listOf("Home_WiFi", "GuestNetwork")
        )
    ),
    val selectedLocation: Location? = null,
    val isScanning: Boolean = false
)

@Composable
fun WifiSignalMapperApp(
    viewModel: WifiViewModel,
    onScanRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentLocationName by remember { mutableStateOf("") }
    var showComparison by remember { mutableStateOf(false) }

    // Use WindowInsets.safeDrawing to respect notch areas
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // Add system padding to handle notch/cutout areas
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // App Header
        Text(
            text = "Wi-Fi Signal Mapper",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Current scan controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentLocationName,
                onValueChange = { currentLocationName = it },
                label = { Text("Location Name") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onScanRequest,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Scan")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (currentLocationName.isNotBlank() && uiState.currentScanResults.isNotEmpty()) {
                        viewModel.saveCurrentLocation(currentLocationName)
                        currentLocationName = ""
                    }
                },
                enabled = currentLocationName.isNotBlank() && uiState.currentScanResults.isNotEmpty(),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Signal strength visualization for current scan
        if (uiState.currentScanResults.isNotEmpty()) {
            Text(
                text = "Current Scan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SignalMatrixVisualization(
                signals = uiState.currentScanResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            )

            Text(
                text = "Access Points: ${uiState.currentAccessPoints.take(5).joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Saved locations section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved Locations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Switch(
                checked = showComparison,
                onCheckedChange = { showComparison = it }
            )

            Text(
                text = "Compare",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showComparison && uiState.savedLocations.isNotEmpty()) {
            // Comparison View
            LocationComparisonView(locations = uiState.savedLocations)
        } else {
            // Individual locations list
            LazyColumn {
                items(uiState.savedLocations) { location ->
                    LocationCard(
                        location = location,
                        isSelected = location == uiState.selectedLocation,
                        onClick = { viewModel.selectLocation(location) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    location: Location,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            SignalMatrixVisualization(
                signals = location.signals,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Access Points: ${location.accessPoints.take(3).joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SignalStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SignalMatrixVisualization(
    signals: List<Int>,
    modifier: Modifier = Modifier
) {
    val normalizedSignals = remember(signals) {
        // Normalize signals to 0-100 range for visualization
        val minSignal = signals.minOrNull() ?: -100
        val maxSignal = signals.maxOrNull() ?: -30
        val range = max(1, abs(maxSignal - minSignal))

        signals.map { signal ->
            (((signal - minSignal).toFloat() / range) * 100).coerceIn(0f, 100f)
        }
    }

    Canvas(modifier = modifier) {
        val cellWidth = size.width / 10
        val cellHeight = size.height / 10

        normalizedSignals.take(100).forEachIndexed { index, strength ->
            val row = index / 10
            val col = index % 10

            val x = col * cellWidth
            val y = row * cellHeight

            // Map signal strength to color (red: weak, green: strong)
            val signalColor = Color(
                red = (1f - strength / 100f),
                green = (strength / 100f),
                blue = 0.1f,
                alpha = 0.7f
            )

            drawRect(
                color = signalColor,
                topLeft = Offset(x, y),
                size = Size(cellWidth, cellHeight)
            )

            // Draw cell border
            drawRect(
                color = Color.Gray.copy(alpha = 0.3f),
                topLeft = Offset(x, y),
                size = Size(cellWidth, cellHeight),
                style = Stroke(width = 0.5f)
            )
        }
    }
}

@Composable
fun LocationComparisonView(locations: List<Location>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Header for the comparison chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Location",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(100.dp)
            )
            Text(
                text = "Signal Distribution",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Stats",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Show each location in the comparison view
        locations.forEach { location ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(100.dp)
                )

                SignalMatrixVisualization(
                    signals = location.signals,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                )

                val avg = location.signals.average().toInt()
                Column(
                    modifier = Modifier.width(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$avg dBm",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${location.accessPoints.size} APs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Visualization legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red.copy(alpha = 0.7f))
            )
            Text(
                text = " Weak Signal ",
                style = MaterialTheme.typography.bodySmall
            )

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Green.copy(alpha = 0.7f))
            )
            Text(
                text = " Strong Signal",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}