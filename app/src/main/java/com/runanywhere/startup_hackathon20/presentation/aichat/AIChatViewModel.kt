package com.runanywhere.startup_hackathon20.presentation.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.PetProfile
import com.runanywhere.startup_hackathon20.data.models.HealthMetrics
import com.runanywhere.startup_hackathon20.data.repositories.PetRepository
import com.runanywhere.startup_hackathon20.data.repositories.HealthRepository
import com.runanywhere.startup_hackathon20.data.repositories.AIRepository
import com.runanywhere.startup_hackathon20.ChatViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT, HEALTH_ALERT, RECOMMENDATION, CODE_BLOCK
}

data class QuickQuestion(
    val text: String,
    val prompt: String,
    val icon: String
)

class AIChatViewModel(
    private val petRepository: PetRepository,
    private val healthRepository: HealthRepository,
    private val aiRepository: AIRepository,
    private val chatViewModel: ChatViewModel
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPet = MutableStateFlow<PetProfile?>(null)
    val currentPet: StateFlow<PetProfile?> = _currentPet.asStateFlow()

    private val _latestMetrics = MutableStateFlow<HealthMetrics?>(null)
    val latestMetrics: StateFlow<HealthMetrics?> = _latestMetrics.asStateFlow()

    private val _quickQuestions = MutableStateFlow(getQuickQuestions())
    val quickQuestions: StateFlow<List<QuickQuestion>> = _quickQuestions.asStateFlow()

    init {
        loadPetAndHealthData()
        addWelcomeMessage()
    }

    private fun loadPetAndHealthData() {
        viewModelScope.launch {
            try {
                // Load demo pet data
                val pets = petRepository.getAllPetsList()
                _currentPet.value = pets.firstOrNull()

                _currentPet.value?.let { pet ->
                    val metrics = healthRepository.getLatestMetricsForPetSync(pet.id)
                    _latestMetrics.value = metrics
                }
            } catch (e: Exception) {
                // Create demo data if no pets exist
                createDemoData()
            }
        }
    }

    private suspend fun createDemoData() {
        // Create demo pet similar to dashboard
        val demoPet = PetProfile(
            name = "Max",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            weight = 25.0f,
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
        _currentPet.value = demoPet

        // Create demo health metrics
        val demoMetrics = HealthMetrics(
            petId = demoPet.id,
            heartRate = 85,
            stepsToday = 7500,
            stepsGoal = 10000,
            hydrationMl = 800,
            hydrationGoalMl = 1000,
            sleepHours = 9.2f,
            sleepQuality = "GOOD",
            bodyTemperature = 38.5f,
            respiratoryRate = 18,
            activityLevel = "MODERATE",
            moodScore = 8.0f,
            stressIndicators = listOf("None")
        )

        healthRepository.insertHealthMetrics(demoMetrics)
        _latestMetrics.value = demoMetrics
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            content = "🐾 Hello! I'm your AI Pet Doctor. I'm here to help with ${_currentPet.value?.name ?: "your pet"}'s health and wellness. What would you like to know?",
            isFromUser = false,
            messageType = MessageType.TEXT
        )
        _messages.value = listOf(welcomeMessage)
    }

    fun updateInput(input: String) {
        if (input.length <= 500) {
            _currentInput.value = input
        }
    }

    fun sendMessage(message: String = _currentInput.value) {
        if (message.isBlank()) return

        val userMessage = ChatMessage(
            content = message,
            isFromUser = true
        )

        _messages.value = _messages.value + userMessage
        _currentInput.value = ""
        _isLoading.value = true

        // Generate AI response
        generateAIResponse(message)
    }

    fun sendQuickQuestion(question: QuickQuestion) {
        sendMessage(question.prompt)
    }

    private fun generateAIResponse(userMessage: String) {
        viewModelScope.launch {
            try {
                // Create context-aware prompt
                val contextPrompt = buildContextualPrompt(userMessage)

                // Simulate streaming response using existing chat functionality
                val aiResponse = generateMockAIResponse(userMessage)

                val aiMessage = ChatMessage(
                    content = aiResponse,
                    isFromUser = false,
                    messageType = detectMessageType(aiResponse)
                )

                _messages.value = _messages.value + aiMessage

            } catch (e: Exception) {
                handleAIError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun generateMockAIResponse(userMessage: String): String {
        val pet = _currentPet.value
        val metrics = _latestMetrics.value

        return when {
            userMessage.contains("health", ignoreCase = true) -> {
                buildString {
                    append("🏥 Based on ${pet?.name ?: "your pet"}'s current metrics, here's the health assessment:\n\n")
                    if (metrics != null) {
                        val heartRate = metrics.heartRate ?: 0
                        append("❤️ **Heart Rate**: $heartRate BPM - ${getHeartRateStatus(heartRate)}\n")
                        append("🏃 **Activity**: ${metrics.stepsToday}/${metrics.stepsGoal} steps (${(metrics.stepsToday.toFloat() / metrics.stepsGoal * 100).toInt()}%)\n")
                        append("😊 **Mood Score**: ${metrics.moodScore}/10 - ${getMoodStatus(metrics.moodScore.toInt())}\n")
                        append("😴 **Sleep Quality**: ${metrics.sleepQuality} (${metrics.sleepHours} hours)\n")
                        append("🌡️ **Temperature**: ${metrics.bodyTemperature}°C - Normal range\n\n")
                        append(
                            "**Overall Assessment**: ${pet?.name ?: "Your pet"} appears to be in ${
                                getOverallHealthStatus(
                                    metrics
                                )
                            } condition! 🐾"
                        )
                    } else {
                        append("I'd love to give you a detailed assessment, but I need some current health data first. Please check the dashboard for the latest metrics! 📊")
                    }
                }
            }

            userMessage.contains("exercise", ignoreCase = true) || userMessage.contains(
                "activity",
                ignoreCase = true
            ) -> {
                buildString {
                    append("🏃 **Exercise Recommendations for ${pet?.name ?: "your pet"}**:\n\n")
                    if (pet != null) {
                        when (pet.breed.lowercase()) {
                            "golden retriever", "labrador" -> {
                                append("🎾 **High Energy Breed Plan**:\n")
                                append("• 60-90 minutes daily exercise\n")
                                append("• Morning walk: 30 minutes\n")
                                append("• Afternoon play session: 20 minutes\n")
                                append("• Evening walk: 30 minutes\n")
                                append("• Swimming (great for joints!)\n")
                                append("• Fetch games for mental stimulation\n\n")
                            }

                            else -> {
                                append("🚶 **Balanced Exercise Plan**:\n")
                                append("• 45-60 minutes daily exercise\n")
                                append("• Two 20-minute walks\n")
                                append("• 15 minutes of play time\n")
                                append("• Mental stimulation activities\n\n")
                            }
                        }
                        append(
                            "**Age Consideration**: At ${pet.age} years old, ${pet.name} is in their ${
                                getAgeCategory(
                                    pet.age
                                )
                            } phase."
                        )
                    }
                    append("\n\n💡 **Pro Tip**: Always monitor for signs of fatigue and adjust intensity based on weather conditions! 🌤️")
                }
            }

            userMessage.contains("diet", ignoreCase = true) || userMessage.contains(
                "food",
                ignoreCase = true
            ) -> {
                buildString {
                    append("🍖 **Nutrition Plan for ${pet?.name ?: "your pet"}**:\n\n")
                    if (pet != null) {
                        val dailyCalories = calculateDailyCalories(pet.weight.toDouble(), pet.age)
                        append("📊 **Daily Requirements**:\n")
                        append("• Calories: ${dailyCalories} kcal/day\n")
                        append("• Protein: ${(pet.weight * 2.5).toInt()}g\n")
                        append("• Fresh water: ${(pet.weight * 50).toInt()}ml\n\n")

                        append("🕐 **Feeding Schedule**:\n")
                        append("• Morning: 40% of daily food (7:00 AM)\n")
                        append("• Evening: 60% of daily food (6:00 PM)\n\n")

                        append("✅ **Recommended Foods**:\n")
                        append("• High-quality protein (chicken, fish, lamb)\n")
                        append("• Complex carbohydrates (sweet potato, rice)\n")
                        append("• Healthy fats (salmon oil, flaxseed)\n")
                        append("• Fresh vegetables (carrots, green beans)\n\n")

                        append("❌ **Avoid**: Chocolate, grapes, onions, garlic, excessive treats")
                    }
                    append("\n\n💡 **Tip**: Monitor weight weekly and adjust portions as needed! ⚖️")
                }
            }

            userMessage.contains("emergency", ignoreCase = true) || userMessage.contains(
                "urgent",
                ignoreCase = true
            ) -> {
                "🚨 **EMERGENCY PROTOCOLS** 🚨\n\n" +
                        "**Immediate Actions:**\n" +
                        "1. Stay calm and assess the situation\n" +
                        "2. Contact your veterinarian immediately\n" +
                        "3. If after hours, call the nearest emergency clinic\n\n" +
                        "**Emergency Signs:**\n" +
                        "🔴 Difficulty breathing\n" +
                        "🔴 Severe bleeding\n" +
                        "🔴 Loss of consciousness\n" +
                        "🔴 Repeated vomiting\n" +
                        "🔴 Signs of extreme pain\n\n" +
                        "**Emergency Contacts:**\n" +
                        "📞 Pet Poison Helpline: (855) 764-7661\n" +
                        "🏥 Find nearest emergency vet in the Vet Finder\n\n" +
                        "⚠️ **This is not a substitute for professional veterinary care. Please seek immediate help if this is a true emergency.**"
            }

            else -> {
                "🐾 I'm here to help with ${pet?.name ?: "your pet"}'s health and wellness! I can provide information about:\n\n" +
                        "❤️ Health assessments\n" +
                        "🏃 Exercise recommendations\n" +
                        "🍖 Diet and nutrition advice\n" +
                        "😊 Behavioral guidance\n" +
                        "🩺 Symptom evaluation\n" +
                        "⚠️ Emergency protocols\n\n" +
                        "Feel free to ask me anything specific about ${pet?.name ?: "your pet"}'s care, or use one of the quick question buttons above! What would you like to know more about?"
            }
        }
    }

    private fun getHeartRateStatus(heartRate: Int): String {
        return when {
            heartRate < 60 -> "Low - Monitor closely"
            heartRate <= 100 -> "Normal - Excellent!"
            heartRate <= 120 -> "Slightly elevated"
            else -> "High - Consider vet consultation"
        }
    }

    private fun getMoodStatus(moodScore: Int): String {
        return when {
            moodScore >= 8 -> "Excellent mood!"
            moodScore >= 6 -> "Good mood"
            moodScore >= 4 -> "Neutral mood"
            else -> "Needs attention"
        }
    }

    private fun getOverallHealthStatus(metrics: HealthMetrics): String {
        val scores = listOf(
            if (metrics.heartRate in 60..100) 1 else 0,
            if (metrics.stepsToday >= metrics.stepsGoal * 0.7) 1 else 0,
            if (metrics.moodScore >= 7) 1 else 0,
            if (metrics.sleepHours >= 8) 1 else 0
        )
        val totalScore = scores.sum()

        return when (totalScore) {
            4 -> "excellent"
            3 -> "very good"
            2 -> "good"
            1 -> "fair"
            else -> "needs attention"
        }
    }

    private fun getAgeCategory(age: Int): String {
        return when {
            age <= 1 -> "puppy"
            age <= 7 -> "adult"
            else -> "senior"
        }
    }

    private fun calculateDailyCalories(weight: Double, age: Int): Int {
        val baseCalories = weight * 30 + 70
        val ageMultiplier = when {
            age <= 1 -> 2.0  // Puppies need more calories
            age <= 7 -> 1.0  // Adult maintenance
            else -> 0.8      // Senior dogs need fewer calories
        }
        return (baseCalories * ageMultiplier).toInt()
    }

    private fun buildContextualPrompt(userMessage: String): String {
        val pet = _currentPet.value
        val metrics = _latestMetrics.value

        return buildString {
            append("You are Dr. PawSync, an expert veterinary AI assistant. ")

            if (pet != null) {
                append("You're consulting about ${pet.name}, a ${pet.age}-year-old ${pet.breed} (${pet.gender}). ")
                append("Weight: ${pet.weight}kg. ")
                if (pet.medicalHistory.isNotEmpty()) {
                    append("Medical history: ${pet.medicalHistory.joinToString(", ")}. ")
                }
            }

            if (metrics != null) {
                append("Current health status: ")
                append("Heart rate: ${metrics.heartRate} BPM, ")
                append("Steps today: ${metrics.stepsToday}/${metrics.stepsGoal}, ")
                append("Mood score: ${metrics.moodScore}/10, ")
                append("Sleep quality: ${metrics.sleepQuality}, ")
                append("Activity level: ${metrics.activityLevel}. ")
            }

            append("\nUser question: $userMessage\n\n")
            append("Provide helpful, accurate veterinary advice. If it's a serious medical concern, recommend consulting a veterinarian. ")
            append("Be warm, professional, and use pet-friendly language with emojis where appropriate.")
        }
    }

    private fun detectMessageType(content: String): MessageType {
        return when {
            content.contains("URGENT", ignoreCase = true) ||
                    content.contains("EMERGENCY", ignoreCase = true) -> MessageType.HEALTH_ALERT
            content.contains("Schedule:", ignoreCase = true) ||
                    content.contains("Plan:", ignoreCase = true) -> MessageType.CODE_BLOCK
            content.contains("recommend", ignoreCase = true) ||
                    content.contains("suggest", ignoreCase = true) -> MessageType.RECOMMENDATION
            else -> MessageType.TEXT
        }
    }

    private fun handleAIError(error: Exception) {
        val errorMessage = ChatMessage(
            content = "I'm sorry, I encountered an issue. Please try again or contact support if the problem persists. 🐾",
            isFromUser = false,
            messageType = MessageType.TEXT
        )
        _messages.value = _messages.value + errorMessage
    }

    fun clearChat() {
        _messages.value = emptyList()
        addWelcomeMessage()
    }

    fun exportChat(): String {
        val pet = _currentPet.value
        val chatHistory = _messages.value
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return buildString {
            appendLine("🐾 PAWSYNC - AI Pet Doctor Consultation")
            appendLine("Generated: ${dateFormat.format(Date())}")
            appendLine()

            if (pet != null) {
                appendLine("Pet Information:")
                appendLine("Name: ${pet.name}")
                appendLine("Breed: ${pet.breed}")
                appendLine("Age: ${pet.age} years")
                appendLine("Weight: ${pet.weight}kg")
                appendLine()
            }

            appendLine("Conversation History:")
            appendLine("=" + "=".repeat(50))

            chatHistory.forEach { message ->
                val sender = if (message.isFromUser) "User" else "Dr. PawSync"
                val timestamp = timeFormat.format(Date(message.timestamp))
                appendLine("[$timestamp] $sender:")
                appendLine(message.content)
                appendLine()
            }

            appendLine("End of Consultation")
            appendLine("For emergency situations, please contact your local veterinarian immediately.")
        }
    }

    private fun getQuickQuestions(): List<QuickQuestion> {
        return listOf(
            QuickQuestion(
                text = "Daily Health Check ❤️",
                prompt = "Please give me a daily health assessment for my pet based on their current metrics and activity levels.",
                icon = "❤️"
            ),
            QuickQuestion(
                text = "Exercise Advice 🏃",
                prompt = "What exercise routine would you recommend for my pet based on their breed, age, and current activity level?",
                icon = "🏃"
            ),
            QuickQuestion(
                text = "Diet Recommendations 🍖",
                prompt = "Can you suggest an optimal diet plan for my pet considering their age, weight, and activity level?",
                icon = "🍖"
            ),
            QuickQuestion(
                text = "Mood Analysis 😊",
                prompt = "How is my pet's emotional well-being based on their behavior patterns and current mood score?",
                icon = "😊"
            ),
            QuickQuestion(
                text = "Symptom Checker 🩺",
                prompt = "I'm concerned about some symptoms I've noticed. Can you help me assess if I should see a vet?",
                icon = "🩺"
            ),
            QuickQuestion(
                text = "Emergency Help ⚠️",
                prompt = "This is urgent - I need immediate guidance on what to do for my pet's current situation.",
                icon = "⚠️"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources
    }
}