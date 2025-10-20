package com.runanywhere.startup_hackathon20.data.models

import java.util.*

data class HealthMetrics(
    val id: String = UUID.randomUUID().toString(),
    val petId: String,
    val timestamp: Long = System.currentTimeMillis(),

    // Vital Signs
    val heartRate: Int? = null, // BPM (60-140 normal for dogs)
    val bodyTemperature: Float? = null, // Celsius (37.5-39.0 normal)
    val respiratoryRate: Int? = null, // breaths per minute (10-35 normal)

    // Activity Metrics
    val stepsToday: Int = 0,
    val stepsGoal: Int = 10000,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val activeMinutes: Int = 0,
    val activityLevel: String = ActivityLevel.MODERATE.name, // SEDENTARY, LIGHT, MODERATE, ACTIVE

    // Hydration & Nutrition
    val hydrationMl: Int = 0,
    val hydrationGoalMl: Int = 1000,
    val lastDrinkTime: Long? = null,
    val mealsToday: Int = 0,
    val lastMealTime: Long? = null,

    // Sleep Data
    val sleepHours: Float = 0f,
    val sleepQuality: String = SleepQuality.GOOD.name, // POOR, FAIR, GOOD, EXCELLENT
    val deepSleepHours: Float = 0f,
    val lightSleepHours: Float = 0f,
    val remSleepHours: Float = 0f,
    val sleepDisturbances: Int = 0,

    // Mood & Behavior
    val moodScore: Float = 7.5f, // 0-10 scale
    val moodType: String = MoodType.HAPPY.name,
    val stressLevel: String = StressLevel.LOW.name, // LOW, MEDIUM, HIGH
    val stressIndicators: List<String> = emptyList(),
    val behavioralNotes: String? = null,

    // Environmental
    val weatherCondition: String? = null,
    val outdoorTimeMinutes: Int = 0,
    val indoorTimeMinutes: Int = 0,

    // Device Data
    val deviceId: String? = null,
    val batteryLevel: Int? = null,
    val signalStrength: Int? = null,
    val lastSyncTime: Long? = null,

    // Health Flags
    val isAbnormal: Boolean = false,
    val alertLevel: String = "NORMAL", // NORMAL, LOW, MEDIUM, HIGH, CRITICAL
    val notes: String? = null
)

enum class ActivityLevel {
    SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE
}

enum class SleepQuality {
    POOR, FAIR, GOOD, EXCELLENT
}

enum class MoodType {
    HAPPY, ANXIOUS, DEPRESSED, AGGRESSIVE, PLAYFUL, TIRED, EXCITED, CALM
}

enum class StressLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

