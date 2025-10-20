# PAWSYNC - AI-Powered Pet Health & Fitness Tracker

An Android app that provides AI-powered pet health monitoring, fitness tracking, and 24/7 veterinary
consultation using on-device AI models.

## Features

### AI Pet Doctor Chat

- **On-Device AI**: Run powerful AI models directly on your device for instant pet health insights
- **Multiple AI Models**:
    - **Qwen 2.5 0.5B** (374 MB) - Recommended for general pet care
    - **SmolLM2 360M** (119 MB) - Lightweight for quick responses
    - **Llama 3.2 1B** (815 MB) - Advanced for complex health analysis
- **Real-time Streaming**: Watch AI responses generate in real-time
- **Context-Aware**: AI understands your pet's health history and current metrics
- **Quick Questions**: One-tap access to common health queries

### Pet Registration & Profiles

- Complete pet profile creation flow
- Medical history tracking
- Vaccination records
- Emergency contact management
- Photo support

### Health Monitoring

- Heart rate tracking
- Activity/step counter
- Hydration monitoring
- Sleep quality analysis
- Mood scoring
- Body temperature tracking

### Activity Tracking

- Daily step goals
- Exercise recommendations
- Activity logging

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- Android device or emulator with:
    - **Minimum SDK**: Android 7.0 (API 24)
    - **Recommended**: Android 10+ (API 29+)
    - **RAM**: 4GB+ recommended for AI models
    - **Storage**: 1-2GB free space for AI models

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd PAWSYNC/Hackss
   ```

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an Existing Project"
    - Navigate to `PAWSYNC/Hackss` folder
    - Wait for Gradle sync to complete

3. **Build & Run**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

   Or use Android Studio's Run button (Shift+F10)

## First-Time Setup

### 1. Welcome Screen

When you first launch the app, you'll see an animated welcome screen introducing PawSync's features.

### 2. Pet Profile Registration

Complete the 5-step registration process:

- **Step 1**: Add your pet's photo
- **Step 2**: Basic information (name, species, breed)
- **Step 3**: Physical stats (age, weight, gender)
- **Step 4**: Medical history (vaccinations, allergies, conditions)
- **Step 5**: Emergency contacts (owner info, emergency contact, vet)

### 3. Dashboard

After registration, you'll access the main dashboard with:

- Health summary card
- Quick stats (steps, heart rate, mood)
- Direct access to AI Pet Doctor

## Using the AI Pet Doctor

### Downloading AI Models

1. **Navigate to AI Chat Tab**
    - Tap the "AI Chat" icon in the bottom navigation bar
    - You'll see a status message about downloading models

2. **Open Model Selector**
    - Tap the menu icon (⋮) in the top-right
    - Select "AI Models"

3. **Choose Your Model**

   **Recommended for first-time users:**
    - **Qwen 2.5 0.5B** (374 MB) - Best balance of speed and quality

   **For quick testing:**
    - **SmolLM2 360M** (119 MB) - Fastest download, good for basic queries

   **For advanced users:**
    - **Llama 3.2 1B** (815 MB) - Highest quality responses

4. **Download & Load**
    - Tap "Download" button
    - Wait for download to complete (progress bar shown)
    - Once downloaded, tap "Load" to activate the model
    - Model is now ready for chat!

### Chatting with AI Pet Doctor

Once a model is loaded:

1. **Type your question** in the input field at the bottom
2. **Use Quick Question buttons** for common queries:
    - Daily Health Check
    - Exercise Advice
    - Diet Recommendations
    - Mood Analysis
    - Symptom Checker
    - Emergency Help

3. **Watch AI respond in real-time** with streaming text generation

### Example Questions to Ask

- "How is my pet's health today based on their metrics?"
- "What exercise routine would you recommend for my golden retriever?"
- "Can you analyze my pet's mood based on their behavior?"
- "Should I be concerned about my pet's current symptoms?"
- "What's the best diet plan for a 3-year-old dog?"
- "My pet seems lethargic - what should I do?"

## AI Chat Features

### System Prompt

The AI is configured with a specialized system prompt for pet health:

- Expert veterinary knowledge
- Context-aware (uses your pet's profile and health data)
- Empathetic and caring tone
- Safety-first approach (recommends vet consultation when needed)

### Message Types

- **Regular Messages**: General conversation
- **Health Alerts**: Critical information highlighted in orange
- **Recommendations**: Actionable advice for pet care

### Chat Management

- **Clear Chat**: Reset conversation (Menu → Clear Chat)
- **Export Chat**: Save conversation history (coming soon)
- **Share Conversation**: Share with your vet (coming soon)

## Advanced Features

### Adding Custom Models

1. Open AI Chat
2. Tap Menu (⋮) → "Add Custom Model"
3. Enter model URL (HuggingFace GGUF models)
4. Provide a friendly name
5. Model will be registered and available for download

### Model Management

- **Refresh Models**: Pull latest model list
- **Switch Models**: Load different models for different use cases
- **Model Status**: See which model is currently active

## App Architecture

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **AI SDK**: RunAnywhere SDK with llama.cpp
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines & Flow

### Key Components

- `MyApplication.kt`: SDK initialization, model registration
- `MainActivity.kt`: Main navigation and app entry point
- `ChatViewModel.kt`: AI chat logic and state management
- `PawSyncAIChatScreen.kt`: Full-featured chat UI
- `PetProfileScreen.kt`: Pet registration flow

## Design System

### Color Palette

- **Primary**: Cyan (#00BCD4) - Trust and technology
- **Accent**: Orange (#FF7043) - Warmth and alerts
- **Success**: Green (#4CAF50) - Health and positive states
- **Background**: White with subtle gradients

### Typography

- Material Design 3 typography scale
- Clear hierarchy for readability

## Troubleshooting

### Model Download Issues

- **Problem**: Download fails or stalls
- **Solution**:
    - Check internet connection
    - Ensure sufficient storage space
    - Try refreshing models list
    - Restart the app

### Model Won't Load

- **Problem**: "Failed to load model" error
- **Solution**:
    - Ensure model download completed (check download progress)
    - Close other memory-intensive apps
    - Try the smaller SmolLM2 model first
    - Restart the app

### App Crashes During Generation

- **Problem**: App closes when generating AI responses
- **Solution**:
    - Use a smaller model (SmolLM2 360M)
    - Close background apps to free memory
    - Ensure device has adequate RAM (4GB+)

### Slow AI Responses

- **Problem**: AI takes long to generate responses
- **Solution**:
    - This is normal for on-device AI
    - Try SmolLM2 360M for faster responses
    - Close background apps
    - Wait for first response (subsequent ones are faster)

## Development Notes

### Building from Source

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Project Structure

```
Hackss/
├── app/
│   ├── src/main/java/com/runanywhere/startup_hackathon20/
│   │   ├── data/
│   │   │   ├── models/          # Data models
│   │   │   ├── repositories/    # Data repositories
│   │   │   └── dao/             # Database access
│   │   ├── presentation/
│   │   │   ├── aichat/          # AI Chat UI & ViewModel
│   │   │   ├── onboarding/      # Pet registration
│   │   │   ├── dashboard/       # Home screen
│   │   │   ├── health/          # Health tracking
│   │   │   ├── activity/        # Activity logging
│   │   │   └── settings/        # App settings
│   │   ├── ui/theme/            # App theming
│   │   ├── ChatViewModel.kt     # Legacy chat ViewModel
│   │   ├── MainActivity.kt      # App entry point
│   │   └── MyApplication.kt     # SDK initialization
│   └── libs/                    # SDK AAR files
├── build.gradle.kts
└── settings.gradle.kts
```

### SDK Documentation

See `app/src/main/java/.../QUICK_START_ANDROID.md` for detailed SDK documentation.

## Privacy & Security

- **On-Device Processing**: All AI computations run locally - no data sent to cloud
- **No Account Required**: Use the app without registration
- **Local Storage**: All pet data stored locally on device
- **Permissions**: Only requires Internet (for model downloads) and Storage

## Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

See LICENSE file for details.

## Support

For issues or questions:

- Open an issue on GitHub
- Check the troubleshooting section above
- Review the SDK documentation

## Acknowledgments

- **RunAnywhere SDK**: Powering on-device AI
- **llama.cpp**: Efficient LLM inference
- **HuggingFace**: Model hosting
- **Material Design 3**: UI components

---

**Made with for pets and their humans**

Keep your furry friends healthy and happy with PawSync!
