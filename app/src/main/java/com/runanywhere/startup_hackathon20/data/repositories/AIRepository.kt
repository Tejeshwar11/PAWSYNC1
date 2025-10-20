package com.runanywhere.startup_hackathon20.data.repositories

import com.runanywhere.startup_hackathon20.data.models.*
import com.runanywhere.sdk.public.RunAnywhere
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class AIRepository {

    // In-memory storage
    private val insights = ConcurrentHashMap<String, AIInsight>()
    private val _insightsFlow = MutableStateFlow<List<AIInsight>>(emptyList())

    // Basic CRUD Operations
    suspend fun saveInsight(insight: AIInsight): Long {
        insights[insight.insightId] = insight
        updateInsightsFlow()
        return System.currentTimeMillis()
    }

    suspend fun getInsightById(insightId: String): AIInsight? = insights[insightId]

    fun getLatestInsightFlow(petId: String): Flow<AIInsight?> =
        _insightsFlow.asStateFlow().map { insightsList ->
            insightsList.filter { it.petId == petId }.maxByOrNull { it.timestamp }
        }

    fun getRecentInsights(petId: String, limit: Int = 10): Flow<List<AIInsight>> =
        _insightsFlow.asStateFlow().map { insightsList ->
            insightsList.filter { it.petId == petId }
                .sortedByDescending { it.timestamp }
                .take(limit)
        }

    // AI Insight Generation
    suspend fun generateHealthInsight(request: InsightRequest): AIInsight {
        val startTime = System.currentTimeMillis()

        try {
            // Build comprehensive prompt for AI
            val prompt = buildHealthAnalysisPrompt(request)

            // Generate AI response using RunAnywhere SDK
            val aiResponse = RunAnywhere.generate(prompt)

            val processingTime = System.currentTimeMillis() - startTime

            // Parse AI response and create insight
            return parseAIResponse(request, aiResponse, processingTime)

        } catch (e: Exception) {
            // Fallback insight if AI fails
            return createFallbackInsight(request, e.message ?: "AI generation failed")
        }
    }

    fun generateHealthInsightStream(request: InsightRequest): Flow<String> = flow {
        val prompt = buildHealthAnalysisPrompt(request)

        try {
            RunAnywhere.generateStream(prompt).collect { token ->
                emit(token)
            }
        } catch (e: Exception) {
            emit("Error generating AI insight: ${e.message}")
        }
    }

    private fun buildHealthAnalysisPrompt(request: InsightRequest): String {
        val pet = request.petProfile
        val metrics = request.healthMetrics

        return """
            You are PAWSYNC AI Doctor, an expert veterinary AI assistant specializing in pet health analysis.
            
            Pet Profile:
            - Name: ${pet.name}
            - Species: ${pet.species}
            - Breed: ${pet.breed}
            - Age: ${pet.age} years
            - Weight: ${pet.weight} kg
            - Gender: ${pet.gender}
            - Medical History: ${pet.medicalHistory.joinToString(", ").ifEmpty { "None reported" }}
            - Allergies: ${pet.allergies.joinToString(", ").ifEmpty { "None reported" }}
            
            Current Health Metrics:
            - Heart Rate: ${metrics.heartRate ?: "Not measured"} BPM
            - Body Temperature: ${metrics.bodyTemperature ?: "Not measured"}°C
            - Activity Today: ${metrics.stepsToday}/${metrics.stepsGoal} steps
            - Sleep: ${metrics.sleepHours} hours (${metrics.sleepQuality})
            - Mood Score: ${metrics.moodScore}/10 (${metrics.moodType})
            - Hydration: ${metrics.hydrationMl}/${metrics.hydrationGoalMl} ml
            - Stress Level: ${metrics.stressLevel}
            - Activity Level: ${metrics.activityLevel}
            
            Analysis Request: ${request.requestType}
            ${if (request.specificConcerns != null) "Specific Concerns: ${request.specificConcerns}" else ""}
            
            Please provide a comprehensive health analysis including:
            1. Overall Health Status (EXCELLENT/GOOD/CONCERNING/CRITICAL)
            2. Key findings and observations
            3. Specific recommendations for improvement
            4. Diet and exercise suggestions based on breed and age
            5. Any red flags requiring veterinary attention
            6. Preventive care suggestions
            
            Format your response as a structured analysis with clear sections.
            Use a caring, professional tone appropriate for pet owners.
            If any metrics indicate concerning values, clearly flag them.
            
            Response:
        """.trimIndent()
    }

    private fun parseAIResponse(
        request: InsightRequest,
        aiResponse: String,
        processingTime: Long
    ): AIInsight {
        // Simple parsing - in a real app, you'd use more sophisticated NLP
        val healthStatus = when {
            aiResponse.contains("CRITICAL", ignoreCase = true) -> HealthStatus.CRITICAL
            aiResponse.contains("CONCERNING", ignoreCase = true) -> HealthStatus.CONCERNING
            aiResponse.contains("EXCELLENT", ignoreCase = true) -> HealthStatus.EXCELLENT
            else -> HealthStatus.GOOD
        }

        val alertLevel = when (healthStatus) {
            HealthStatus.CRITICAL -> "CRITICAL"
            HealthStatus.CONCERNING -> "HIGH"
            HealthStatus.GOOD -> "NORMAL"
            HealthStatus.EXCELLENT -> "NORMAL"
            else -> "NORMAL"
        }

        val vetConsultRequired = aiResponse.contains("veterinary", ignoreCase = true) ||
                aiResponse.contains("vet visit", ignoreCase = true) ||
                healthStatus == HealthStatus.CRITICAL

        // Extract recommendations (simple approach)
        val recommendations = extractRecommendations(aiResponse)

        return AIInsight(
            petId = request.petProfile.id,
            healthStatus = healthStatus.name,
            summary = aiResponse.take(200) + if (aiResponse.length > 200) "..." else "",
            detailedAnalysis = aiResponse,
            recommendations = recommendations,
            alertLevel = alertLevel,
            veterinaryConsultRequired = vetConsultRequired,
            aiModelUsed = "PawSync AI Doctor",
            confidenceScore = 0.85f,
            processingTimeMs = processingTime,
            category = InsightCategory.GENERAL_HEALTH.name
        )
    }

    private fun extractRecommendations(aiResponse: String): List<String> {
        val recommendations = mutableListOf<String>()

        // Simple keyword-based extraction
        val lines = aiResponse.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("-") || trimmed.startsWith("•") ||
                trimmed.contains("recommend", ignoreCase = true) ||
                trimmed.contains("suggest", ignoreCase = true)
            ) {
                recommendations.add(trimmed.removePrefix("-").removePrefix("•").trim())
            }
        }

        return recommendations.take(10) // Limit to 10 recommendations
    }

    private fun createFallbackInsight(request: InsightRequest, errorMessage: String): AIInsight {
        val metrics = request.healthMetrics
        val pet = request.petProfile

        // Basic rule-based analysis as fallback
        val healthStatus = when {
            metrics.heartRate?.let { it < 60 || it > 140 } == true -> HealthStatus.CONCERNING
            metrics.bodyTemperature?.let { it < 37.5f || it > 39.0f } == true -> HealthStatus.CONCERNING
            metrics.moodScore < 5.0f -> HealthStatus.CONCERNING
            else -> HealthStatus.GOOD
        }

        val basicAnalysis = """
            Basic Health Assessment for ${pet.name}:
            
            Current Status: ${healthStatus.name}
            
            Key Observations:
            - Heart Rate: ${metrics.heartRate ?: "Not measured"} BPM
            - Temperature: ${metrics.bodyTemperature ?: "Not measured"}°C
            - Activity: ${metrics.stepsToday} steps today
            - Mood: ${metrics.moodScore}/10
            
            General Recommendations:
            - Maintain regular exercise routine
            - Ensure adequate hydration
            - Monitor vital signs regularly
            - Continue current diet unless otherwise advised
            
            Note: This is a basic assessment. For detailed analysis, please ensure AI services are available.
        """.trimIndent()

        return AIInsight(
            petId = pet.id,
            healthStatus = healthStatus.name,
            summary = "Basic health assessment completed. ${pet.name} appears to be in ${healthStatus.name.lowercase()} health.",
            detailedAnalysis = basicAnalysis,
            recommendations = listOf(
                "Maintain regular exercise",
                "Monitor vital signs",
                "Ensure adequate hydration"
            ),
            alertLevel = if (healthStatus == HealthStatus.CONCERNING) "MEDIUM" else "NORMAL",
            veterinaryConsultRequired = healthStatus == HealthStatus.CONCERNING,
            aiModelUsed = "Fallback Analysis",
            confidenceScore = 0.60f,
            processingTimeMs = 100,
            category = InsightCategory.GENERAL_HEALTH.name
        )
    }

    private fun updateInsightsFlow() {
        _insightsFlow.value = insights.values.toList()
    }

    // Analytics and Statistics
    suspend fun getInsightStatistics(petId: String, days: Int = 30) =
        insights.values.filter { it.petId == petId && it.timestamp > System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L) }

    suspend fun getHealthStatusDistribution(petId: String, days: Int = 30) =
        insights.values.filter { it.petId == petId && it.timestamp > System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L) }
            .groupBy { it.healthStatus }
            .mapValues { it.value.size }

    fun getCriticalInsights(petId: String): Flow<List<AIInsight>> =
        _insightsFlow.asStateFlow().map { insightsList ->
            insightsList.filter { it.petId == petId && it.alertLevel == "CRITICAL" }
        }

    fun getInsightsRequiringVetConsult(petId: String): Flow<List<AIInsight>> =
        _insightsFlow.asStateFlow().map { insightsList ->
            insightsList.filter { it.petId == petId && it.veterinaryConsultRequired }
        }

    // User Feedback
    suspend fun updateUserFeedback(insightId: String, rating: Int?, feedback: String?) {
        val insight = insights[insightId]
        if (insight != null) {
            val updatedInsight = insight.copy(
                userRating = rating,
                userFeedback = feedback
            )
            insights[insightId] = updatedInsight
            updateInsightsFlow()
        }
    }

    suspend fun bookmarkInsight(insightId: String, bookmarked: Boolean) {
        val insight = insights[insightId]
        if (insight != null) {
            val updatedInsight = insight.copy(isBookmarked = bookmarked)
            insights[insightId] = updatedInsight
            updateInsightsFlow()
        }
    }
}