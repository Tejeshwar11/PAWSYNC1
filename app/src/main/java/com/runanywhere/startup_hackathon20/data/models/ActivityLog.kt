package com.runanywhere.startup_hackathon20.data.models

import java.util.*

data class ActivityLog(
    val id: String = UUID.randomUUID().toString(),
    val petId: String,
    val timestamp: Long = System.currentTimeMillis(),

    // Activity Details
    val activityType: String = ActivityType.WALK.name, // WALK, RUN, PLAY, TRAINING, etc.
    val duration: Int, // in minutes
    val intensity: String = ActivityIntensity.MODERATE.name, // LOW, MODERATE, HIGH
    val caloriesBurned: Int = 0,
    val distanceKm: Float = 0f,

    // Location and Environment
    val location: String? = null, // Park, Home, Beach, etc.
    val weatherConditions: String? = null,
    val temperature: Float? = null, // in Celsius

    // Performance Metrics
    val averageHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val stepsCount: Int = 0,

    // Social and Training
    val socialInteraction: Boolean = false, // With other pets/people
    val trainingFocus: List<String> = emptyList(), // Commands, tricks, behaviors
    val behaviorNotes: String? = null,

    // Equipment and Setup
    val equipmentUsed: List<String> = emptyList(), // Leash, toys, treats, etc.
    val surfaceType: String? = null, // Grass, concrete, sand, etc.

    // Monitoring and Health
    val moodBefore: Float = 5f, // 1-10 scale
    val moodAfter: Float = 7f, // 1-10 scale
    val energyLevelBefore: String = EnergyLevel.NORMAL.name,
    val energyLevelAfter: String = EnergyLevel.TIRED.name,

    // External Factors
    val companions: List<String> = emptyList(), // Other pets, people involved
    val challenges: String? = null, // Any difficulties encountered
    val achievements: String? = null, // Milestones, successes

    // Data Quality
    val dataSource: String = DataSource.USER_INPUT.name, // USER_INPUT, DEVICE_SENSOR, GPS_TRACKER
    val accuracy: Float = 0.95f, // Confidence in data accuracy (0-1)
    val isVerified: Boolean = false,

    // Follow-up and Planning
    val plannedFollowUp: String? = null,
    val nextGoals: List<String> = emptyList(),

    // Rating and Feedback
    val userRating: Int? = null, // 1-5 stars for the activity session
    val petEnjoyment: Int? = null, // 1-10 scale for pet's enjoyment
    val notes: String? = null,

    // Metadata
    val createdBy: String? = null, // User who logged the activity
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = false, // For community sharing
    val isFavorite: Boolean = false
)

enum class ActivityType {
    WALK, RUN, HIKE, PLAY, TRAINING, SWIMMING, CYCLING, FETCH,
    TUG_OF_WAR, AGILITY, SOCIALIZATION, GROOMING, FEEDING,
    VET_VISIT, MEDITATION, INDOOR_PLAY, OUTDOOR_EXPLORATION, OTHER
}

enum class ActivityIntensity {
    VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH
}

enum class EnergyLevel {
    VERY_LOW, LOW, NORMAL, HIGH, VERY_HIGH, TIRED
}

enum class DataSource {
    USER_INPUT, DEVICE_SENSOR, GPS_TRACKER
}

// Data class for activity summaries
data class ActivitySummary(
    val totalActivities: Int,
    val totalDuration: Long, // in minutes
    val totalDistance: Float, // in km
    val totalCalories: Int,
    val averageIntensity: String,
    val mostCommonActivity: String,
    val achievementsCount: Int,
    val streakDays: Int,
    val dateRange: Pair<Long, Long>
)

// Data class for activity goals
data class ActivityGoal(
    val goalId: String = UUID.randomUUID().toString(),
    val petId: String,
    val goalType: String, // DAILY_STEPS, WEEKLY_DISTANCE, TRAINING_SESSIONS
    val targetValue: Float,
    val currentValue: Float = 0f,
    val unit: String, // "steps", "km", "minutes", "sessions"
    val deadline: Long?,
    val isActive: Boolean = true,
    val reward: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)