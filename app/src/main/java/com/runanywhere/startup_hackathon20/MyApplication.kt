package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i("PAWSYNC", "App started - Initializing SDK...")

        // Initialize SDK asynchronously to prevent startup delays
        GlobalScope.launch(Dispatchers.IO) {
            initializeSDK()
        }
    }

    private suspend fun initializeSDK() {
        try {
            // Step 1: Initialize RunAnywhere SDK
            RunAnywhere.initialize(
                context = this@MyApplication,
                apiKey = "dev",  // Any string works in dev mode
                environment = SDKEnvironment.DEVELOPMENT
            )
            Log.i("PAWSYNC", "✓ SDK Core initialized")

            // Step 2: Register LLM Service Provider (llama.cpp)
            LlamaCppServiceProvider.register()
            Log.i("PAWSYNC", "✓ LlamaCpp Service Provider registered")

            // Step 3: Register Pet Health AI Models
            registerPetHealthModels()
            Log.i("PAWSYNC", "✓ Pet Health AI models registered")

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()
            Log.i("PAWSYNC", "✓ Scanned for downloaded models")

            Log.i("PAWSYNC", "🐾 PawSync AI System ready!")

        } catch (e: Exception) {
            Log.e("PAWSYNC", "❌ SDK initialization failed: ${e.message}", e)
        }
    }

    private suspend fun registerPetHealthModels() {
        try {
            // Qwen 2.5 0.5B - Best balance of size and quality for pet health
            // 374 MB - Optimized for general pet care conversations
            addModelFromURL(
                url = "https://huggingface.co/Triangle104/Qwen2.5-0.5B-Instruct-Q6_K-GGUF/resolve/main/qwen2.5-0.5b-instruct-q6_k.gguf",
                name = "Qwen 2.5 0.5B Instruct Q6_K",
                type = "LLM"
            )
            Log.i("PAWSYNC", "Registered: Qwen 2.5 0.5B (374 MB)")

            // SmolLM2 360M - Lightweight for quick responses
            // 119 MB - Good for fast basic queries
            addModelFromURL(
                url = "https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf",
                name = "SmolLM2 360M Q8_0",
                type = "LLM"
            )
            Log.i("PAWSYNC", "Registered: SmolLM2 360M (119 MB)")

            // Llama 3.2 1B - Higher quality for complex health analysis
            // 815 MB - Better for detailed veterinary consultations
            addModelFromURL(
                url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                name = "Llama 3.2 1B Instruct Q6_K",
                type = "LLM"
            )
            Log.i("PAWSYNC", "Registered: Llama 3.2 1B (815 MB)")

        } catch (e: Exception) {
            Log.e("PAWSYNC", "Failed to register models: ${e.message}", e)
        }
    }
}
