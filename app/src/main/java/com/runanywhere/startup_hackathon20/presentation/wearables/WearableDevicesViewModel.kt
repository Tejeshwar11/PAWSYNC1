package com.runanywhere.startup_hackathon20.presentation.wearables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


data class WearableDevicesUiState(
    val isLoading: Boolean = false,
    val isScanning: Boolean = false,
    val connectedDevices: List<WearableDevice> = emptyList(),
    val availableDevices: List<WearableDevice> = emptyList(),
    val isStreamingData: Boolean = false,
    val error: String? = null
) {
    val allDevices: List<WearableDevice> = connectedDevices + availableDevices
}

class WearableDevicesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WearableDevicesUiState())
    val uiState: StateFlow<WearableDevicesUiState> = _uiState.asStateFlow()

    private val mockDevices = listOf(
        WearableDevice(
            id = "collar_001",
            name = "PawTracker Pro",
            type = DeviceType.SMART_COLLAR,
            batteryLevel = 78,
            isConnected = true,
            lastSync = System.currentTimeMillis() - 120000, // 2 minutes ago
            signalStrength = 85,
            firmware = "v2.1.3",
            features = listOf("GPS", "Activity", "Health", "Temperature")
        ),
        WearableDevice(
            id = "tracker_002",
            name = "FitPaw Tracker",
            type = DeviceType.FITNESS_TRACKER,
            batteryLevel = 92,
            isConnected = false,
            lastSync = 0, // Never synced
            signalStrength = 62,
            firmware = "v1.8.2",
            features = listOf("Activity", "Sleep", "Steps")
        ),
        WearableDevice(
            id = "gps_003",
            name = "SafePaw GPS",
            type = DeviceType.GPS_TAG,
            batteryLevel = 65,
            isConnected = false,
            lastSync = 0, // Never synced
            signalStrength = 58,
            firmware = "v3.0.1",
            features = listOf("GPS", "Geofencing", "Emergency")
        )
    )

    init {
        loadInitialDevices()
    }

    private fun loadInitialDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                connectedDevices = mockDevices.filter { it.isConnected },
                availableDevices = mockDevices.filter { !it.isConnected }
            )
        }
    }

    fun scanForDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)

            // Simulate scanning delay
            delay(3000)

            // Add some more mock devices
            val newDevices = listOf(
                WearableDevice(
                    id = "health_004",
                    name = "VitalPaw Monitor",
                    type = DeviceType.HEALTH_MONITOR,
                    batteryLevel = 88,
                    isConnected = false,
                    lastSync = 0,
                    signalStrength = 72,
                    firmware = "v1.5.0",
                    features = listOf("Heart Rate", "Temperature", "Activity")
                )
            )

            _uiState.value = _uiState.value.copy(
                isScanning = false,
                availableDevices = _uiState.value.availableDevices + newDevices
            )
        }
    }

    fun toggleDeviceConnection(deviceId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val device = currentState.allDevices.find { it.id == deviceId } ?: return@launch

            if (device.isConnected) {
                // Disconnect device
                val updatedDevice = device.copy(isConnected = false)
                _uiState.value = currentState.copy(
                    connectedDevices = currentState.connectedDevices.filter { it.id != deviceId },
                    availableDevices = currentState.availableDevices + updatedDevice
                )
            } else {
                // Connect device
                val updatedDevice = device.copy(
                    isConnected = true,
                    lastSync = System.currentTimeMillis()
                )
                _uiState.value = currentState.copy(
                    connectedDevices = currentState.connectedDevices + updatedDevice,
                    availableDevices = currentState.availableDevices.filter { it.id != deviceId }
                )
            }
        }
    }

    fun toggleDataStreaming() {
        _uiState.value = _uiState.value.copy(
            isStreamingData = !_uiState.value.isStreamingData
        )
    }

    fun addMockDevice(deviceType: DeviceType) {
        viewModelScope.launch {
            val mockDevice = WearableDevice(
                id = "mock_${System.currentTimeMillis()}",
                name = "Mock ${deviceType.name}",
                type = deviceType,
                batteryLevel = (50..100).random(),
                isConnected = false,
                lastSync = 0,
                signalStrength = (40..90).random(),
                firmware = "v1.0.0",
                features = when (deviceType) {
                    DeviceType.SMART_COLLAR -> listOf("GPS", "Activity", "Health")
                    DeviceType.FITNESS_TRACKER -> listOf("Steps", "Activity", "Sleep")
                    DeviceType.GPS_TAG -> listOf("GPS", "Location", "Tracking")
                    DeviceType.HEALTH_MONITOR -> listOf("Heart Rate", "Temperature", "Vitals")
                    DeviceType.CAMERA_COLLAR -> listOf("Camera", "Video", "Photos")
                    DeviceType.FEEDING_STATION -> listOf("Feeding", "Schedule", "Monitoring")
                }
            )

            _uiState.value = _uiState.value.copy(
                availableDevices = _uiState.value.availableDevices + mockDevice
            )
        }
    }
}