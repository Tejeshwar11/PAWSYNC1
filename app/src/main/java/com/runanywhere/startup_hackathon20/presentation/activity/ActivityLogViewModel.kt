package com.runanywhere.startup_hackathon20.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.ActivityLog
import com.runanywhere.startup_hackathon20.data.models.ActivityType
import com.runanywhere.startup_hackathon20.data.models.ActivityIntensity
import com.runanywhere.startup_hackathon20.data.repositories.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class ActivityLogUiState(
    val activities: List<ActivityLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedActivity: ActivityLog? = null,
    val showAddDialog: Boolean = false,
    val totalActivitiesThisMonth: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val averageDuration: Long = 0,
    val mostCommonActivity: String? = null
)

data class NewActivityState(
    val activityType: ActivityType = ActivityType.WALK,
    val duration: Int = 30,
    val intensity: ActivityIntensity = ActivityIntensity.MODERATE,
    val notes: String = "",
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val location: String = ""
)

class ActivityLogViewModel(
    private val activityRepository: ActivityRepository = ActivityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityLogUiState())
    val uiState: StateFlow<ActivityLogUiState> = _uiState.asStateFlow()

    private val _newActivityState = MutableStateFlow(NewActivityState())
    val newActivityState: StateFlow<NewActivityState> = _newActivityState.asStateFlow()

    init {
        loadActivities()
        loadStatistics()
    }

    fun loadActivities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                activityRepository.getAllActivitiesForPet("current_pet").collect { activities ->
                    _uiState.value = _uiState.value.copy(
                        activities = activities,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val stats = activityRepository.getOverallActivityStats("current_pet", 30)
                val typeDistribution = activityRepository.getActivityTypeDistribution("current_pet")

                _uiState.value = _uiState.value.copy(
                    totalActivitiesThisMonth = stats.totalActivities,
                    totalCaloriesBurned = stats.totalCalories,
                    averageDuration = if (stats.totalActivities > 0) stats.totalDuration / stats.totalActivities else 0,
                    mostCommonActivity = typeDistribution.maxByOrNull { it.value }?.key
                )
            } catch (e: Exception) {
                // Handle error silently for statistics
            }
        }
    }

    fun showAddActivityDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun hideAddActivityDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
        _newActivityState.value = NewActivityState()
    }

    fun updateNewActivity(update: (NewActivityState) -> NewActivityState) {
        _newActivityState.value = update(_newActivityState.value)
    }

    fun saveActivity() {
        viewModelScope.launch {
            try {
                val state = _newActivityState.value

                activityRepository.logActivity(
                    petId = "current_pet",
                    activityType = state.activityType,
                    duration = state.duration,
                    intensity = state.intensity,
                    distanceKm = state.distanceKm,
                    caloriesBurned = state.caloriesBurned,
                    notes = state.notes.takeIf { it.isNotBlank() },
                    location = state.location.takeIf { it.isNotBlank() }
                )

                hideAddActivityDialog()
                loadActivities()
                loadStatistics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteActivity(activity: ActivityLog) {
        viewModelScope.launch {
            try {
                activityRepository.deleteActivity(activity)
                loadActivities()
                loadStatistics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivityLogViewModel::class.java)) {
                return ActivityLogViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}