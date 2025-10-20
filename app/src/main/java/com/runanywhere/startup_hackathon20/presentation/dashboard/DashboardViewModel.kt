package com.runanywhere.startup_hackathon20.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.*
import com.runanywhere.startup_hackathon20.data.repositories.*
import com.runanywhere.startup_hackathon20.domain.usecases.AnalyzePetHealthUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import java.util.*

data class DashboardUiState(
    val currentPet: PetProfile? = null,
    val healthMetrics: HealthMetrics? = null,
    val latestInsight: AIInsight? = null,
    val healthScore: Float = 0f,
    val healthStatus: HealthStatus = HealthStatus.GOOD,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long = 0L,
    val animateHealthScore: Boolean = false,
    val showSuccessMessage: String? = null
)

enum class HealthStatus(val displayName: String, val color: Long) {
    EXCELLENT("Excellent", 0xFF4CAF50),
    GOOD("Good", 0xFF8BC34A),
    FAIR("Fair", 0xFFFF9800),
    CONCERNING("Concerning", 0xFFFF5722),
    CRITICAL("Critical", 0xFFD32F2F)
}

class DashboardViewModel(
    private val petRepository: PetRepository,
    private val healthRepository: HealthRepository,
    private val aiRepository: AIRepository,
    private val analyzePetHealthUseCase: AnalyzePetHealthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var insightGenerationJob: Job? = null

    init {
        loadDashboardData()
        startPeriodicHealthUpdates()
    }

    /**
     * Load initial dashboard data with proper error handling
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Check if we have any pets
                val pets = petRepository.getAllPetsList()

                if (pets.isEmpty()) {
                    // Create demo pet if none exists
                    createDemoPet()
                } else {
                    val currentPet = pets.first()
                    _uiState.value = _uiState.value.copy(currentPet = currentPet)
                    loadPetHealthData(currentPet.id)
                }

            } catch (e: CancellationException) {
                // Handle coroutine cancellation gracefully
                throw e
            } catch (e: Exception) {
                handleError("Failed to load dashboard data", e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Load pet health data with optimized database queries
     */
    private suspend fun loadPetHealthData(petId: String) {
        try {
            // Load latest health metrics with fallback to mock data
            val metrics = healthRepository.getLatestMetricsForPetSync(petId)
                ?: generateMockHealthMetrics(petId)

            val healthScore = calculateHealthScore(metrics)
            val healthStatus = determineHealthStatus(healthScore)

            _uiState.value = _uiState.value.copy(
                healthMetrics = metrics,
                healthScore = healthScore,
                healthStatus = healthStatus,
                lastUpdated = System.currentTimeMillis(),
                animateHealthScore = true
            )

            // Load AI insights asynchronously
            loadAIInsights(petId)

            // Reset animation flag after delay
            delay(500)
            _uiState.value = _uiState.value.copy(animateHealthScore = false)

        } catch (e: Exception) {
            handleError("Failed to load pet health data", e)
        }
    }

    /**
     * Load AI insights with proper cancellation support
     */
    private suspend fun loadAIInsights(petId: String) {
        insightGenerationJob?.cancel()
        insightGenerationJob = viewModelScope.launch {
            try {
                // Try to get existing insights using the correct repository method
                val recentInsights = aiRepository.getRecentInsights(petId, 1)
                recentInsights.collect { insights ->
                    val latestInsight = insights.firstOrNull()

                    if (latestInsight != null && !isInsightStale(latestInsight)) {
                        _uiState.value = _uiState.value.copy(latestInsight = latestInsight)
                    } else {
                        // Generate new insight
                        generateNewInsight(petId)
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Don't show errors for insight generation failures
                // Just log them silently
            }
        }
    }

    /**
     * Generate new AI insight with proper error handling
     */
    private suspend fun generateNewInsight(petId: String) {
        try {
            val currentPet = _uiState.value.currentPet ?: return
            val currentMetrics = _uiState.value.healthMetrics ?: return

            // Create insight request for the AI repository
            val insightRequest = InsightRequest(
                petProfile = currentPet,
                healthMetrics = currentMetrics,
                requestType = "GENERAL_HEALTH",
                specificConcerns = null
            )

            val insight = aiRepository.generateHealthInsight(insightRequest)

            // Save insight to database using the correct repository method
            aiRepository.saveInsight(insight)

            _uiState.value = _uiState.value.copy(latestInsight = insight)

        } catch (e: Exception) {
            // Create fallback insight
            val fallbackInsight = createFallbackInsight(petId)
            _uiState.value = _uiState.value.copy(latestInsight = fallbackInsight)
        }
    }

    /**
     * Calculate health score based on multiple factors
     */
    private fun calculateHealthScore(metrics: HealthMetrics): Float {
        var score = 0f
        var factors = 0

        // Heart rate scoring (0-25 points)
        metrics.heartRate?.let { hr ->
            score += when {
                hr in 60..100 -> 25f
                hr in 50..120 -> 20f
                hr in 40..130 -> 15f
                else -> 10f
            }
            factors++
        }

        // Activity scoring (0-25 points)
        val activityPercent = metrics.stepsToday.toFloat() / metrics.stepsGoal
        score += (activityPercent * 25f).coerceAtMost(25f)
        factors++

        // Mood scoring (0-25 points)
        score += (metrics.moodScore / 10f) * 25f
        factors++

        // Sleep scoring (0-25 points)
        val sleepScore = when {
            metrics.sleepHours >= 8f -> 25f
            metrics.sleepHours >= 6f -> 20f
            metrics.sleepHours >= 4f -> 15f
            else -> 10f
        }
        score += sleepScore
        factors++

        return if (factors > 0) score / factors * 4f else 0f // Scale to 0-100
    }

    /**
     * Determine health status based on score
     */
    private fun determineHealthStatus(score: Float): HealthStatus {
        return when {
            score >= 90f -> HealthStatus.EXCELLENT
            score >= 75f -> HealthStatus.GOOD
            score >= 60f -> HealthStatus.FAIR
            score >= 40f -> HealthStatus.CONCERNING
            else -> HealthStatus.CRITICAL
        }
    }

    /**
     * Check if insight is stale and needs refresh
     */
    private fun isInsightStale(insight: AIInsight): Boolean {
        val hoursSince = (System.currentTimeMillis() - insight.timestamp) / (1000 * 60 * 60)
        return hoursSince > 6 // Consider stale after 6 hours
    }

    /**
     * Generate mock health metrics with realistic values
     */
    private fun generateMockHealthMetrics(petId: String): HealthMetrics {
        return HealthMetrics(
            petId = petId,
            timestamp = System.currentTimeMillis(),
            heartRate = kotlin.random.Random.nextInt(70, 95),
            bodyTemperature = 37.5f + kotlin.random.Random.nextFloat() * 1.5f,
            stepsToday = kotlin.random.Random.nextInt(6000, 12000),
            stepsGoal = 10000,
            hydrationMl = kotlin.random.Random.nextInt(600, 1000),
            hydrationGoalMl = 1000,
            sleepHours = 7f + kotlin.random.Random.nextFloat() * 3f,
            sleepQuality = listOf("POOR", "FAIR", "GOOD", "EXCELLENT").random(),
            respiratoryRate = kotlin.random.Random.nextInt(15, 25),
            moodScore = 6f + kotlin.random.Random.nextFloat() * 4f,
            activityLevel = listOf("SEDENTARY", "LIGHT", "MODERATE", "ACTIVE").random(),
            stressIndicators = listOf("None", "Mild", "Moderate").random().let { listOf(it) }
        )
    }

    /**
     * Create fallback insight when AI generation fails
     */
    private fun createFallbackInsight(petId: String): AIInsight {
        val metrics = _uiState.value.healthMetrics
        val pet = _uiState.value.currentPet

        return AIInsight(
            petId = petId,
            timestamp = System.currentTimeMillis(),
            healthStatus = _uiState.value.healthStatus.name,
            summary = "Health data has been recorded for ${pet?.name ?: "your pet"}. " +
                    "Current vital signs appear ${_uiState.value.healthStatus.displayName.lowercase()}.",
            detailedAnalysis = buildString {
                append("Based on the latest health metrics:\n\n")
                metrics?.let { m ->
                    append("• Heart Rate: ${m.heartRate} BPM - ")
                    append(if (m.heartRate in 60..100) "Normal range" else "Monitor closely")
                    append("\n• Activity: ${m.stepsToday}/${m.stepsGoal} steps - ")
                    append("${(m.stepsToday.toFloat() / m.stepsGoal * 100).toInt()}% of goal\n")
                    append("• Sleep Quality: ${m.sleepQuality} (${m.sleepHours} hours)\n")
                    append("• Mood Score: ${m.moodScore}/10\n")
                }
                append("\nContinue monitoring your pet's health and consult a veterinarian if you notice any concerning changes.")
            },
            recommendations = listOf(
                "Continue current care routine",
                "Monitor daily activity levels",
                "Ensure adequate hydration",
                "Maintain regular sleep schedule",
                "Schedule routine vet checkup if due"
            ),
            alertLevel = "NORMAL",
            veterinaryConsultRequired = false
        )
    }

    /**
     * Start periodic health updates every 5 minutes
     */
    private fun startPeriodicHealthUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L) // 5 minutes
                try {
                    _uiState.value.currentPet?.let { pet ->
                        // Generate new mock data to simulate real-time updates
                        val newMetrics = generateMockHealthMetrics(pet.id)
                        healthRepository.insertHealthMetrics(newMetrics)

                        val newScore = calculateHealthScore(newMetrics)
                        val newStatus = determineHealthStatus(newScore)

                        _uiState.value = _uiState.value.copy(
                            healthMetrics = newMetrics,
                            healthScore = newScore,
                            healthStatus = newStatus,
                            lastUpdated = System.currentTimeMillis(),
                            animateHealthScore = true
                        )

                        delay(500)
                        _uiState.value = _uiState.value.copy(animateHealthScore = false)
                    }
                } catch (e: Exception) {
                    // Silently handle periodic update errors
                }
            }
        }
    }

    /**
     * Refresh data with loading state
     */
    fun refreshData() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)

            try {
                _uiState.value.currentPet?.let { pet ->
                    loadPetHealthData(pet.id)
                    _uiState.value = _uiState.value.copy(
                        showSuccessMessage = "Data refreshed successfully!"
                    )

                    // Clear success message after 3 seconds
                    delay(3000)
                    _uiState.value = _uiState.value.copy(showSuccessMessage = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError("Failed to refresh data", e)
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    /**
     * Generate new mock health data
     */
    fun generateMockHealthData() {
        viewModelScope.launch {
            try {
                _uiState.value.currentPet?.let { pet ->
                    val mockMetrics = generateMockHealthMetrics(pet.id)
                    healthRepository.insertHealthMetrics(mockMetrics)

                    val healthScore = calculateHealthScore(mockMetrics)
                    val healthStatus = determineHealthStatus(healthScore)

                    _uiState.value = _uiState.value.copy(
                        healthMetrics = mockMetrics,
                        healthScore = healthScore,
                        healthStatus = healthStatus,
                        lastUpdated = System.currentTimeMillis(),
                        animateHealthScore = true,
                        showSuccessMessage = "New health data generated!"
                    )

                    // Generate AI insight for new data
                    generateNewInsight(pet.id)

                    // Reset animation and clear success message
                    delay(500)
                    _uiState.value = _uiState.value.copy(animateHealthScore = false)

                    delay(2500)
                    _uiState.value = _uiState.value.copy(showSuccessMessage = null)
                }
            } catch (e: Exception) {
                handleError("Failed to generate health data", e)
            }
        }
    }

    /**
     * Create demo pet with error handling
     */
    fun createDemoPet() {
        viewModelScope.launch {
            try {
                val demoPet = PetProfile(
                    name = "Max",
                    breed = "Golden Retriever",
                    species = "Dog",
                    age = 3,
                    weight = 25.5f,
                    gender = "Male",
                    photoUri = "",
                    medicalHistory = listOf("Vaccinated", "Healthy"),
                    vaccinationRecords = listOf("Rabies - 2023", "DHPP - 2023"),
                    microchipId = "123456789",
                    ownerName = "Pet Parent",
                    ownerPhone = "+1234567890",
                    emergencyContact = "+1234567890"
                )

                petRepository.insertPet(demoPet)
                _uiState.value = _uiState.value.copy(
                    currentPet = demoPet,
                    showSuccessMessage = "Welcome to PawSync! Meet Max, your demo pet."
                )

                // Generate initial health data
                generateMockHealthData()

            } catch (e: Exception) {
                handleError("Failed to create demo pet", e)
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = null)
    }

    /**
     * Handle errors with proper logging and user-friendly messages
     */
    private fun handleError(context: String, exception: Exception) {
        val userFriendlyMessage = when (exception) {
            is java.net.UnknownHostException -> "No internet connection. Please check your network."
            is java.util.concurrent.TimeoutException -> "Request timed out. Please try again."
            is kotlinx.coroutines.CancellationException -> return // Don't show cancellation as error
            else -> "$context. Please try again."
        }

        _uiState.value = _uiState.value.copy(error = userFriendlyMessage)

        // Auto-clear error after 10 seconds
        viewModelScope.launch {
            delay(10000)
            if (_uiState.value.error == userFriendlyMessage) {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    /**
     * Clean up resources when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
        insightGenerationJob?.cancel()
    }
}