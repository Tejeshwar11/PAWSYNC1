package com.runanywhere.startup_hackathon20.presentation.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.runanywhere.startup_hackathon20.data.repositories.HealthRepository
import com.runanywhere.startup_hackathon20.data.repositories.PetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TimeRange(val displayName: String, val days: Int) {
    TODAY("Today", 1),
    WEEK("Week", 7),
    MONTH("Month", 30),
    QUARTER("3 Months", 90),
    YEAR("Year", 365)
}

enum class HealthTrend {
    IMPROVING, DECLINING, STABLE
}

enum class EventSeverity {
    LOW, NORMAL, MEDIUM, HIGH
}

data class ActivityDataPoint(
    val timestamp: Long,
    val steps: Int,
    val activeMinutes: Int
)

data class HeartRateDataPoint(
    val timestamp: Long,
    val heartRate: Int
)

data class MoodDataPoint(
    val timestamp: Long,
    val moodScore: Float
)

data class SleepDataPoint(
    val timestamp: Long,
    val sleepHours: Float,
    val quality: String
)

data class HealthEvent(
    val id: String,
    val title: String,
    val description: String,
    val timeAgo: String,
    val icon: String,
    val color: Color,
    val severity: EventSeverity,
    val timestamp: Long
)

data class HealthHistoryUiState(
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Overview Data
    val averageHeartRate: Int = 0,
    val averageSteps: Int = 0,
    val averageMood: Float = 0f,
    val averageSleep: Float = 0f,

    // Trends
    val heartRateTrend: HealthTrend = HealthTrend.STABLE,
    val activityTrend: HealthTrend = HealthTrend.STABLE,
    val moodTrend: HealthTrend = HealthTrend.STABLE,
    val sleepTrend: HealthTrend = HealthTrend.STABLE,

    // Chart Data
    val activityData: List<ActivityDataPoint> = emptyList(),
    val heartRateData: List<HeartRateDataPoint> = emptyList(),
    val moodData: List<MoodDataPoint> = emptyList(),
    val sleepData: List<SleepDataPoint> = emptyList(),

    // Timeline
    val healthEvents: List<HealthEvent> = emptyList()
)

class HealthHistoryViewModel(
    private val healthRepository: HealthRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthHistoryUiState())
    val uiState: StateFlow<HealthHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHealthData()
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
        loadHealthData()
    }

    private fun loadHealthData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                // Load pet data
                val pets = petRepository.getAllPetsList()
                val currentPet = pets.firstOrNull()

                if (currentPet != null) {
                    // Generate mock data for demo
                    val mockData = generateMockHealthData(_uiState.value.selectedTimeRange)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        averageHeartRate = mockData.averageHeartRate,
                        averageSteps = mockData.averageSteps,
                        averageMood = mockData.averageMood,
                        averageSleep = mockData.averageSleep,
                        heartRateTrend = mockData.heartRateTrend,
                        activityTrend = mockData.activityTrend,
                        moodTrend = mockData.moodTrend,
                        sleepTrend = mockData.sleepTrend,
                        activityData = mockData.activityData,
                        heartRateData = mockData.heartRateData,
                        moodData = mockData.moodData,
                        sleepData = mockData.sleepData,
                        healthEvents = mockData.healthEvents
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load health data: ${e.message}"
                )
            }
        }
    }

    private fun generateMockHealthData(timeRange: TimeRange): HealthHistoryUiState {
        val dataPoints = when (timeRange) {
            TimeRange.TODAY -> 24 // Hourly data
            TimeRange.WEEK -> 7   // Daily data
            TimeRange.MONTH -> 30 // Daily data
            TimeRange.QUARTER -> 90 // Daily data
            TimeRange.YEAR -> 52  // Weekly data
        }

        val currentTime = System.currentTimeMillis()
        val intervalMs = when (timeRange) {
            TimeRange.TODAY -> 60 * 60 * 1000L // 1 hour
            TimeRange.WEEK -> 24 * 60 * 60 * 1000L // 1 day
            TimeRange.MONTH -> 24 * 60 * 60 * 1000L // 1 day
            TimeRange.QUARTER -> 24 * 60 * 60 * 1000L // 1 day
            TimeRange.YEAR -> 7 * 24 * 60 * 60 * 1000L // 1 week
        }

        // Generate activity data
        val activityData: List<ActivityDataPoint> = (0 until dataPoints).map { i ->
            ActivityDataPoint(
                timestamp = currentTime - (dataPoints - i) * intervalMs,
                steps = kotlin.random.Random.nextInt(6000, 12001),
                activeMinutes = kotlin.random.Random.nextInt(30, 91)
            )
        }

        // Generate heart rate data
        val heartRateData: List<HeartRateDataPoint> = (0 until dataPoints).map { i ->
            HeartRateDataPoint(
                timestamp = currentTime - (dataPoints - i) * intervalMs,
                heartRate = kotlin.random.Random.nextInt(70, 96)
            )
        }

        // Generate mood data
        val moodData: List<MoodDataPoint> = (0 until dataPoints).map { i ->
            MoodDataPoint(
                timestamp = currentTime - (dataPoints - i) * intervalMs,
                moodScore = 6.0f + kotlin.random.Random.nextFloat() * 3.5f
            )
        }

        // Generate sleep data
        val sleepData: List<SleepDataPoint> = (0 until dataPoints).map { i ->
            val hours = 7.0f + kotlin.random.Random.nextFloat() * 3.5f
            val quality = when {
                hours >= 9 -> "EXCELLENT"
                hours >= 8 -> "GOOD"
                hours >= 7 -> "FAIR"
                else -> "POOR"
            }
            SleepDataPoint(
                timestamp = currentTime - (dataPoints - i) * intervalMs,
                sleepHours = hours,
                quality = quality
            )
        }

        // Generate health events
        val healthEvents = listOf(
            HealthEvent(
                id = "1",
                title = "Daily Health Check",
                description = "All vitals within normal range. Great activity level today!",
                timeAgo = "2 hours ago",
                icon = "❤️",
                color = Color(0xFF4CAF50),
                severity = EventSeverity.NORMAL,
                timestamp = currentTime - 2 * 60 * 60 * 1000L
            ),
            HealthEvent(
                id = "2",
                title = "Exercise Goal Achieved",
                description = "Max completed 12,000 steps - exceeded daily goal!",
                timeAgo = "5 hours ago",
                icon = "🏃",
                color = Color(0xFF00BCD4),
                severity = EventSeverity.LOW,
                timestamp = currentTime - 5 * 60 * 60 * 1000L
            ),
            HealthEvent(
                id = "3",
                title = "Sleep Quality Improved",
                description = "Excellent sleep quality - 9.2 hours of deep rest",
                timeAgo = "1 day ago",
                icon = "😴",
                color = Color(0xFF9C27B0),
                severity = EventSeverity.LOW,
                timestamp = currentTime - 24 * 60 * 60 * 1000L
            ),
            HealthEvent(
                id = "4",
                title = "Vaccination Due",
                description = "Annual rabies vaccination is due next week",
                timeAgo = "2 days ago",
                icon = "💉",
                color = Color(0xFFFF9800),
                severity = EventSeverity.MEDIUM,
                timestamp = currentTime - 2 * 24 * 60 * 60 * 1000L
            ),
            HealthEvent(
                id = "5",
                title = "Vet Checkup",
                description = "Regular checkup completed. All systems healthy!",
                timeAgo = "1 week ago",
                icon = "👨‍⚕️",
                color = Color(0xFF4CAF50),
                severity = EventSeverity.NORMAL,
                timestamp = currentTime - 7 * 24 * 60 * 60 * 1000L
            )
        )

        // Calculate averages
        val avgHeartRate = heartRateData.map { it.heartRate }.average().toInt()
        val avgSteps = activityData.map { it.steps }.average().toInt()
        val avgMood = moodData.map { it.moodScore }.average().toFloat()
        val avgSleep = sleepData.map { it.sleepHours }.average().toFloat()

        // Determine trends (simplified)
        val heartRateTrend = if (heartRateData.size >= 2) {
            val recent = heartRateData.takeLast(3).map { it.heartRate }.average()
            val earlier = heartRateData.take(3).map { it.heartRate }.average()
            when {
                recent > earlier + 2 -> HealthTrend.DECLINING // Higher heart rate = concerning
                recent < earlier - 2 -> HealthTrend.IMPROVING
                else -> HealthTrend.STABLE
            }
        } else HealthTrend.STABLE

        val activityTrend = if (activityData.size >= 2) {
            val recent = activityData.takeLast(3).map { it.steps }.average()
            val earlier = activityData.take(3).map { it.steps }.average()
            when {
                recent > earlier + 500 -> HealthTrend.IMPROVING
                recent < earlier - 500 -> HealthTrend.DECLINING
                else -> HealthTrend.STABLE
            }
        } else HealthTrend.STABLE

        val moodTrend = if (moodData.size >= 2) {
            val recent = moodData.takeLast(3).map { it.moodScore }.average()
            val earlier = moodData.take(3).map { it.moodScore }.average()
            when {
                recent > earlier + 0.5 -> HealthTrend.IMPROVING
                recent < earlier - 0.5 -> HealthTrend.DECLINING
                else -> HealthTrend.STABLE
            }
        } else HealthTrend.STABLE

        val sleepTrend = if (sleepData.size >= 2) {
            val recent = sleepData.takeLast(3).map { it.sleepHours }.average()
            val earlier = sleepData.take(3).map { it.sleepHours }.average()
            when {
                recent > earlier + 0.5 -> HealthTrend.IMPROVING
                recent < earlier - 0.5 -> HealthTrend.DECLINING
                else -> HealthTrend.STABLE
            }
        } else HealthTrend.STABLE

        return HealthHistoryUiState(
            averageHeartRate = avgHeartRate,
            averageSteps = avgSteps,
            averageMood = avgMood,
            averageSleep = avgSleep,
            heartRateTrend = heartRateTrend,
            activityTrend = activityTrend,
            moodTrend = moodTrend,
            sleepTrend = sleepTrend,
            activityData = activityData,
            heartRateData = heartRateData,
            moodData = moodData,
            sleepData = sleepData,
            healthEvents = healthEvents
        )
    }

    suspend fun exportHealthReport() {
        // Implementation for exporting health report as PDF
        // This would typically use a PDF generation library
        try {
            val reportData = generateHealthReport()
            // Save to device or share
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "Failed to export report: ${e.message}")
        }
    }

    suspend fun shareHealthReport() {
        // Implementation for sharing health report
        try {
            val reportData = generateHealthReport()
            // Share via intent
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "Failed to share report: ${e.message}")
        }
    }

    private fun generateHealthReport(): String {
        val state = _uiState.value
        return buildString {
            appendLine("🐾 PAWSYNC - Pet Health Report")
            appendLine(
                "Generated: ${
                    java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())
                }"
            )
            appendLine()
            appendLine("📊 Health Summary (${state.selectedTimeRange.displayName}):")
            appendLine("Average Heart Rate: ${state.averageHeartRate} BPM")
            appendLine("Average Daily Steps: ${state.averageSteps}")
            appendLine("Average Mood Score: ${"%.1f".format(state.averageMood)}/10")
            appendLine("Average Sleep: ${"%.1f".format(state.averageSleep)} hours")
            appendLine()
            appendLine("📈 Health Trends:")
            appendLine("Heart Rate: ${state.heartRateTrend.name}")
            appendLine("Activity: ${state.activityTrend.name}")
            appendLine("Mood: ${state.moodTrend.name}")
            appendLine("Sleep: ${state.sleepTrend.name}")
            appendLine()
            appendLine("Recent Health Events:")
            state.healthEvents.take(5).forEach { event ->
                appendLine("• ${event.title} - ${event.timeAgo}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}