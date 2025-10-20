package com.runanywhere.startup_hackathon20.presentation.activity

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.runanywhere.startup_hackathon20.data.models.ActivityLog
import com.runanywhere.startup_hackathon20.data.models.ActivityType
import com.runanywhere.startup_hackathon20.data.models.ActivityIntensity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    onBack: () -> Unit = {},
    viewModel: ActivityLogViewModel = viewModel(factory = ActivityLogViewModel.Factory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogForm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🏃 Activity Tracker",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF00BCD4),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddActivityDialog() },
                containerColor = Color(0xFFFF7043),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Log Activity")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activity Statistics
            item {
                ActivityStatsCard(
                    totalActivities = uiState.totalActivitiesThisMonth,
                    totalDuration = uiState.averageDuration.toInt(),
                    totalCalories = uiState.totalCaloriesBurned,
                    mostCommonActivity = uiState.mostCommonActivity ?: "WALK"
                )
            }

            // Recent Activities List
            item {
                Text(
                    "Recent Activities",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.activities.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            } else {
                items(uiState.activities) { activity ->
                    ActivityCard(
                        activity = activity,
                        onDelete = { viewModel.deleteActivity(activity) }
                    )
                }
            }
        }
    }

    // Add Activity Dialog
    if (uiState.showAddDialog) {
        AddActivityDialog(
            onDismiss = { viewModel.hideAddActivityDialog() },
            onSave = { viewModel.saveActivity() },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ActivityStatsCard(
    totalActivities: Int,
    totalDuration: Int,
    totalCalories: Int,
    mostCommonActivity: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "📊 This Week's Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BCD4)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("🏃", "Activities", totalActivities.toString())
                StatItem("⏱️", "Minutes", "${totalDuration}min")
                StatItem("🔥", "Calories", "${totalCalories}cal")
                StatItem("💪", "Most Common Activity", mostCommonActivity)
            }
        }
    }
}

@Composable
private fun StatItem(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun ActivityCard(
    activity: ActivityLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF00BCD4).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getActivityIcon(ActivityType.valueOf(activity.activityType)),
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Activity details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.activityType.lowercase().capitalize(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${activity.duration}min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(" • ", color = Color.Gray)
                    Text(
                        "${activity.caloriesBurned} cal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    if (activity.distanceKm > 0) {
                        Text(" • ", color = Color.Gray)
                        Text(
                            "${activity.distanceKm}km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    formatTimeAgo(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🐾", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No activities logged yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tap the + button to log your pet's first activity",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddActivityDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    viewModel: ActivityLogViewModel
) {
    val newActivityState by viewModel.newActivityState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🏃 Log New Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Activity Type
                item {
                    Text("Activity Type", fontWeight = FontWeight.Medium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ActivityType.values().take(6)) { type ->
                            FilterChip(
                                selected = newActivityState.activityType == type,
                                onClick = {
                                    viewModel.updateNewActivity { it.copy(activityType = type) }
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(getActivityIcon(type))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(type.name.lowercase().capitalize())
                                    }
                                }
                            )
                        }
                    }
                }

                // Duration
                item {
                    OutlinedTextField(
                        value = newActivityState.duration.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { duration ->
                                viewModel.updateNewActivity { it.copy(duration = duration) }
                            }
                        },
                        label = { Text("Duration (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("⏱️") }
                    )
                }

                // Intensity
                item {
                    Text("Intensity", fontWeight = FontWeight.Medium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActivityIntensity.values().forEach { intensityOption ->
                            FilterChip(
                                selected = newActivityState.intensity == intensityOption,
                                onClick = {
                                    viewModel.updateNewActivity { it.copy(intensity = intensityOption) }
                                },
                                label = { Text(intensityOption.name.lowercase().capitalize()) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Distance (optional)
                item {
                    OutlinedTextField(
                        value = if (newActivityState.distanceKm > 0) newActivityState.distanceKm.toString() else "",
                        onValueChange = { value ->
                            val distance = value.toFloatOrNull() ?: 0f
                            viewModel.updateNewActivity { it.copy(distanceKm = distance) }
                        },
                        label = { Text("Distance (km) - Optional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text("📏") }
                    )
                }

                // Calories (optional)
                item {
                    OutlinedTextField(
                        value = if (newActivityState.caloriesBurned > 0) newActivityState.caloriesBurned.toString() else "",
                        onValueChange = { value ->
                            val calories = value.toIntOrNull() ?: 0
                            viewModel.updateNewActivity { it.copy(caloriesBurned = calories) }
                        },
                        label = { Text("Calories Burned - Optional") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("🔥") }
                    )
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = newActivityState.notes,
                        onValueChange = { value ->
                            viewModel.updateNewActivity { it.copy(notes = value) }
                        },
                        label = { Text("Notes") },
                        placeholder = { Text("How did your pet enjoy this activity?") },
                        maxLines = 3,
                        leadingIcon = { Text("📝") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = newActivityState.duration > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4)
                )
            ) {
                Text("Save Activity")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getActivityIcon(type: ActivityType): String {
    return when (type) {
        ActivityType.WALK -> "🚶"
        ActivityType.RUN -> "🏃"
        ActivityType.PLAY -> "🎾"
        ActivityType.TRAINING -> "🎯"
        ActivityType.SWIMMING -> "🏊"
        ActivityType.FETCH -> "🎾"
        ActivityType.FEEDING -> "🍽️"
        ActivityType.GROOMING -> "✂️"
        ActivityType.VET_VISIT -> "👨‍⚕️"
        else -> "🐾"
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> "${diff / (24 * 60 * 60 * 1000)}d ago"
    }
}