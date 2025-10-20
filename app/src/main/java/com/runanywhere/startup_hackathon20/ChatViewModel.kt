// ✅ The code is ready to run: Open Android Studio and build from there
// Android Studio has an embedded JDK that will work

package com.runanywhere.startup_hackathon20

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.listAvailableModels
import com.runanywhere.sdk.models.ModelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Pet Health Message Data Class
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    HEALTH_ALERT,
    RECOMMENDATION
}

// Pet Data Classes
data class PetProfile(
    val name: String = "Your Pet",
    val breed: String = "Unknown",
    val age: Int = 0,
    val weight: Float = 0f
)

// PawSync ChatViewModel
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress

    private val _currentModelId = MutableStateFlow<String?>(null)
    val currentModelId: StateFlow<String?> = _currentModelId

    private val _statusMessage = MutableStateFlow<String>("Initializing PawSync AI...")
    val statusMessage: StateFlow<String> = _statusMessage

    // Pet profile state
    private val _petProfile = MutableStateFlow(PetProfile())
    val petProfile: StateFlow<PetProfile> = _petProfile

    // Mock health data for demo
    private val mockHealthData = """
        Current Pet Health Metrics:
        - Heart Rate: 85 BPM (Normal)
        - Activity: 7,543 steps today
        - Hydration: 650ml / 1000ml goal
        - Sleep: 9.2 hours (Good quality)
        - Mood Score: 8.5/10 (Happy & Energetic)
        - Temperature: 38.3°C (Normal)
    """.trimIndent()

    init {
        loadAvailableModels()
        addWelcomeMessage()
    }

    private fun addWelcomeMessage() {
        _messages.value = listOf(
            ChatMessage(
                text = "🐾 Welcome to PAWSYNC - Your AI Pet Health Companion!\n\n" +
                        "I'm your Pet Health AI Doctor. I can help you with:\n" +
                        "• Analyzing your pet's health metrics\n" +
                        "• Providing diet and exercise recommendations\n" +
                        "• Answering pet care questions\n" +
                        "• Detecting early warning signs\n\n" +
                        "Please download and load an AI model to get started!",
                isUser = false
            )
        )
    }

    private fun loadAvailableModels() {
        viewModelScope.launch {
            try {
                val models = listAvailableModels()
                _availableModels.value = models
                if (models.isNotEmpty()) {
                    _statusMessage.value = "Ready - Download a Pet AI model to begin"
                } else {
                    _statusMessage.value = "Initializing AI models..."
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading models: ${e.message}"
            }
        }
    }

    fun downloadModel(modelId: String) {
        viewModelScope.launch {
            try {
                _statusMessage.value = "Downloading Pet AI model..."
                RunAnywhere.downloadModel(modelId).collect { progress ->
                    _downloadProgress.value = progress
                    _statusMessage.value = "Downloading: ${(progress * 100).toInt()}%"
                }
                _downloadProgress.value = null
                _statusMessage.value = "✓ Download complete! Please load the model."
            } catch (e: Exception) {
                _statusMessage.value = "Download failed: ${e.message}"
                _downloadProgress.value = null
            }
        }
    }

    fun loadModel(modelId: String) {
        viewModelScope.launch {
            try {
                _statusMessage.value = "Loading Pet AI model..."
                val success = RunAnywhere.loadModel(modelId)
                if (success) {
                    _currentModelId.value = modelId
                    _statusMessage.value = "✓ Pet AI Ready! Ask me anything about pet health."

                    // Add model loaded confirmation
                    _messages.value += ChatMessage(
                        text = "🤖 Pet AI Doctor is now online! How can I help with your pet's health today?",
                        isUser = false
                    )
                } else {
                    _statusMessage.value = "Failed to load model"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error loading model: ${e.message}"
            }
        }
    }

    fun updatePetProfile(name: String, breed: String, age: Int, weight: Float) {
        _petProfile.value = PetProfile(name, breed, age, weight)
    }

    fun sendMessage(text: String) {
        if (_currentModelId.value == null) {
            _statusMessage.value = "⚠️ Please load a Pet AI model first"
            _messages.value += ChatMessage(
                text = "Please download and load an AI model from the Models menu to start chatting!",
                isUser = false,
                messageType = MessageType.HEALTH_ALERT
            )
            return
        }

        // Add user message
        _messages.value += ChatMessage(text, isUser = true)

        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Build context-aware prompt with pet health data
                val enhancedPrompt = buildPetHealthPrompt(text)

                // Generate response with streaming
                var assistantResponse = ""
                RunAnywhere.generateStream(enhancedPrompt).collect { token ->
                    assistantResponse += token

                    // Update assistant message in real-time
                    val currentMessages = _messages.value.toMutableList()
                    if (currentMessages.lastOrNull()?.isUser == false) {
                        currentMessages[currentMessages.lastIndex] =
                            ChatMessage(assistantResponse, isUser = false)
                    } else {
                        currentMessages.add(ChatMessage(assistantResponse, isUser = false))
                    }
                    _messages.value = currentMessages
                }
            } catch (e: Exception) {
                _messages.value += ChatMessage(
                    text = "⚠️ Error: ${e.message}\n\nPlease try again or reload the model.",
                    isUser = false,
                    messageType = MessageType.HEALTH_ALERT
                )
            }

            _isLoading.value = false
        }
    }

    private fun buildPetHealthPrompt(userQuery: String): String {
        val pet = _petProfile.value

        return """
You are PAWSYNC AI Doctor, an expert veterinary AI assistant specializing in pet health and wellness.

Pet Profile:
- Name: ${pet.name}
- Breed: ${pet.breed}
- Age: ${pet.age} years
- Weight: ${pet.weight} kg

$mockHealthData

User Question: $userQuery

Instructions:
1. Provide caring, empathetic responses
2. Base recommendations on the health data provided
3. Use emojis sparingly (🐕 🐈 ❤️ ⚠️) for warmth
4. Keep responses concise (3-5 sentences)
5. If critical health issue detected, recommend vet consultation
6. Focus on actionable advice

Response:
        """.trimIndent()
    }

    // Quick action buttons
    fun askQuickQuestion(question: QuickQuestion) {
        val questionText = when (question) {
            QuickQuestion.DAILY_HEALTH -> "How is my pet's health today based on the metrics?"
            QuickQuestion.EXERCISE_PLAN -> "What exercise routine do you recommend?"
            QuickQuestion.DIET_ADVICE -> "What diet plan would be best for my pet?"
            QuickQuestion.MOOD_ANALYSIS -> "Can you analyze my pet's mood and behavior?"
            QuickQuestion.VET_NEEDED -> "Should I consult a veterinarian based on current health?"
        }
        sendMessage(questionText)
    }

    fun refreshModels() {
        loadAvailableModels()
    }

    fun clearChat() {
        _messages.value = emptyList()
        addWelcomeMessage()
    }

    // Add custom model at runtime
    fun addCustomModel(url: String, name: String) {
        viewModelScope.launch {
            try {
                _statusMessage.value = "Adding new AI model..."
                com.runanywhere.sdk.public.extensions.addModelFromURL(
                    url = url,
                    name = name,
                    type = "LLM"
                )
                // Refresh the models list
                loadAvailableModels()
                _statusMessage.value = "✓ New model added successfully!"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to add model: ${e.message}"
            }
        }
    }

    // Add predefined specialized models for specific pet types
    fun addSpecializedModels() {
        viewModelScope.launch {
            try {
                // Add a model optimized for specific use cases
                addCustomModel(
                    url = "https://huggingface.co/microsoft/DialoGPT-small/resolve/main/pytorch_model.bin",
                    name = "PawSync ConversationAI"
                )
            } catch (e: Exception) {
                _statusMessage.value = "Failed to add specialized models: ${e.message}"
            }
        }
    }
}

enum class QuickQuestion {
    DAILY_HEALTH,
    EXERCISE_PLAN,
    DIET_ADVICE,
    MOOD_ANALYSIS,
    VET_NEEDED
}
