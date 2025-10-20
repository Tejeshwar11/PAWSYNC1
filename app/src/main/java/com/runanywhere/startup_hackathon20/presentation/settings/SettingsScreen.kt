package com.runanywhere.startup_hackathon20.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory())
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8),
                        Color.White
                    )
                )
            )
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    "⚙️ Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pet Profile Section
            item {
                SettingsSection(
                    title = "🐾 Pet Profile",
                    icon = Icons.Default.Pets
                ) {
                    SettingsItem(
                        title = "Current Pet: ${uiState.currentPet?.name ?: "No Pet Selected"}",
                        subtitle = uiState.currentPet?.breed ?: "Add your first pet",
                        icon = Icons.Default.AccountCircle,
                        onClick = { viewModel.editCurrentPet() }
                    )

                    SettingsItem(
                        title = "Manage Pets",
                        subtitle = "${uiState.totalPets} pets registered",
                        icon = Icons.Default.Pets,
                        onClick = { viewModel.managePets() }
                    )
                }
            }

            // Health & Monitoring
            item {
                SettingsSection(
                    title = "❤️ Health & Monitoring",
                    icon = Icons.Default.MonitorHeart
                ) {
                    SettingsToggleItem(
                        title = "Health Alerts",
                        subtitle = "Get notified about health issues",
                        icon = Icons.Default.Notifications,
                        checked = uiState.healthAlertsEnabled,
                        onCheckedChange = { viewModel.toggleHealthAlerts() }
                    )

                    SettingsItem(
                        title = "Daily Summary Time",
                        subtitle = uiState.dailySummaryTime,
                        icon = Icons.Default.Schedule,
                        onClick = { viewModel.setDailySummaryTime() }
                    )

                    SettingsToggleItem(
                        title = "Medication Reminders",
                        subtitle = "Remind me about pet medications",
                        icon = Icons.Default.MedicalServices,
                        checked = uiState.medicationRemindersEnabled,
                        onCheckedChange = { viewModel.toggleMedicationReminders() }
                    )
                }
            }

            // AI & Models
            item {
                SettingsSection(
                    title = "🤖 AI Assistant",
                    icon = Icons.Default.Psychology
                ) {
                    SettingsItem(
                        title = "Current AI Model",
                        subtitle = uiState.currentAIModel,
                        icon = Icons.Default.Memory,
                        onClick = { viewModel.selectAIModel() }
                    )

                    SettingsItem(
                        title = "Download Models",
                        subtitle = "${uiState.availableModels.size} models available",
                        icon = Icons.Default.Download,
                        onClick = { viewModel.downloadModels() }
                    )

                    SettingsToggleItem(
                        title = "On-Device Processing",
                        subtitle = "Process AI requests locally for privacy",
                        icon = Icons.Default.Security,
                        checked = uiState.onDeviceProcessing,
                        onCheckedChange = { viewModel.toggleOnDeviceProcessing() }
                    )
                }
            }

            // Wearable Devices
            item {
                SettingsSection(
                    title = "⌚ Wearable Devices",
                    icon = Icons.Default.Watch
                ) {
                    SettingsItem(
                        title = "Connected Devices",
                        subtitle = "${uiState.connectedDevices.size} devices connected",
                        icon = Icons.Default.DeviceHub,
                        onClick = { viewModel.manageDevices() }
                    )

                    SettingsItem(
                        title = "Add New Device",
                        subtitle = "Pair a new smart collar or tracker",
                        icon = Icons.Default.Add,
                        onClick = { viewModel.addNewDevice() }
                    )

                    SettingsItem(
                        title = "Sync Frequency",
                        subtitle = uiState.syncFrequency,
                        icon = Icons.Default.Sync,
                        onClick = { viewModel.setSyncFrequency() }
                    )
                }
            }

            // App Preferences
            item {
                SettingsSection(
                    title = "📱 App Preferences",
                    icon = Icons.Default.Settings
                ) {
                    SettingsItem(
                        title = "Theme",
                        subtitle = uiState.selectedTheme.displayName,
                        icon = Icons.Default.Palette,
                        onClick = { viewModel.selectTheme() }
                    )

                    SettingsItem(
                        title = "Language",
                        subtitle = uiState.selectedLanguage.displayName,
                        icon = Icons.Default.Language,
                        onClick = { viewModel.selectLanguage() }
                    )

                    SettingsItem(
                        title = "Units",
                        subtitle = uiState.selectedUnits.displayName,
                        icon = Icons.Default.Straighten,
                        onClick = { viewModel.selectUnits() }
                    )
                }
            }

            // Data & Privacy
            item {
                SettingsSection(
                    title = "🔒 Data & Privacy",
                    icon = Icons.Default.Security
                ) {
                    SettingsItem(
                        title = "Export Data",
                        subtitle = "Download all your pet's data",
                        icon = Icons.Default.FileDownload,
                        onClick = { viewModel.exportData() }
                    )

                    SettingsItem(
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        icon = Icons.Default.Policy,
                        onClick = { viewModel.openPrivacyPolicy() }
                    )

                    SettingsItem(
                        title = "Terms of Service",
                        subtitle = "App usage terms and conditions",
                        icon = Icons.Default.Description,
                        onClick = { viewModel.openTermsOfService() }
                    )
                }
            }

            // Community & Social
            item {
                SettingsSection(
                    title = "🌐 Community",
                    icon = Icons.Default.Group
                ) {
                    SettingsToggleItem(
                        title = "Community Features",
                        subtitle = "Share and connect with other pet parents",
                        icon = Icons.Default.Share,
                        checked = uiState.communityFeaturesEnabled,
                        onCheckedChange = { viewModel.toggleCommunityFeatures() }
                    )

                    SettingsToggleItem(
                        title = "Public Profile",
                        subtitle = "Make your pet's profile discoverable",
                        icon = Icons.Default.Public,
                        checked = uiState.publicProfileEnabled,
                        onCheckedChange = { viewModel.togglePublicProfile() }
                    )
                }
            }

            // About & Support
            item {
                SettingsSection(
                    title = "ℹ️ About & Support",
                    icon = Icons.Default.Info
                ) {
                    SettingsItem(
                        title = "App Version",
                        subtitle = uiState.appVersion,
                        icon = Icons.Default.Code,
                        onClick = { }
                    )

                    SettingsItem(
                        title = "Help & Support",
                        subtitle = "Get help or report issues",
                        icon = Icons.Default.Help,
                        onClick = { viewModel.openSupport() }
                    )

                    SettingsItem(
                        title = "Rate App",
                        subtitle = "Enjoying PAWSYNC? Leave a review!",
                        icon = Icons.Default.Star,
                        onClick = { viewModel.rateApp() }
                    )

                    SettingsItem(
                        title = "Credits",
                        subtitle = "Made with ❤️ for pet parents",
                        icon = Icons.Default.Favorite,
                        onClick = { viewModel.showCredits() }
                    )
                }
            }

            // Danger Zone
            if (uiState.showDangerZone) {
                item {
                    SettingsSection(
                        title = "⚠️ Danger Zone",
                        icon = Icons.Default.Warning,
                        backgroundColor = Color(0xFFFFEBEE)
                    ) {
                        SettingsItem(
                            title = "Delete Account",
                            subtitle = "Permanently delete all data",
                            icon = Icons.Default.DeleteForever,
                            textColor = Color.Red,
                            onClick = { viewModel.deleteAccount() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    backgroundColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// Enum classes for settings options
enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    AUTO("System Default")
}

enum class AppLanguage(val displayName: String) {
    ENGLISH("English"),
    HINDI("हिंदी"),
    SPANISH("Español"),
    FRENCH("Français")
}

enum class AppUnits(val displayName: String) {
    METRIC("Metric (kg, km)"),
    IMPERIAL("Imperial (lbs, miles)")
}