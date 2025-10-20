package com.runanywhere.startup_hackathon20.data.repositories

import com.runanywhere.startup_hackathon20.data.models.ActivityLog
import com.runanywhere.startup_hackathon20.data.models.ActivityType
import com.runanywhere.startup_hackathon20.data.models.ActivityIntensity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.*

class ActivityRepository {

    private val _activities = MutableStateFlow<List<ActivityLog>>(emptyList())
    private val activities: Flow<List<ActivityLog>> = _activities

    // Basic CRUD Operations
    suspend fun insertActivity(activity: ActivityLog): Long {
        val currentList = _activities.value.toMutableList()
        currentList.add(activity)
        _activities.value = currentList
        return activity.timestamp
    }

    suspend fun updateActivity(activity: ActivityLog) {
        val currentList = _activities.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == activity.id }
        if (index != -1) {
            currentList[index] = activity
            _activities.value = currentList
        }
    }

    suspend fun deleteActivity(activity: ActivityLog) {
        val currentList = _activities.value.toMutableList()
        currentList.removeAll { it.id == activity.id }
        _activities.value = currentList
    }

    // Retrieve Activities
    fun getAllActivitiesForPet(petId: String): Flow<List<ActivityLog>> =
        activities.map { list -> list.filter { it.petId == petId } }

    fun getRecentActivities(petId: String, limit: Int = 20): Flow<List<ActivityLog>> =
        activities.map { list ->
        list.filter { it.petId == petId }
                .sortedByDescending { it.timestamp }
                .take(limit)
        }

    suspend fun getActivityById(logId: String): ActivityLog? =
        _activities.value.find { it.id == logId }

    fun getActivitiesInDateRange(
        petId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<ActivityLog>> =
        activities.map { list ->
            list.filter {
                it.petId == petId && it.timestamp >= startTime && it.timestamp <= endTime
            }
        }

    // Activity Type Filtering
    fun getActivitiesByType(petId: String, type: String): Flow<List<ActivityLog>> =
        activities.map { list ->
        list.filter { it.petId == petId && it.activityType == type }
        }

    suspend fun getActivityTypeDistribution(petId: String): Map<String, Int> {
        return _activities.value
            .filter { it.petId == petId }
            .groupBy { it.activityType }
            .mapValues { it.value.size }
    }

    // Performance and Goals
    fun getGoalAchievements(petId: String): Flow<List<ActivityLog>> =
        activities.map { list ->
        list.filter { it.petId == petId && it.achievements != null }
        }

    fun getPersonalRecords(petId: String): Flow<List<ActivityLog>> =
        activities.map { list ->
        list.filter { it.petId == petId && it.isFavorite }
        }

    suspend fun getAchievementStats(petId: String, days: Int = 30): AchievementStats {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val recentActivities = _activities.value.filter {
            it.petId == petId && it.timestamp >= cutoffTime
        }

        return AchievementStats(
            totalActivities = recentActivities.size,
            goalsAchieved = recentActivities.count { it.achievements != null },
            personalRecords = recentActivities.count { it.isFavorite }
        )
    }

    // Statistics
    suspend fun getOverallActivityStats(petId: String, days: Int = 30): ActivityStats {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val recentActivities = _activities.value.filter {
            it.petId == petId && it.timestamp >= cutoffTime
        }

        return ActivityStats(
            totalDuration = recentActivities.sumOf { it.duration.toLong() },
            totalCalories = recentActivities.sumOf { it.caloriesBurned },
            totalDistance = recentActivities.sumOf { it.distanceKm.toDouble() }.toFloat(),
            avgRating = if (recentActivities.isNotEmpty()) {
                recentActivities.mapNotNull { it.userRating }.average()
            } else 0.0,
            totalActivities = recentActivities.size
        )
    }

    suspend fun getPersonalBests(petId: String): PersonalBests {
        val userActivities = _activities.value.filter { it.petId == petId }

        return PersonalBests(
            longestDuration = userActivities.maxOfOrNull { it.duration.toLong() } ?: 0L,
            longestDistance = userActivities.maxOfOrNull { it.distanceKm } ?: 0f,
            mostCalories = userActivities.maxOfOrNull { it.caloriesBurned } ?: 0,
            bestRating = userActivities.mapNotNull { it.userRating }.maxOrNull() ?: 0
        )
    }

    // Business Logic Methods
    suspend fun logActivity(
        petId: String,
        activityType: ActivityType,
        duration: Int,
        intensity: ActivityIntensity = ActivityIntensity.MODERATE,
        distanceKm: Float = 0f,
        caloriesBurned: Int = 0,
        notes: String? = null,
        location: String? = null
    ): Long {
        val activity = ActivityLog(
            id = UUID.randomUUID().toString(),
            petId = petId,
            activityType = activityType.name,
            duration = duration,
            intensity = intensity.name,
            distanceKm = distanceKm,
            caloriesBurned = caloriesBurned,
            notes = notes,
            location = location,
            timestamp = System.currentTimeMillis()
        )
        return insertActivity(activity)
    }

    suspend fun getDailyActivitySummary(petId: String, days: Int = 7): List<DailySummary> {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        val recentActivities = _activities.value.filter {
            it.petId == petId && it.timestamp >= cutoffTime
        }

        return recentActivities.groupBy {
            // Group by day
            val dayStart = it.timestamp - (it.timestamp % (24 * 60 * 60 * 1000L))
            dayStart
        }.map { (day, activitiesList) ->
            DailySummary(
                date = day,
                totalActivities = activitiesList.size,
                totalDuration = activitiesList.sumOf { it.duration.toLong() },
                totalCalories = activitiesList.sumOf { it.caloriesBurned ?: 0 },
                totalDistance = activitiesList.sumOf { (it.distanceKm ?: 0f).toDouble() }.toFloat()
            )
        }.sortedByDescending { it.date }
    }

    suspend fun getWeeklyStats(petId: String): WeeklyActivityStats {
        val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val weekActivities = _activities.value.filter {
            it.petId == petId && it.timestamp >= weekStart
        }
        val stats = getOverallActivityStats(petId, 7)

        return WeeklyActivityStats(
            totalActivities = weekActivities.size,
            totalDuration = stats.totalDuration,
            totalCalories = stats.totalCalories,
            totalDistance = stats.totalDistance,
            averageRating = stats.avgRating
        )
    }

    // Search functionality
    fun searchActivities(petId: String, query: String): Flow<List<ActivityLog>> =
        activities.map { list ->
            list.filter {
                it.petId == petId &&
                        (it.activityType.contains(query, ignoreCase = true) ||
                                it.notes?.contains(query, ignoreCase = true) == true ||
                                it.location?.contains(query, ignoreCase = true) == true)
            }
        }
}

data class WeeklyActivityStats(
    val totalActivities: Int,
    val totalDuration: Long,
    val totalCalories: Int,
    val totalDistance: Float,
    val averageRating: Double
)

data class ActivityStats(
    val totalDuration: Long,
    val totalCalories: Int,
    val totalDistance: Float,
    val avgRating: Double,
    val totalActivities: Int
)

data class AchievementStats(
    val totalActivities: Int,
    val goalsAchieved: Int,
    val personalRecords: Int
)

data class PersonalBests(
    val longestDuration: Long,
    val longestDistance: Float,
    val mostCalories: Int,
    val bestRating: Int
)

data class DailySummary(
    val date: Long,
    val totalActivities: Int,
    val totalDuration: Long,
    val totalCalories: Int,
    val totalDistance: Float
)