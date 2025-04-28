package com.example.check

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var wifiManager: WifiManager
    private val locationData = mutableStateMapOf<String, LocationData>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions needed to scan WiFi", Toast.LENGTH_LONG).show()
        }
    }

    // Receiver for WiFi scan results
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                // Handle scan results here if needed
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Register for WiFi scan results
        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        // Initialize with 3 sample locations
        locationData["Living Room"] = generateRandomLocationData("Living Room")
        locationData["Kitchen"] = generateRandomLocationData("Kitchen")
        locationData["Bedroom"] = generateRandomLocationData("Bedroom")

        setContent {
            WifiSignalAnalyzerTheme {
                WifiSignalAnalyzerApp(
                    locationData = locationData,
                    onScanLocation = { locationName ->
                        scanWifiForLocation(locationName)
                    },
                    onAddLocation = { locationName ->
                        if (locationName.isNotBlank() && !locationData.containsKey(locationName)) {
                            scanWifiForLocation(locationName)
                        } else {
                            Toast.makeText(
                                this,
                                "Please enter a unique location name",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onRemoveLocation = { locationName ->
                        locationData.remove(locationName)
                    }
                )
            }
        }

        // Request needed permissions
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
        }
        requestPermissionLauncher.launch(permissionsToRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver when activity is destroyed
        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            // Ignore if receiver wasn't registered
        }
    }

    private fun generateRandomLocationData(locationName: String): LocationData {
        val apName = "AP_${Random.nextInt(1, 10)}"
        val bssid = String.format(
            "%02X:%02X:%02X:%02X:%02X:%02X",
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256),
            Random.nextInt(256)
        )

        // Signal strength usually ranges from -30 (very strong) to -90 (very weak)
        val baseSignalStrength = -60 - Random.nextInt(0, 30)
        val signalStrengths = List(100) {
            baseSignalStrength + Random.nextInt(-15, 15)
        }

        return LocationData(
            locationName = locationName,
            apName = apName,
            bssid = bssid,
            signalStrengths = signalStrengths
        )
    }

    private fun scanWifiForLocation(locationName: String) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Use the newer approach for scanning WiFi
            val success = wifiManager.scanResults?.isNotEmpty() ?: false
            if (!success) {
                // If we already have scan results, use them
                // Otherwise, request a new scan (this is now handled via BroadcastReceiver)
                if (!wifiManager.startScan()) {  // While deprecated, we still use it but handle results via receiver
                    Toast.makeText(
                        this,
                        "Failed to start WiFi scan, using fallback data",
                        Toast.LENGTH_LONG
                    ).show()
                    locationData[locationName] = generateRandomLocationData(locationName)
                    return
                }
            }

            val scanResults = wifiManager.scanResults
            if (scanResults.isEmpty()) {
                Toast.makeText(
                    this,
                    "No WiFi APs found, please check your WiFi is enabled",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // Find the strongest AP
            val strongestAP = scanResults.maxByOrNull { it.level } ?: return

            // Start collecting 100 readings
            Thread {
                try {
                    val readings = mutableListOf<Int>()
                    val startTime = System.currentTimeMillis()
                    val targetBSSID = strongestAP.BSSID

                    while (readings.size < 100) {
                        // Request scan (handled by receiver)
                        wifiManager.startScan()
                        Thread.sleep(100) // Short delay between scans

                        val results = wifiManager.scanResults
                        val ap = results.find { it.BSSID == targetBSSID }

                        if (ap != null) {
                            readings.add(ap.level)
                        } else {
                            // If we can't find the same AP, use the previous reading or a default
                            readings.add(readings.lastOrNull() ?: -70)
                        }

                        // Safety timeout after 30 seconds
                        if (System.currentTimeMillis() - startTime > 30000) break
                    }

                    // Pad to 100 if needed
                    while (readings.size < 100) {
                        readings.add(readings.lastOrNull() ?: -70)
                    }

                    // Trim to exactly 100
                    val finalReadings = readings.take(100)

                    runOnUiThread {
                        // Use the non-deprecated way to get SSID if possible
                        val apName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // For Android 13+ use wifiSsid
                            val wifiSsid = strongestAP.wifiSsid
                            if (wifiSsid != null && wifiSsid.toString().isNotEmpty()) {
                                wifiSsid.toString().removeSurrounding("\"")
                            } else {
                                "Unknown AP"
                            }
                        } else {
                            // Legacy approach for older versions
                            if (strongestAP.SSID.isNotEmpty()) strongestAP.SSID.removeSurrounding("\"") else "Unknown AP"
                        }

                        locationData[locationName] = LocationData(
                            locationName = locationName,
                            apName = apName,
                            bssid = strongestAP.BSSID,
                            signalStrengths = finalReadings
                        )
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error collecting WiFi data: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()

            // For immediate feedback, generate random data first
            locationData[locationName] = generateRandomLocationData(locationName)
            Toast.makeText(
                this,
                "Started WiFi scanning for $locationName...",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error scanning WiFi: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

data class LocationData(
    val locationName: String,
    val apName: String,
    val bssid: String,
    val signalStrengths: List<Int>
)

@Composable
fun WifiSignalAnalyzerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiSignalAnalyzerApp(
    locationData: Map<String, LocationData>,
    onScanLocation: (String) -> Unit,
    onAddLocation: (String) -> Unit,
    onRemoveLocation: (String) -> Unit
) {
    var selectedLocation by remember { mutableStateOf<String?>(locationData.keys.firstOrNull()) }
    var newLocationName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Signal Strength Analyzer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Location selector
            Text(
                text = "Select a Location:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                items(locationData.keys.toList()) { location ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLocation == location,
                            onClick = { selectedLocation = location }
                        )
                        Text(
                            text = location,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    onScanLocation(location)
                                }
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text("Rescan")
                        }
                        Button(
                            onClick = { onRemoveLocation(location) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }

            // Add new location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newLocationName,
                    onValueChange = { newLocationName = it },
                    label = { Text("New Location Name") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newLocationName.isNotBlank()) {
                            onAddLocation(newLocationName)
                            selectedLocation = newLocationName
                            newLocationName = ""
                        }
                    }
                ) {
                    Text("Add Location")
                }
            }

            // Signal strength visualization
            selectedLocation?.let { location ->
                locationData[location]?.let { data ->
                    LocationDataDisplay(data)
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Select a location to view WiFi signal data",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun LocationDataDisplay(data: LocationData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Location: ${data.locationName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Access Point: ${data.apName}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "BSSID: ${data.bssid}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SignalStrengthGraph(
            signalStrengths = data.signalStrengths,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top = 16.dp)
        )

        // Signal strength statistics
        val minSignal = data.signalStrengths.minOrNull() ?: 0
        val maxSignal = data.signalStrengths.maxOrNull() ?: 0
        val avgSignal = data.signalStrengths.average().toInt()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SignalStatCard("Min", minSignal)
            SignalStatCard("Avg", avgSignal)
            SignalStatCard("Max", maxSignal)
        }
    }
}

@Composable
fun SignalStatCard(label: String, value: Int) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$value dBm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Simple color indicator based on signal strength
            val color = when {
                value > -60 -> Color.Green
                value > -70 -> Color(0xFF8BC34A)  // Light Green
                value > -80 -> Color(0xFFFFC107)  // Amber
                else -> Color.Red
            }

            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(16.dp)
                    .background(color = color, shape = MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
fun SignalStrengthGraph(
    signalStrengths: List<Int>,
    modifier: Modifier = Modifier
) {
    if (signalStrengths.isEmpty()) return

    val minSignal = -100 // Typical minimum WiFi signal
    val maxSignal = -30  // Typical maximum WiFi signal

    Card(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val xStep = width / (signalStrengths.size - 1)

                // Draw axes
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, height),
                    end = Offset(width, height),
                    strokeWidth = 2f
                )

                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, height),
                    strokeWidth = 2f
                )

                // Draw horizontal gridlines and labels
                val gridLines = 5
                for (i in 0..gridLines) {
                    val y = height - (height * i) / gridLines

                    // Grid line
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )

                    // Label
                    val signalValue = minSignal + ((maxSignal - minSignal) * i) / gridLines
                    drawContext.canvas.nativeCanvas.drawText(
                        "$signalValue dBm",
                        10f,
                        y - 10,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                        }
                    )
                }

                // Draw line graph
                val path = Path()

                signalStrengths.forEachIndexed { index, signal ->
                    val normalizedSignal =
                        (signal - minSignal).toFloat() / (maxSignal - minSignal)
                    val x = index * xStep
                    val y = height - (normalizedSignal * height).coerceIn(0f, height)

                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Draw point
                    drawCircle(
                        color = Color.Blue,
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }

                // Draw path
                drawPath(
                    path = path,
                    color = Color.Blue,
                    style = Stroke(width = 3f)
                )

                // X axis labels (only a few points to avoid crowding)
                val labelPoints = listOf(0, 25, 50, 75, 99)
                labelPoints.forEach { index ->
                    if (index < signalStrengths.size) {
                        val x = index * xStep
                        drawContext.canvas.nativeCanvas.drawText(
                            index.toString(),
                            x,
                            height + 40,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }

            Text(
                text = "Signal Strength Over Time (100 samples)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }
    }
}