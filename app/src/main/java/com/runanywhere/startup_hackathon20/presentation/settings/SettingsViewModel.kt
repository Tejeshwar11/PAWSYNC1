package com.runanywhere.startup_hackathon20.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.PetProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentPet: PetProfile? = null,
    val totalPets: Int = 1,
    val healthAlertsEnabled: Boolean = true,
    val dailySummaryTime: String = "8:00 AM",
    val medicationRemindersEnabled: Boolean = true,
    val currentAIModel: String = "PetHealth-LLM-v2.1",
    val availableModels: List<AIModel> = emptyList(),
    val onDeviceProcessing: Boolean = true,
    val connectedDevices: List<ConnectedDevice> = emptyList(),
    val syncFrequency: String = "Every 15 minutes",
    val selectedTheme: AppTheme = AppTheme.AUTO,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val selectedUnits: AppUnits = AppUnits.METRIC,
    val communityFeaturesEnabled: Boolean = false,
    val publicProfileEnabled: Boolean = false,
    val appVersion: String = "1.0.0",
    val showDangerZone: Boolean = false
)

data class AIModel(
    val id: String,
    val name: String,
    val size: String,
    val isDownloaded: Boolean,
    val accuracy: Float
)

data class ConnectedDevice(
    val id: String,
    val name: String,
    val type: String,
    val batteryLevel: Int,
    val isConnected: Boolean
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load mock data
            val mockPet = PetProfile(
                id = "1",
                name = "Max",
                breed = "Golden Retriever",
                species = "Dog",
                age = 3,
                weight = 25.5f,
                gender = "Male",
                ownerName = "John Doe",
                ownerPhone = "+1-555-0123"
            )

            val mockModels = listOf(
                AIModel("1", "PetHealth-LLM-v2.1", "2.3 GB", true, 0.94f),
                AIModel("2", "VetAssist-Pro", "1.8 GB", false, 0.91f),
                AIModel("3", "PetBehavior-AI", "1.2 GB", false, 0.88f)
            )

            val mockDevices = listOf(
                ConnectedDevice("1", "Smart Collar Pro", "Collar", 85, true),
                ConnectedDevice("2", "Activity Tracker", "Tracker", 67, true)
            )

            _uiState.value = _uiState.value.copy(
                currentPet = mockPet,
                availableModels = mockModels,
                connectedDevices = mockDevices
            )
        }
    }

    // Pet Profile Management
    fun editCurrentPet() {
        // Navigate to pet profile editing
    }

    fun managePets() {
        // Navigate to pet management screen
    }

    // Health & Monitoring
    fun toggleHealthAlerts() {
        _uiState.value = _uiState.value.copy(
            healthAlertsEnabled = !_uiState.value.healthAlertsEnabled
        )
    }

    fun setDailySummaryTime() {
        // Open time picker dialog
    }

    fun toggleMedicationReminders() {
        _uiState.value = _uiState.value.copy(
            medicationRemindersEnabled = !_uiState.value.medicationRemindersEnabled
        )
    }

    // AI Settings
    fun selectAIModel() {
        // Open AI model selection dialog
    }

    fun downloadModels() {
        // Navigate to model download screen
    }

    fun toggleOnDeviceProcessing() {
        _uiState.value = _uiState.value.copy(
            onDeviceProcessing = !_uiState.value.onDeviceProcessing
        )
    }

    // Wearable Devices
    fun manageDevices() {
        // Navigate to device management screen
    }

    fun addNewDevice() {
        // Start device pairing process
    }

    fun setSyncFrequency() {
        // Open sync frequency selection
    }

    // App Preferences
    fun selectTheme() {
        // Cycle through themes or open selection dialog
        val currentTheme = _uiState.value.selectedTheme
        val nextTheme = when (currentTheme) {
            AppTheme.LIGHT -> AppTheme.DARK
            AppTheme.DARK -> AppTheme.AUTO
            AppTheme.AUTO -> AppTheme.LIGHT
        }
        _uiState.value = _uiState.value.copy(selectedTheme = nextTheme)
    }

    fun selectLanguage() {
        // Open language selection dialog
    }

    fun selectUnits() {
        // Toggle between metric and imperial
        val currentUnits = _uiState.value.selectedUnits
        val nextUnits = when (currentUnits) {
            AppUnits.METRIC -> AppUnits.IMPERIAL
            AppUnits.IMPERIAL -> AppUnits.METRIC
        }
        _uiState.value = _uiState.value.copy(selectedUnits = nextUnits)
    }

    // Data & Privacy
    fun exportData() {
        // Start data export process
    }

    fun openPrivacyPolicy() {
        // Open privacy policy in browser
    }

    fun openTermsOfService() {
        // Open terms of service in browser
    }

    // Community
    fun toggleCommunityFeatures() {
        _uiState.value = _uiState.value.copy(
            communityFeaturesEnabled = !_uiState.value.communityFeaturesEnabled
        )
    }

    fun togglePublicProfile() {
        _uiState.value = _uiState.value.copy(
            publicProfileEnabled = !_uiState.value.publicProfileEnabled
        )
    }

    // Support & About
    fun openSupport() {
        // Open support/help screen
    }

    fun rateApp() {
        // Open app store for rating
    }

    fun showCredits() {
        // Show credits dialog
    }

    // Danger Zone
    fun deleteAccount() {
        // Show confirmation dialog for account deletion
        _uiState.value = _uiState.value.copy(showDangerZone = true)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}