package com.runanywhere.startup_hackathon20.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.data.models.AIInsight
import com.runanywhere.startup_hackathon20.data.models.HealthMetrics
import com.runanywhere.startup_hackathon20.data.models.HealthStatus as DataHealthStatus
import com.runanywhere.startup_hackathon20.data.models.PetProfile
import com.runanywhere.startup_hackathon20.presentation.dashboard.HealthStatus as PresentationHealthStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawSyncDashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToAIChat: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
    onNavigateToVetFinder: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // Show success message snackbar
    LaunchedEffect(uiState.showSuccessMessage) {
        uiState.showSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF81D4FA).copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
        ) {
            // Top App Bar with refresh button
            TopAppBar(
                title = {
                    Text(
                        text = "🐾 PawSync Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF00BCD4)
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.refreshData() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh data"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Main content with loading state
            if (uiState.isLoading) {
                LoadingScreen()
            } else if (uiState.currentPet == null) {
                NoPetScreen(
                    onCreateDemoPet = { viewModel.createDemoPet() }
                )
            } else {
                PetDashboardContent(
                    uiState = uiState,
                    onGenerateHealthData = { viewModel.generateMockHealthData() },
                    onRefreshData = { viewModel.refreshData() },
                    onNavigateToAIChat = onNavigateToAIChat,
                    onNavigateToActivity = onNavigateToActivity,
                    onNavigateToVetFinder = onNavigateToVetFinder,
                    onNavigateToHealth = onNavigateToHealth
                )
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF00BCD4),
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading your pet's data...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun NoPetScreen(
    onCreateDemoPet: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🐾",
                    fontSize = 64.sp
                )

                Text(
                    text = "Welcome to PawSync!",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Let's get started by creating your first pet profile or try our demo.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Button(
                    onClick = onCreateDemoPet,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Demo with Max")
                }
            }
        }
    }
}

@Composable
fun PetDashboardContent(
    uiState: DashboardUiState,
    onGenerateHealthData: () -> Unit,
    onRefreshData: () -> Unit,
    onNavigateToAIChat: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToVetFinder: () -> Unit,
    onNavigateToHealth: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pet Header with Health Score
        item {
            PetHeaderCard(
                pet = uiState.currentPet!!,
                healthScore = uiState.healthScore,
                healthStatus = uiState.healthStatus,
                animateScore = uiState.animateHealthScore,
                lastUpdated = uiState.lastUpdated
            )
        }

        // Health Metrics Grid
        item {
            Text(
                text = "📊 Health Metrics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            uiState.healthMetrics?.let { metrics ->
                HealthMetricsGrid(metrics = metrics)
            }
        }

        // AI Insights Section
        item {
            uiState.latestInsight?.let { insight ->
                AIInsightCard(insight = insight, onViewFullAnalysis = onNavigateToAIChat)
            }
        }

        // Quick Actions
        item {
            QuickActionsRow(
                onGenerateHealthData = onGenerateHealthData,
                onRefreshData = onRefreshData,
                onNavigateToAIChat = onNavigateToAIChat,
                onNavigateToActivity = onNavigateToActivity,
                onNavigateToVetFinder = onNavigateToVetFinder,
                onNavigateToHealth = onNavigateToHealth
            )
        }
    }
}

@Composable
fun PetHeaderCard(
    pet: PetProfile,
    healthScore: Float,
    healthStatus: PresentationHealthStatus,
    animateScore: Boolean,
    lastUpdated: Long
) {
    val animatedScore by animateFloatAsState(
        targetValue = if (animateScore) healthScore else healthScore,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "health_score_animation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00BCD4).copy(alpha = 0.2f),
                                Color(0xFF00BCD4).copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐕",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Pet Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${pet.breed} • ${pet.age} years old",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = healthStatus.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(healthStatus.color),
                    fontWeight = FontWeight.SemiBold
                )

                if (lastUpdated > 0) {
                    Text(
                        text = "Updated ${formatRelativeTime(lastUpdated)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Health Score Ring
            AnimatedHealthScoreRing(
                score = animatedScore,
                healthStatus = healthStatus,
                size = 100.dp
            )
        }
    }
}

@Composable
fun AnimatedHealthScoreRing(
    score: Float,
    healthStatus: PresentationHealthStatus,
    size: androidx.compose.ui.unit.Dp
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size)
        ) {
            val strokeWidth = 8.dp.toPx()
            val radius = (sizePx - strokeWidth) / 2
            val center = Offset(sizePx / 2, sizePx / 2)

            // Background circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            val sweepAngle = (score / 100f) * 360f
            drawArc(
                color = Color(healthStatus.color),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = androidx.compose.ui.geometry.Size(
                    sizePx - strokeWidth,
                    sizePx - strokeWidth
                )
            )
        }

        // Score text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${score.toInt()}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(healthStatus.color)
            )
            Text(
                text = "HEALTH",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun HealthMetricsGrid(
    metrics: HealthMetrics
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(400.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            listOf(
                HealthMetricItem(
                    title = "Activity",
                    value = "${metrics.stepsToday}",
                    subtitle = "/${metrics.stepsGoal} steps",
                    progress = metrics.stepsToday.toFloat() / metrics.stepsGoal,
                    icon = "🏃",
                    color = Color(0xFF00BCD4)
                ),
                HealthMetricItem(
                    title = "Heart Rate",
                    value = "${metrics.heartRate ?: 0}",
                    subtitle = "BPM",
                    progress = ((metrics.heartRate ?: 80) - 60).toFloat() / 40f,
                    icon = "❤️",
                    color = Color(0xFFFF7043)
                ),
                HealthMetricItem(
                    title = "Hydration",
                    value = "${metrics.hydrationMl}",
                    subtitle = "/${metrics.hydrationGoalMl} ml",
                    progress = metrics.hydrationMl.toFloat() / metrics.hydrationGoalMl,
                    icon = "💧",
                    color = Color(0xFF2196F3)
                ),
                HealthMetricItem(
                    title = "Sleep",
                    value = "${metrics.sleepHours}h",
                    subtitle = metrics.sleepQuality,
                    progress = metrics.sleepHours / 12f,
                    icon = "😴",
                    color = Color(0xFF9C27B0)
                ),
                HealthMetricItem(
                    title = "Mood",
                    value = "${metrics.moodScore}/10",
                    subtitle = "Excellent",
                    progress = metrics.moodScore / 10f,
                    icon = "😊",
                    color = Color(0xFF4CAF50)
                ),
                HealthMetricItem(
                    title = "Temperature",
                    value = "${metrics.bodyTemperature ?: 38.0f}°C",
                    subtitle = "Normal",
                    progress = 0.8f,
                    icon = "🌡️",
                    color = Color(0xFFFF9800)
                )
            )
        ) { item ->
            HealthMetricCard(item = item)
        }
    }
}

data class HealthMetricItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val progress: Float,
    val icon: String,
    val color: Color
)

@Composable
fun HealthMetricCard(
    item: HealthMetricItem
) {
    val animatedProgress by animateFloatAsState(
        targetValue = item.progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 800,
            easing = EaseOutCubic
        ),
        label = "progress_animation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.icon,
                    fontSize = 24.sp
                )

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = item.color,
                    trackColor = item.color.copy(alpha = 0.2f)
                )
            }

            Column {
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = item.color
                )

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AIInsightCard(
    insight: AIInsight,
    onViewFullAnalysis: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧠",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Health Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text("NEW") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF4CAF50),
                        labelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = insight.summary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )

            if (insight.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Recommendations:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        insight.recommendations.take(3).forEach { recommendation ->
                            AssistChip(
                                onClick = { },
                                label = { Text(recommendation) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF00BCD4).copy(alpha = 0.1f),
                                    labelColor = Color(0xFF00BCD4)
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onViewFullAnalysis,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Full Analysis")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onGenerateHealthData: () -> Unit,
    onRefreshData: () -> Unit,
    onNavigateToAIChat: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToVetFinder: () -> Unit,
    onNavigateToHealth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "⚡ Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onGenerateHealthData,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Generate data"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Data")
                }

                OutlinedButton(
                    onClick = onRefreshData,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Button(
                        onClick = onNavigateToAIChat,
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BCD4)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Chat",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                item {
                    Button(
                        onClick = onNavigateToActivity,
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Activity",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                item {
                    Button(
                        onClick = onNavigateToVetFinder,
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7043)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Find Vet",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                item {
                    Button(
                        onClick = onNavigateToHealth,
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Health History",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
        else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
    }
}