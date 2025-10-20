package com.runanywhere.startup_hackathon20.data.models

import java.util.*

data class AIInsight(
    val insightId: String = UUID.randomUUID().toString(),
    val petId: String,
    val timestamp: Long = System.currentTimeMillis(),

    // Health Assessment
    val healthStatus: String = HealthStatus.GOOD.name, // EXCELLENT, GOOD, CONCERNING, CRITICAL
    val healthScore: Float = 85f, // 0-100 overall health score
    val summary: String, // Brief 2-3 sentence summary
    val detailedAnalysis: String, // Comprehensive analysis

    // Recommendations
    val recommendations: List<String> = emptyList(),
    val dietPlan: String? = null,
    val exercisePlan: String? = null,
    val behavioralTips: List<String> = emptyList(),

    // Medical Insights
    val symptomsDetected: List<String> = emptyList(),
    val riskFactors: List<String> = emptyList(),
    val preventiveMeasures: List<String> = emptyList(),

    // Alerts & Actions
    val alertLevel: String = AlertLevel.NORMAL.name,
    val veterinaryConsultRequired: Boolean = false,
    val urgencyLevel: String = UrgencyLevel.ROUTINE.name, // ROUTINE, SOON, URGENT, EMERGENCY
    val nextCheckupDate: Long? = null,

    // AI Model Info
    val aiModelUsed: String? = null,
    val confidenceScore: Float = 0.85f, // 0-1 confidence in analysis
    val processingTimeMs: Long = 0,

    // Insights Categories
    val category: String = InsightCategory.GENERAL_HEALTH.name,
    val tags: List<String> = emptyList(),

    // User Interaction
    val userRating: Int? = null, // 1-5 stars user feedback
    val userFeedback: String? = null,
    val isBookmarked: Boolean = false,
    val isShared: Boolean = false,

    // Follow-up
    val hasFollowUp: Boolean = false,
    val followUpDate: Long? = null,
    val followUpNotes: String? = null
)

enum class HealthStatus {
    EXCELLENT, GOOD, FAIR, CONCERNING, CRITICAL
}

enum class AlertLevel {
    NORMAL, CAUTION, WARNING, CRITICAL
}

enum class UrgencyLevel {
    ROUTINE, SOON, URGENT, EMERGENCY
}

enum class InsightCategory {
    GENERAL_HEALTH, NUTRITION, EXERCISE, BEHAVIOR, MEDICAL, PREVENTIVE, EMERGENCY
}

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Data class for AI insight generation request
data class InsightRequest(
    val petProfile: PetProfile,
    val healthMetrics: HealthMetrics,
    val recentSymptoms: List<String> = emptyList(),
    val specificConcerns: String? = null,
    val requestType: String = "GENERAL_CHECKUP"
)

// Data class for AI insight with recommendations
data class AIRecommendation(
    val type: String, // DIET, EXERCISE, MEDICAL, BEHAVIORAL
    val title: String,
    val description: String,
    val priority: Priority, // LOW, MEDIUM, HIGH, CRITICAL
    val actionRequired: Boolean = false,
    val estimatedDuration: String? = null, // "2 weeks", "daily", etc.
    val resources: List<String> = emptyList() // URLs, guides, etc.
)