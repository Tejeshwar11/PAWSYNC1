package com.runanywhere.startup_hackathon20.data.repositories

import com.runanywhere.startup_hackathon20.data.models.HealthMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class HealthRepository {

    // In-memory storage
    private val healthMetrics = ConcurrentHashMap<String, HealthMetrics>()
    private val _healthMetricsFlow = MutableStateFlow<List<HealthMetrics>>(emptyList())

    // Basic CRUD Operations
    suspend fun insertHealthMetrics(metrics: HealthMetrics): Long {
        healthMetrics[metrics.id] = metrics
        updateHealthMetricsFlow()
        return System.currentTimeMillis()
    }

    suspend fun updateHealthMetrics(metrics: HealthMetrics) {
        healthMetrics[metrics.id] = metrics
        updateHealthMetricsFlow()
    }

    suspend fun deleteHealthMetrics(metrics: HealthMetrics) {
        healthMetrics.remove(metrics.id)
        updateHealthMetricsFlow()
    }

    // Retrieve Latest Data
    suspend fun getLatestMetrics(petId: String): HealthMetrics? =
        healthMetrics.values.filter { it.petId == petId }.maxByOrNull { it.timestamp }

    fun getLatestMetricsFlow(petId: String): Flow<HealthMetrics?> =
        _healthMetricsFlow.asStateFlow().map { metrics ->
            metrics.filter { it.petId == petId }.maxByOrNull { it.timestamp }
        }

    fun getRecentMetrics(petId: String, limit: Int = 10): Flow<List<HealthMetrics>> =
        _healthMetricsFlow.asStateFlow().map { metrics ->
            metrics.filter { it.petId == petId }
                .sortedByDescending { it.timestamp }
                .take(limit)
        }

    // Date Range Queries
    fun getMetricsInDateRange(
        petId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<HealthMetrics>> =
        _healthMetricsFlow.asStateFlow().map { metrics ->
            metrics.filter {
                it.petId == petId && it.timestamp >= startTime && it.timestamp <= endTime
            }.sortedByDescending { it.timestamp }
        }

    suspend fun getMetricsSince(petId: String, since: Long): List<HealthMetrics> =
        healthMetrics.values.filter {
            it.petId == petId && it.timestamp >= since
        }.sortedByDescending { it.timestamp }

    // Health Analysis
    suspend fun getHealthAverages(petId: String, since: Long): HealthAverages? {
        val metrics = getMetricsSince(petId, since)
        if (metrics.isEmpty()) return null

        val heartRates = metrics.mapNotNull { it.heartRate }
        val temperatures = metrics.mapNotNull { it.bodyTemperature }
        val steps = metrics.map { it.stepsToday }
        val sleepHours = metrics.map { it.sleepHours }
        val moods = metrics.map { it.moodScore }

        return HealthAverages(
            avgHeartRate = if (heartRates.isNotEmpty()) heartRates.average() else null,
            avgTemperature = if (temperatures.isNotEmpty()) temperatures.average() else null,
            avgSteps = if (steps.isNotEmpty()) steps.average() else null,
            avgSleep = if (sleepHours.isNotEmpty()) sleepHours.average() else null,
            avgMood = if (moods.isNotEmpty()) moods.average() else null
        )
    }

    fun getAnomalousReadings(petId: String): Flow<List<HealthMetrics>> =
        _healthMetricsFlow.asStateFlow().map { metrics ->
            metrics.filter { it.petId == petId && isAnomalous(it) }
                .sortedByDescending { it.timestamp }
        }

    private fun isAnomalous(metrics: HealthMetrics): Boolean {
        return metrics.heartRate?.let { it < 60 || it > 140 } == true ||
                metrics.bodyTemperature?.let { it < 37.5f || it > 39.0f } == true ||
                metrics.moodScore < 4.0f
    }

    suspend fun getDailyActivitySummary(petId: String, since: Long): List<DailyActivitySummary> {
        val metrics = getMetricsSince(petId, since)
        return metrics.groupBy { it.timestamp / (24 * 60 * 60 * 1000L) }
            .map { (day, dayMetrics) ->
                DailyActivitySummary(
                    date = (day * 24 * 60 * 60 * 1000L).toString(),
                    totalSteps = dayMetrics.sumOf { it.stepsToday },
                    avgActiveMinutes = dayMetrics.map { it.activeMinutes }.average(),
                    totalCalories = dayMetrics.sumOf { it.caloriesBurned }
                )
            }
    }

    suspend fun getStepsStatistics(petId: String, since: Long): StepsStatistics? {
        val metrics = getMetricsSince(petId, since)
        val steps = metrics.map { it.stepsToday }.filter { it > 0 }

        if (steps.isEmpty()) return null

        return StepsStatistics(
            avgSteps = steps.average(),
            maxSteps = steps.max(),
            minSteps = steps.min(),
            recordCount = steps.size
        )
    }

    // Sleep Analysis
    suspend fun getSleepAnalysis(petId: String, since: Long): List<SleepAnalysis> {
        val metrics = getMetricsSince(petId, since)
        return metrics.filter { it.sleepHours > 0 }
            .groupBy { it.sleepQuality }
            .map { (quality, qualityMetrics) ->
                SleepAnalysis(
                    avgSleepHours = qualityMetrics.map { it.sleepHours }.average(),
                    avgDeepSleep = qualityMetrics.map { it.deepSleepHours }.average(),
                    avgLightSleep = qualityMetrics.map { it.lightSleepHours }.average(),
                    avgDisturbances = qualityMetrics.map { it.sleepDisturbances }.average(),
                    sleepQuality = quality,
                    nights = qualityMetrics.size
                )
            }
    }

    // Mood Analysis
    suspend fun getMoodAnalysis(petId: String, since: Long): List<MoodAnalysis> {
        val metrics = getMetricsSince(petId, since)
        return metrics.filter { it.moodScore > 0 }
            .groupBy { it.moodType }
            .map { (moodType, moodMetrics) ->
                MoodAnalysis(
                    moodType = moodType,
                    avgScore = moodMetrics.map { it.moodScore }.average(),
                    frequency = moodMetrics.size,
                    avgStress = moodMetrics.first().stressLevel // Simplified
                )
            }
    }

    // Health Alerts
    fun getHealthAlerts(petId: String): Flow<List<HealthMetrics>> =
        _healthMetricsFlow.asStateFlow().map { metrics ->
            metrics.filter {
                it.petId == petId && (it.alertLevel == "HIGH" || it.alertLevel == "CRITICAL")
            }.sortedByDescending { it.timestamp }.take(50)
        }

    suspend fun getAlertCount(petId: String, since: Long): Int =
        healthMetrics.values.count {
            it.petId == petId &&
                    it.timestamp >= since &&
                    (it.alertLevel == "HIGH" || it.alertLevel == "CRITICAL")
        }

    // Business Logic Methods
    suspend fun saveHealthMetrics(
        petId: String,
        heartRate: Int? = null,
        bodyTemperature: Float? = null,
        stepsToday: Int = 0,
        hydrationMl: Int = 0,
        sleepHours: Float = 0f,
        moodScore: Float = 7.5f,
        activityLevel: String = "MODERATE"
    ): Long {
        val metrics = HealthMetrics(
            petId = petId,
            heartRate = heartRate,
            bodyTemperature = bodyTemperature,
            stepsToday = stepsToday,
            hydrationMl = hydrationMl,
            sleepHours = sleepHours,
            moodScore = moodScore,
            activityLevel = activityLevel,
            timestamp = System.currentTimeMillis()
        )
        return insertHealthMetrics(metrics)
    }

    suspend fun analyzeHealthTrends(petId: String, days: Int = 7): HealthTrendAnalysis {
        val since = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val metrics = getMetricsSince(petId, since)

        if (metrics.isEmpty()) {
            return HealthTrendAnalysis(
                heartRateTrend = "No data",
                activityTrend = "No data",
                moodTrend = "No data",
                overallTrend = "Insufficient data"
            )
        }

        val heartRates = metrics.mapNotNull { it.heartRate }
        val steps = metrics.map { it.stepsToday }
        val moods = metrics.map { it.moodScore }

        return HealthTrendAnalysis(
            heartRateTrend = analyzeTrend(heartRates.map { it.toFloat() }),
            activityTrend = analyzeTrend(steps.map { it.toFloat() }),
            moodTrend = analyzeTrend(moods),
            overallTrend = calculateOverallTrend(metrics)
        )
    }

    private fun analyzeTrend(values: List<Float>): String {
        if (values.size < 2) return "Stable"

        val first = values.take(values.size / 2).average()
        val second = values.drop(values.size / 2).average()

        return when {
            second > first * 1.1 -> "Improving"
            second < first * 0.9 -> "Declining"
            else -> "Stable"
        }
    }

    private fun calculateOverallTrend(metrics: List<HealthMetrics>): String {
        val recentMetrics = metrics.takeLast(3)
        val criticalCount = recentMetrics.count { it.alertLevel == "CRITICAL" }
        val highCount = recentMetrics.count { it.alertLevel == "HIGH" }

        return when {
            criticalCount > 0 -> "Critical - Immediate attention needed"
            highCount > 1 -> "Concerning - Monitor closely"
            else -> "Good - Continue current care"
        }
    }

    suspend fun getLatestMetricsForPet(petId: String): Flow<HealthMetrics?> =
        getLatestMetricsFlow(petId)

    suspend fun getLatestMetricsForPetSync(petId: String): HealthMetrics? =
        getLatestMetrics(petId)

    private fun updateHealthMetricsFlow() {
        _healthMetricsFlow.value = healthMetrics.values.toList()
    }

    // Data classes for query results
    data class HealthAverages(
        val avgHeartRate: Double?,
        val avgTemperature: Double?,
        val avgSteps: Double?,
        val avgSleep: Double?,
        val avgMood: Double?
    )

    data class DailyActivitySummary(
        val date: String,
        val totalSteps: Int,
        val avgActiveMinutes: Double,
        val totalCalories: Int
    )

    data class StepsStatistics(
        val avgSteps: Double,
        val maxSteps: Int,
        val minSteps: Int,
        val recordCount: Int
    )

    data class SleepAnalysis(
        val avgSleepHours: Double,
        val avgDeepSleep: Double,
        val avgLightSleep: Double,
        val avgDisturbances: Double,
        val sleepQuality: String,
        val nights: Int
    )

    data class MoodAnalysis(
        val moodType: String,
        val avgScore: Double,
        val frequency: Int,
        val avgStress: String
    )

    data class HealthTrendAnalysis(
        val heartRateTrend: String,
        val activityTrend: String,
        val moodTrend: String,
        val overallTrend: String
    )
}