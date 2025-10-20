package com.runanywhere.startup_hackathon20.domain.usecases

import com.runanywhere.startup_hackathon20.data.models.*
import com.runanywhere.startup_hackathon20.data.models.HealthStatus as DataHealthStatus
import com.runanywhere.startup_hackathon20.data.repositories.PetRepository
import com.runanywhere.startup_hackathon20.data.repositories.HealthRepository
import com.runanywhere.startup_hackathon20.data.repositories.AIRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.minutes

/**
 * Enhanced use case for analyzing pet health with improved error handling,
 * performance optimizations, caching, and animation state management.
 */
class AnalyzePetHealthUseCase(
    private val petRepository: PetRepository,
    private val healthRepository: HealthRepository,
    private val aiRepository: AIRepository
) {
    // Thread-safe cache for health status with TTL
    private val healthStatusCache = ConcurrentHashMap<String, CachedHealthStatus>()
    private val trendCache = ConcurrentHashMap<String, CachedTrends>()
    private val analysisLock = Mutex()

    // Cache TTL configurations
    private val statusCacheTtl = 2.minutes.inWholeMilliseconds
    private val trendCacheTtl = 5.minutes.inWholeMilliseconds

    /**
     * Execute health analysis with comprehensive error handling and timeout
     */
    suspend fun execute(petId: String): Result<AnalysisResult> {
        return try {
            withTimeout(30.seconds) {
                analysisLock.withLock {
                    performHealthAnalysis(petId)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(
                AnalysisException.Timeout(
                    "Health analysis timed out after 30 seconds",
                    e
                )
            )
        } catch (e: Exception) {
            Result.failure(AnalysisException.Generic("Health analysis failed", e))
        }
    }

    private suspend fun performHealthAnalysis(petId: String): Result<AnalysisResult> {
        // Validate input
        if (petId.isBlank()) {
            return Result.failure(AnalysisException.InvalidInput("Pet ID cannot be blank"))
        }

        // Get pet profile with validation
        val petProfile = petRepository.getPetById(petId)
            ?: return Result.failure(AnalysisException.PetNotFound("Pet with ID $petId not found"))

        // Get latest health metrics with fallback
        val healthMetrics = healthRepository.getLatestMetrics(petId)
            ?: return Result.failure(AnalysisException.NoHealthData("No health data available for pet $petId"))

        // Validate health metrics
        val validationResult = validateHealthMetrics(healthMetrics)
        if (!validationResult.isValid) {
            return Result.failure(AnalysisException.InvalidHealthData(validationResult.errorMessage))
        }

        return try {
            // Create insight request with validation
            val insightRequest = InsightRequest(
                petProfile = petProfile,
                healthMetrics = healthMetrics,
                requestType = "COMPREHENSIVE_HEALTH_ANALYSIS",
                specificConcerns = null
            )

            // Generate AI insight with retry logic
            val aiInsight = generateInsightWithRetry(insightRequest, maxRetries = 3)

            // Save insight to database
            val savedId = aiRepository.saveInsight(aiInsight)

            Result.success(
                AnalysisResult(
                    insight = aiInsight,
                    savedId = savedId,
                    processingTimeMs = aiInsight.processingTimeMs,
                    cacheUsed = false
                )
            )
        } catch (e: Exception) {
            Result.failure(AnalysisException.AIProcessingFailed("AI insight generation failed", e))
        }
    }

    private suspend fun generateInsightWithRetry(
        request: InsightRequest,
        maxRetries: Int = 3
    ): AIInsight {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                return aiRepository.generateHealthInsight(request)
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    // Exponential backoff: 1s, 2s, 4s
                    delay((1000L * (1 shl attempt)))
                }
            }
        }

        throw lastException ?: Exception("Failed to generate insight after $maxRetries attempts")
    }

    /**
     * Observe health status with caching and smooth state transitions
     */
    fun observeHealthStatus(petId: String): Flow<HealthStatusState> {
        return flow {
            // Check cache first
            val cached = healthStatusCache[petId]
            if (cached != null && !cached.isExpired(statusCacheTtl)) {
                emit(HealthStatusState.Success(cached.status, fromCache = true))
            } else {
                emit(HealthStatusState.Loading)
            }

            // Get fresh data
            combine(
                petRepository.getPetByIdFlow(petId),
                healthRepository.getLatestMetricsFlow(petId)
            ) { pet, metrics ->
                when {
                    pet == null -> HealthStatusState.Error(AnalysisException.PetNotFound("Pet not found"))
                    metrics == null -> HealthStatusState.Error(AnalysisException.NoHealthData("No health data"))
                    else -> {
                        val status = calculateHealthStatus(metrics)
                        // Update cache
                        healthStatusCache[petId] =
                            CachedHealthStatus(status, System.currentTimeMillis())
                        HealthStatusState.Success(status, fromCache = false)
                    }
                }
            }.collect { state ->
                emit(state)
                // Add smooth transition delay for animations
                if (state is HealthStatusState.Success && !state.fromCache) {
                    delay(100) // Allow UI animations to complete
                }
            }
        }.catch { throwable ->
            emit(
                HealthStatusState.Error(
                    AnalysisException.Generic(
                        "Status observation failed",
                        throwable
                    )
                )
            )
        }.distinctUntilChanged()
    }

    /**
     * Enhanced health status calculation with more nuanced logic
     */
    private fun calculateHealthStatus(metrics: HealthMetrics): DataHealthStatus {
        val scores = mutableListOf<HealthScore>()

        // Heart rate scoring (weight: 25%)
        metrics.heartRate?.let { hr ->
            val score = when {
                hr in 70..110 -> 100
                hr in 60..130 -> 80
                hr in 50..140 -> 60
                hr in 40..150 -> 40
                else -> 20
            }
            scores.add(HealthScore("heart_rate", score, 0.25))
        }

        // Temperature scoring (weight: 25%)
        metrics.bodyTemperature?.let { temp ->
            val score = when {
                temp in 37.5f..39.0f -> 100
                temp in 37.0f..39.5f -> 80
                temp in 36.5f..40.0f -> 60
                temp in 36.0f..40.5f -> 40
                else -> 20
            }
            scores.add(HealthScore("temperature", score, 0.25))
        }

        // Activity scoring (weight: 20%)
        val activityPercent = metrics.stepsToday.toFloat() / metrics.stepsGoal.coerceAtLeast(1)
        val activityScore = when {
            activityPercent >= 1.0f -> 100
            activityPercent >= 0.8f -> 90
            activityPercent >= 0.6f -> 80
            activityPercent >= 0.4f -> 70
            activityPercent >= 0.2f -> 60
            else -> 40
        }
        scores.add(HealthScore("activity", activityScore, 0.20))

        // Mood scoring (weight: 15%)
        val moodScore = (metrics.moodScore / 10f * 100).toInt().coerceIn(0, 100)
        scores.add(HealthScore("mood", moodScore, 0.15))

        // Sleep scoring (weight: 15%)
        val sleepScore = when {
            metrics.sleepHours >= 8f -> 100
            metrics.sleepHours >= 6f -> 80
            metrics.sleepHours >= 4f -> 60
            metrics.sleepHours >= 2f -> 40
            else -> 20
        }
        scores.add(HealthScore("sleep", sleepScore, 0.15))

        // Calculate weighted average
        val totalScore = if (scores.isNotEmpty()) {
            scores.sumOf { it.score * it.weight } / scores.sumOf { it.weight }
        } else 50.0 // Default to fair if no data

        // Check for critical conditions first
        if (isHealthCritical(metrics)) return DataHealthStatus.CRITICAL
        if (isHealthConcerning(metrics)) return DataHealthStatus.CONCERNING

        // Use calculated score for other statuses
        return when {
            totalScore >= 90 -> DataHealthStatus.EXCELLENT
            totalScore >= 75 -> DataHealthStatus.GOOD
            totalScore >= 60 -> DataHealthStatus.FAIR
            totalScore >= 40 -> DataHealthStatus.CONCERNING
            else -> DataHealthStatus.CRITICAL
        }
    }

    private fun isHealthCritical(metrics: HealthMetrics): Boolean {
        return metrics.heartRate?.let { it < 40 || it > 180 } == true ||
                metrics.bodyTemperature?.let { it < 35.0f || it > 41.0f } == true ||
                metrics.moodScore < 2.0f ||
                metrics.alertLevel == "CRITICAL" ||
                metrics.stressLevel == "CRITICAL"
    }

    private fun isHealthConcerning(metrics: HealthMetrics): Boolean {
        return metrics.heartRate?.let { it < 50 || it > 150 } == true ||
                metrics.bodyTemperature?.let { it < 36.0f || it > 40.5f } == true ||
                metrics.moodScore < 4.0f ||
                metrics.stepsToday < (metrics.stepsGoal * 0.2f) ||
                metrics.sleepHours < 4.0f ||
                metrics.alertLevel == "HIGH" ||
                metrics.stressLevel == "HIGH"
    }

    /**
     * Get health trends with caching and comprehensive analysis
     */
    suspend fun getHealthTrends(petId: String, days: Int = 7): Result<List<HealthTrend>> {
        return try {
            // Check cache first
            val cacheKey = "${petId}_${days}d"
            val cached = trendCache[cacheKey]
            if (cached != null && !cached.isExpired(trendCacheTtl)) {
                return Result.success(cached.trends)
            }

            // Validate inputs
            if (petId.isBlank()) {
                return Result.failure(AnalysisException.InvalidInput("Pet ID cannot be blank"))
            }
            if (days < 1 || days > 365) {
                return Result.failure(AnalysisException.InvalidInput("Days must be between 1 and 365"))
            }

            val since = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            val metrics = healthRepository.getMetricsSince(petId, since)

            if (metrics.isEmpty()) {
                return Result.failure(AnalysisException.NoHealthData("No health data available for the specified period"))
            }

            val trends = analyzeHealthTrends(metrics)

            // Update cache
            trendCache[cacheKey] = CachedTrends(trends, System.currentTimeMillis())

            Result.success(trends)
        } catch (e: Exception) {
            Result.failure(AnalysisException.TrendAnalysisFailed("Trend analysis failed", e))
        }
    }

    private fun analyzeHealthTrends(metrics: List<HealthMetrics>): List<HealthTrend> {
        if (metrics.size < 2) {
            return emptyList()
        }

        val trends = mutableListOf<HealthTrend>()

        // Heart rate trend
        val heartRates = metrics.mapNotNull { it.heartRate?.toFloat() }
        if (heartRates.size >= 2) {
            val trend = calculateTrend(heartRates)
            val confidence = calculateTrendConfidence(heartRates)
            trends.add(
                HealthTrend(
                    metric = "Heart Rate",
                    direction = trend,
                    currentValue = heartRates.last(),
                    averageValue = heartRates.average().toFloat(),
                    changePercent = calculateChangePercent(heartRates),
                    confidence = confidence,
                    dataPoints = heartRates.size
                )
            )
        }

        // Activity trend
        val steps = metrics.map { it.stepsToday.toFloat() }
        if (steps.size >= 2) {
            val trend = calculateTrend(steps)
            val confidence = calculateTrendConfidence(steps)
            trends.add(
                HealthTrend(
                    metric = "Activity",
                    direction = trend,
                    currentValue = steps.last(),
                    averageValue = steps.average().toFloat(),
                    changePercent = calculateChangePercent(steps),
                    confidence = confidence,
                    dataPoints = steps.size
                )
            )
        }

        // Mood trend
        val moods = metrics.map { it.moodScore }
        if (moods.size >= 2) {
            val trend = calculateTrend(moods)
            val confidence = calculateTrendConfidence(moods)
            trends.add(
                HealthTrend(
                    metric = "Mood",
                    direction = trend,
                    currentValue = moods.last(),
                    averageValue = moods.average().toFloat(),
                    changePercent = calculateChangePercent(moods),
                    confidence = confidence,
                    dataPoints = moods.size
                )
            )
        }

        // Sleep trend
        val sleepHours = metrics.map { it.sleepHours }
        if (sleepHours.size >= 2) {
            val trend = calculateTrend(sleepHours)
            val confidence = calculateTrendConfidence(sleepHours)
            trends.add(
                HealthTrend(
                    metric = "Sleep",
                    direction = trend,
                    currentValue = sleepHours.last(),
                    averageValue = sleepHours.average().toFloat(),
                    changePercent = calculateChangePercent(sleepHours),
                    confidence = confidence,
                    dataPoints = sleepHours.size
                )
            )
        }

        return trends
    }

    private fun calculateTrend(values: List<Float>): TrendDirection {
        if (values.size < 2) return TrendDirection.STABLE

        // Use linear regression for more accurate trend detection
        val n = values.size
        val indices = (0 until n).map { it.toFloat() }

        val sumX = indices.sum()
        val sumY = values.sum()
        val sumXY = indices.zip(values).sumOf { (x, y) -> (x * y).toDouble() }
        val sumXX = indices.sumOf { (it * it).toDouble() }

        val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)

        return when {
            slope > 0.1 -> TrendDirection.IMPROVING
            slope < -0.1 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }

    private fun calculateChangePercent(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val first = values.first()
        val last = values.last()
        return if (first != 0f) ((last - first) / first * 100) else 0f
    }

    private fun calculateTrendConfidence(values: List<Float>): Float {
        if (values.size < 3) return 0.5f

        // Calculate based on data consistency and sample size
        val variance = calculateVariance(values)
        val sampleSizeFactor = (values.size.toFloat() / 10f).coerceAtMost(1f)
        val consistencyFactor = (1f / (1f + variance)).coerceIn(0f, 1f)

        return (sampleSizeFactor + consistencyFactor) / 2f
    }

    private fun calculateVariance(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average().toFloat()
        return values.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    private fun validateHealthMetrics(metrics: HealthMetrics): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate heart rate
        metrics.heartRate?.let { hr ->
            if (hr < 0 || hr > 300) {
                errors.add("Invalid heart rate: $hr BPM")
            }
        }

        // Validate temperature
        metrics.bodyTemperature?.let { temp ->
            if (temp < 30f || temp > 50f) {
                errors.add("Invalid body temperature: $temp°C")
            }
        }

        // Validate mood score
        if (metrics.moodScore < 0f || metrics.moodScore > 10f) {
            errors.add("Invalid mood score: ${metrics.moodScore}")
        }

        // Validate steps
        if (metrics.stepsToday < 0 || metrics.stepsToday > 100000) {
            errors.add("Invalid step count: ${metrics.stepsToday}")
        }

        return ValidationResult(errors.isEmpty(), errors.joinToString("; "))
    }

    /**
     * Clear caches (useful for testing or memory management)
     */
    fun clearCaches() {
        healthStatusCache.clear()
        trendCache.clear()
    }

    // Data classes for enhanced functionality
    data class AnalysisResult(
        val insight: AIInsight,
        val savedId: Long,
        val processingTimeMs: Long,
        val cacheUsed: Boolean
    )

    sealed class HealthStatusState {
        object Loading : HealthStatusState()
        data class Success(val status: DataHealthStatus, val fromCache: Boolean) :
            HealthStatusState()

        data class Error(val exception: AnalysisException) : HealthStatusState()
    }

    data class HealthTrend(
        val metric: String,
        val direction: TrendDirection,
        val currentValue: Float,
        val averageValue: Float,
        val changePercent: Float,
        val confidence: Float, // 0.0 to 1.0
        val dataPoints: Int
    )

    enum class TrendDirection {
        IMPROVING, DECLINING, STABLE
    }

    private data class HealthScore(
        val metric: String,
        val score: Int, // 0-100
        val weight: Double
    )

    private data class CachedHealthStatus(
        val status: DataHealthStatus,
        val timestamp: Long
    ) {
        fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
    }

    private data class CachedTrends(
        val trends: List<HealthTrend>,
        val timestamp: Long
    ) {
        fun isExpired(ttl: Long): Boolean = System.currentTimeMillis() - timestamp > ttl
    }

    private data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )

    // Enhanced exception hierarchy
    sealed class AnalysisException(message: String, cause: Throwable? = null) :
        Exception(message, cause) {
        class InvalidInput(message: String) : AnalysisException(message)
        class PetNotFound(message: String) : AnalysisException(message)
        class NoHealthData(message: String) : AnalysisException(message)
        class InvalidHealthData(message: String) : AnalysisException(message)
        class AIProcessingFailed(message: String, cause: Throwable) :
            AnalysisException(message, cause)

        class TrendAnalysisFailed(message: String, cause: Throwable) :
            AnalysisException(message, cause)

        class Timeout(message: String, cause: Throwable) : AnalysisException(message, cause)
        class Generic(message: String, cause: Throwable) : AnalysisException(message, cause)
    }
}