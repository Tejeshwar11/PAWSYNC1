package com.runanywhere.startup_hackathon20.presentation.aichat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.ChatViewModel
import com.runanywhere.startup_hackathon20.ChatMessage
import com.runanywhere.startup_hackathon20.MessageType
import com.runanywhere.startup_hackathon20.QuickQuestion
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawSyncAIChatScreen(
    chatViewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val messages by chatViewModel.messages.collectAsState()
    val isLoading by chatViewModel.isLoading.collectAsState()
    val availableModels by chatViewModel.availableModels.collectAsState()
    val downloadProgress by chatViewModel.downloadProgress.collectAsState()
    val currentModelId by chatViewModel.currentModelId.collectAsState()
    val statusMessage by chatViewModel.statusMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showModelSelector by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showCustomModelDialog by remember { mutableStateOf(false) }
    var customModelUrl by remember { mutableStateOf("") }
    var customModelName by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            PawSyncChatTopBar(
                statusMessage = statusMessage,
                currentModelId = currentModelId,
                showMenu = showMenu,
                onMenuToggle = { showMenu = !showMenu },
                onModelSelectorToggle = {
                    showModelSelector = !showModelSelector
                    showMenu = false
                },
                onClearChat = {
                    chatViewModel.clearChat()
                    showMenu = false
                },
                onAddCustomModel = {
                    showMenu = false
                    showCustomModelDialog = true
                },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color.White
                        )
                    )
                )
        ) {
            // AI Model Status Bar
            AIStatusBar(
                statusMessage = statusMessage,
                downloadProgress = downloadProgress,
                currentModelId = currentModelId
            )

            // Model Selector
            if (showModelSelector) {
                PawSyncModelSelector(
                    models = availableModels,
                    currentModelId = currentModelId,
                    onDownload = { modelId -> chatViewModel.downloadModel(modelId) },
                    onLoad = { modelId -> chatViewModel.loadModel(modelId) },
                    onRefresh = { chatViewModel.refreshModels() }
                )
            }

            // Custom Model Dialog
            if (showCustomModelDialog) {
                CustomModelDialog(
                    customModelUrl = customModelUrl,
                    customModelName = customModelName,
                    onUrlChange = { customModelUrl = it },
                    onNameChange = { customModelName = it },
                    onConfirm = {
                        if (customModelUrl.isNotBlank() && customModelName.isNotBlank()) {
                            chatViewModel.addCustomModel(customModelUrl, customModelName)
                            showCustomModelDialog = false
                            customModelUrl = ""
                            customModelName = ""
                        }
                    },
                    onDismiss = { showCustomModelDialog = false }
                )
            }

            // Quick Question Chips
            if (currentModelId != null && !showModelSelector) {
                QuickQuestionChips(
                    onQuestionClick = { question ->
                        chatViewModel.askQuickQuestion(question)
                        keyboardController?.hide()
                    }
                )
            }

            // Messages List
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    PawSyncMessageBubble(
                        message = message,
                        onCopyMessage = { /* TODO: Copy functionality */ },
                        onShareMessage = { /* TODO: Share functionality */ }
                    )
                }

                // Typing indicator
                if (isLoading) {
                    item {
                        PawSyncTypingIndicator()
                    }
                }
            }

            // Auto-scroll to bottom
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            // Input Section
            PawSyncChatInput(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSendMessage = {
                    if (inputText.isNotBlank()) {
                        chatViewModel.sendMessage(inputText)
                        inputText = ""
                        keyboardController?.hide()
                    }
                },
                isEnabled = !isLoading && currentModelId != null,
                isLoading = isLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawSyncChatTopBar(
    statusMessage: String,
    currentModelId: String?,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    onModelSelectorToggle: () -> Unit,
    onClearChat: () -> Unit,
    onAddCustomModel: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "🐾 AI Pet Doctor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (currentModelId != null) Color(0xFF4CAF50) else Color(
                                    0xFFF44336
                                ),
                                shape = CircleShape
                            )
                    )
                }
                Text(
                    "Powered by RunAnywhere AI",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onMenuToggle) {
                Icon(Icons.Default.MoreVert, "Menu")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onMenuToggle() }
            ) {
                DropdownMenuItem(
                    text = { Text("AI Models") },
                    onClick = onModelSelectorToggle,
                    leadingIcon = { Icon(Icons.Default.Settings, null) }
                )
                DropdownMenuItem(
                    text = { Text("Clear Chat") },
                    onClick = onClearChat,
                    leadingIcon = { Icon(Icons.Default.Delete, null) }
                )
                DropdownMenuItem(
                    text = { Text("Add Custom Model") },
                    onClick = onAddCustomModel,
                    leadingIcon = { Icon(Icons.Default.Add, null) }
                )
                DropdownMenuItem(
                    text = { Text("Share Conversation") },
                    onClick = { /* TODO: Share functionality */ },
                    leadingIcon = { Icon(Icons.Default.Share, null) }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF00BCD4),
            titleContentColor = Color.White,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}

@Composable
fun AIStatusBar(
    statusMessage: String,
    downloadProgress: Float?,
    currentModelId: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFB2EBF2),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (currentModelId != null) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (currentModelId != null) Color(0xFF4CAF50) else Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            downloadProgress?.let { progress ->
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = Color(0xFF00BCD4)
                )
            }
        }
    }
}

@Composable
fun QuickQuestionChips(
    onQuestionClick: (QuickQuestion) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            QuickQuestionChip(
                label = "Daily Health Check",
                icon = "❤️",
                onClick = { onQuestionClick(QuickQuestion.DAILY_HEALTH) }
            )
        }
        item {
            QuickQuestionChip(
                label = "Exercise Advice",
                icon = "🏃",
                onClick = { onQuestionClick(QuickQuestion.EXERCISE_PLAN) }
            )
        }
        item {
            QuickQuestionChip(
                label = "Diet Recommendations",
                icon = "🍖",
                onClick = { onQuestionClick(QuickQuestion.DIET_ADVICE) }
            )
        }
        item {
            QuickQuestionChip(
                label = "Mood Analysis",
                icon = "😊",
                onClick = { onQuestionClick(QuickQuestion.MOOD_ANALYSIS) }
            )
        }
        item {
            QuickQuestionChip(
                label = "Symptom Checker",
                icon = "🩺",
                onClick = { onQuestionClick(QuickQuestion.VET_NEEDED) }
            )
        }
        item {
            QuickQuestionChip(
                label = "Emergency Help",
                icon = "⚠️",
                onClick = { onQuestionClick(QuickQuestion.VET_NEEDED) }
            )
        }
    }
}

@Composable
fun QuickQuestionChip(
    label: String,
    icon: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        enabled = true,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFE0F7FA),
            labelColor = Color(0xFF00838F),
            selectedContainerColor = Color(0xFFE0F7FA),
            selectedLabelColor = Color(0xFF00838F)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = Color(0xFF00BCD4).copy(alpha = 0.3f)
        )
    )
}

@Composable
fun PawSyncMessageBubble(
    message: ChatMessage,
    onCopyMessage: () -> Unit = {},
    onShareMessage: () -> Unit = {}
) {
    val backgroundColor = when {
        message.isUser -> Color(0xFF00BCD4)
        message.messageType == MessageType.HEALTH_ALERT -> Color(0xFFFF7043)
        else -> Color(0xFFF5F5F5)
    }

    val textColor = if (message.isUser) Color.White else Color.Black
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { /* TODO: Show action menu */ }
                ),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Message header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (message.isUser) "You" else "🐾 AI Pet Doctor",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isUser) Color.White.copy(alpha = 0.8f) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = formatMessageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (message.isUser) Color.White.copy(alpha = 0.6f) else Color.Gray.copy(
                            alpha = 0.8f
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message content
                if (message.messageType == MessageType.HEALTH_ALERT) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Health Alert",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Health Alert",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = 20.sp
                )

                // Action buttons for AI messages
                if (!message.isUser && message.text.contains("veterinary", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* TODO: Navigate to vet finder */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7043),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Consult Vet Now", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun PawSyncTypingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🐾 AI Pet Doctor is thinking", style = MaterialTheme.typography.bodySmall)

        // Animated dots
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 200),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Text(
                "•",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = alpha)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawSyncChatInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column {
            // Character counter
            if (inputText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${inputText.length}/500",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (inputText.length > 500) Color.Red else Color.Gray
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Input field
                TextField(
                    value = inputText,
                    onValueChange = { if (it.length <= 500) onInputChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Ask about your pet's health...",
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    },
                    enabled = isEnabled,
                    leadingIcon = {
                        Text("🐾", fontSize = 20.sp)
                    },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { onInputChange("") }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (inputText.isNotBlank()) onSendMessage() }
                    ),
                    maxLines = 3
                )

                // Voice input button
                IconButton(
                    onClick = { /* TODO: Voice input */ },
                    enabled = isEnabled
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isEnabled) Color(0xFF00BCD4) else Color.Gray
                    )
                }

                // Send button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isLoading && inputText.isNotBlank() && isEnabled)
                                Color(0xFF00BCD4)
                            else
                                Color.Gray
                        )
                        .clickable(
                            enabled = !isLoading && inputText.isNotBlank() && isEnabled
                        ) {
                            onSendMessage()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomModelDialog(
    customModelUrl: String,
    customModelName: String,
    onUrlChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom AI Model")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Add a custom GGUF model from HuggingFace or other sources:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                TextField(
                    value = customModelUrl,
                    onValueChange = onUrlChange,
                    label = { Text("Model URL") },
                    placeholder = { Text("https://huggingface.co/...") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = customModelName,
                    onValueChange = onNameChange,
                    label = { Text("Model Name") },
                    placeholder = { Text("My Custom Pet AI") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Note: Only GGUF format models are supported",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFFA500)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = customModelUrl.isNotBlank() && customModelName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Model")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PawSyncModelSelector(
    models: List<com.runanywhere.sdk.models.ModelInfo>,
    currentModelId: String?,
    onDownload: (String) -> Unit,
    onLoad: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFAFAFA),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🤖 Pet AI Models",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose the best AI for your pet",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (models.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFFFA000))
                        Column {
                            Text(
                                text = "Loading AI models...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Please wait while we fetch the latest pet health AI models.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(models) { model ->
                        PetAIModelCard(
                            model = model,
                            isLoaded = model.id == currentModelId,
                            onDownload = { onDownload(model.id) },
                            onLoad = { onLoad(model.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetAIModelCard(
    model: com.runanywhere.sdk.models.ModelInfo,
    isLoaded: Boolean,
    onDownload: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded) Color(0xFFC8E6C9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLoaded) 6.dp else 3.dp
        ),
        border = if (isLoaded) BorderStroke(2.dp, Color(0xFF4CAF50)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getModelEmoji(model.name),
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = model.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = getModelDescription(model.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Text(
                        text = getModelSpecs(model.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00BCD4),
                        fontWeight = FontWeight.Medium
                    )
                }

                if (isLoaded) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isLoaded) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "✓ Currently providing pet health insights",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                        enabled = !model.isDownloaded,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BCD4)
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            if (model.isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (model.isDownloaded) "Downloaded" else "Download",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = onLoad,
                        modifier = Modifier.weight(1f),
                        enabled = model.isDownloaded,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Load",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Helper Functions
private fun formatMessageTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun getModelEmoji(modelName: String): String {
    return when {
        modelName.contains("Quick", ignoreCase = true) -> "⚡"
        modelName.contains("Vet", ignoreCase = true) -> "👨‍⚕️"
        modelName.contains("Expert", ignoreCase = true) -> "🎓"
        modelName.contains("Pro", ignoreCase = true) -> "💎"
        modelName.contains("Medical", ignoreCase = true) -> "🩺"
        else -> "🤖"
    }
}

private fun getModelDescription(modelName: String): String {
    return when {
        modelName.contains(
            "Quick",
            ignoreCase = true
        ) -> "Fast responses for basic pet health queries"

        modelName.contains(
            "Vet",
            ignoreCase = true
        ) -> "Balanced AI for general pet health consultations"

        modelName.contains(
            "Expert",
            ignoreCase = true
        ) -> "Advanced analysis for complex health issues"

        modelName.contains("Pro", ignoreCase = true) -> "Professional-grade veterinary insights"
        modelName.contains("Medical", ignoreCase = true) -> "Specialized medical emergency guidance"
        else -> "AI assistant for pet health and wellness"
    }
}

private fun getModelSpecs(modelName: String): String {
    return when {
        modelName.contains("360M", ignoreCase = true) -> "119 MB • Ultra Fast"
        modelName.contains("0.5B", ignoreCase = true) -> "374 MB • Balanced"
        modelName.contains("1B", ignoreCase = true) -> "815 MB • High Quality"
        modelName.contains("1.5B", ignoreCase = true) -> "1.2 GB • Professional"
        else -> "AI Model"
    }
}