package com.runanywhere.startup_hackathon20.presentation.wearables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class WearableDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val batteryLevel: Int,
    val isConnected: Boolean,
    val lastSync: Long,
    val signalStrength: Int,
    val firmware: String,
    val features: List<String>
)

enum class DeviceType(val displayName: String, val icon: String) {
    SMART_COLLAR("Smart Collar", "🦮"),
    FITNESS_TRACKER("Fitness Tracker", "⌚"),
    GPS_TAG("GPS Tracker", "📍"),
    HEALTH_MONITOR("Health Monitor", "❤️"),
    CAMERA_COLLAR("Camera Collar", "📷"),
    FEEDING_STATION("Smart Feeder", "🍽️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableDevicesScreen(
    viewModel: WearableDevicesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00BCD4).copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
    ) {
        // Top App Bar
        LargeTopAppBar(
            title = {
                Text(
                    text = "📱 Smart Devices",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        scope.launch {
                            viewModel.scanForDevices()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan for devices"
                    )
                }
            },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Connection Status Overview
            item {
                ConnectionStatusCard(
                    connectedDevices = uiState.connectedDevices,
                    totalDevices = uiState.allDevices.size
                )
            }

            // Add New Device Section
            item {
                AddDeviceCard(
                    isScanning = uiState.isScanning,
                    onAddDevice = { deviceType ->
                        scope.launch {
                            viewModel.addMockDevice(deviceType)
                        }
                    }
                )
            }

            // Connected Devices
            if (uiState.connectedDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "🟢 Connected Devices",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.connectedDevices) { device ->
                    DeviceCard(
                        device = device,
                        onToggleConnection = { viewModel.toggleDeviceConnection(device.id) },
                        onDeviceSettings = { /* Navigate to device settings */ }
                    )
                }
            }

            // Available Devices
            if (uiState.availableDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "🔍 Available Devices",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.availableDevices) { device ->
                    DeviceCard(
                        device = device,
                        onToggleConnection = { viewModel.toggleDeviceConnection(device.id) },
                        onDeviceSettings = { /* Navigate to device settings */ }
                    )
                }
            }

            // Real-time Data Stream
            if (uiState.connectedDevices.isNotEmpty()) {
                item {
                    RealTimeDataCard(
                        isStreaming = uiState.isStreamingData,
                        onToggleStreaming = viewModel::toggleDataStreaming
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    connectedDevices: List<WearableDevice>,
    totalDevices: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Connection Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (connectedDevices.isNotEmpty())
                                Color(0xFF4CAF50)
                            else
                                Color(0xFFFF5722)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (connectedDevices.isNotEmpty()) "🟢" else "🔴",
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (connectedDevices.isNotEmpty()) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Device Count
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${connectedDevices.size}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4)
                )
                Text(
                    text = "of $totalDevices devices",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Signal Strength
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SignalStrengthIndicator(
                    strength = connectedDevices.maxOfOrNull { it.signalStrength } ?: 0
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Signal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddDeviceCard(
    isScanning: Boolean,
    onAddDevice: (DeviceType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📲 Add New Device",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF00BCD4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Device Type Selection
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DeviceType.values()) { deviceType ->
                    OutlinedCard(
                        onClick = { onAddDevice(deviceType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = deviceType.icon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = deviceType.displayName,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: WearableDevice,
    onToggleConnection: () -> Unit,
    onDeviceSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isConnected)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Device Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = device.type.icon,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = device.type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Switch(
                    checked = device.isConnected,
                    onCheckedChange = { onToggleConnection() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Device Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DeviceStatItem(
                    icon = "🔋",
                    label = "Battery",
                    value = "${device.batteryLevel}%",
                    color = when {
                        device.batteryLevel > 50 -> Color(0xFF4CAF50)
                        device.batteryLevel > 20 -> Color(0xFFFF9800)
                        else -> Color(0xFFFF5722)
                    }
                )

                DeviceStatItem(
                    icon = "📶",
                    label = "Signal",
                    value = when {
                        device.signalStrength > 80 -> "Excellent"
                        device.signalStrength > 60 -> "Good"
                        device.signalStrength > 40 -> "Fair"
                        else -> "Poor"
                    },
                    color = when {
                        device.signalStrength > 60 -> Color(0xFF4CAF50)
                        device.signalStrength > 40 -> Color(0xFFFF9800)
                        else -> Color(0xFFFF5722)
                    }
                )

                DeviceStatItem(
                    icon = "⏱️",
                    label = "Last Sync",
                    value = formatLastSync(device.lastSync),
                    color = Color(0xFF00BCD4)
                )
            }

            // Features
            if (device.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(device.features) { feature ->
                        AssistChip(
                            onClick = { },
                            label = { Text(feature) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f),
                                labelColor = Color(0xFF00BCD4)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceStatItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun SignalStrengthIndicator(strength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(4) { index ->
            val isActive = strength > (index * 25)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((8 + index * 4).dp)
                    .background(
                        color = if (isActive) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun RealTimeDataCard(
    isStreaming: Boolean,
    onToggleStreaming: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isStreaming)
                Color(0xFF00BCD4).copy(alpha = 0.1f)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📡 Real-time Data Stream",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isStreaming) "Live data streaming active" else "Data streaming paused",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isStreaming) Color(0xFF4CAF50) else Color.Gray
                    )
                }

                Button(
                    onClick = onToggleStreaming,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStreaming) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = if (isStreaming) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isStreaming) "Stop streaming" else "Start streaming"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isStreaming) "Stop" else "Start")
                }
            }

            if (isStreaming) {
                Spacer(modifier = Modifier.height(16.dp))

                // Live Data Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LiveDataIndicator("❤️", "Heart Rate", "85 BPM")
                    LiveDataIndicator("🏃", "Activity", "Active")
                    LiveDataIndicator("📍", "Location", "Home")
                    LiveDataIndicator("🌡️", "Temp", "38.2°C")
                }
            }
        }
    }
}

@Composable
fun LiveDataIndicator(
    icon: String,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

private fun formatLastSync(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
    }
}