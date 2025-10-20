package com.runanywhere.startup_hackathon20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.runanywhere.startup_hackathon20.presentation.aichat.PawSyncAIChatScreen
import com.runanywhere.startup_hackathon20.presentation.onboarding.WelcomeScreen
import com.runanywhere.startup_hackathon20.presentation.onboarding.PetProfileScreen
import com.runanywhere.startup_hackathon20.ChatViewModel
import com.runanywhere.startup_hackathon20.ui.theme.Startup_hackathon20Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Startup_hackathon20Theme {
                PawSyncApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PawSyncApp() {
    val navController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(false) }

    // Check if onboarding is completed (simplified - in production use DataStore)
    var onboardingCompleted by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBottomBar && onboardingCompleted) {
                PawSyncBottomNav(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) "dashboard" else "welcome",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Onboarding Flow
            composable("welcome") {
                LaunchedEffect(Unit) { showBottomBar = false }
                WelcomeScreen(
                    onGetStarted = {
                        navController.navigate("pet_profile") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    },
                    onSkip = {
                        onboardingCompleted = true
                        navController.navigate("dashboard") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                )
            }

            composable("pet_profile") {
                LaunchedEffect(Unit) { showBottomBar = false }
                PetProfileScreen(
                    onProfileCreated = {
                        onboardingCompleted = true
                        navController.navigate("dashboard") {
                            popUpTo("pet_profile") { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.navigate("welcome") {
                            popUpTo("pet_profile") { inclusive = true }
                        }
                    }
                )
            }

            // Main App Screens
            composable("dashboard") {
                LaunchedEffect(Unit) { showBottomBar = true }
                SimpleDashboardScreen(
                    onNavigateToAI = {
                        navController.navigate("aichat") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }

            composable("health") {
                LaunchedEffect(Unit) { showBottomBar = true }
                SimpleHealthScreen()
            }

            composable("aichat") {
                LaunchedEffect(Unit) { showBottomBar = true }
                val chatViewModel: ChatViewModel = viewModel()
                PawSyncAIChatScreen(
                    chatViewModel = chatViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("activity") {
                LaunchedEffect(Unit) { showBottomBar = true }
                SimpleActivityScreen()
            }

            composable("vet") {
                LaunchedEffect(Unit) { showBottomBar = true }
                SimpleVetScreen()
            }

            composable("settings") {
                LaunchedEffect(Unit) { showBottomBar = true }
                SimpleSettingsScreen()
            }
        }
    }
}

@Composable
fun PawSyncBottomNav(navController: NavHostController) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home") },
            selected = currentRoute == "dashboard",
            onClick = {
                navController.navigate("dashboard") {
                    popUpTo("dashboard") { inclusive = true }
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00BCD4),
                selectedTextColor = Color(0xFF00BCD4),
                indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.MonitorHeart, contentDescription = "Health") },
            label = { Text("Health") },
            selected = currentRoute == "health",
            onClick = {
                navController.navigate("health") {
                    popUpTo("dashboard")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00BCD4),
                selectedTextColor = Color(0xFF00BCD4),
                indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Chat, contentDescription = "AI Chat") },
            label = { Text("AI Chat") },
            selected = currentRoute == "aichat",
            onClick = {
                navController.navigate("aichat") {
                    popUpTo("dashboard")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00BCD4),
                selectedTextColor = Color(0xFF00BCD4),
                indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = "Activity"
                )
            },
            label = { Text("Activity") },
            selected = currentRoute == "activity",
            onClick = {
                navController.navigate("activity") {
                    popUpTo("dashboard")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00BCD4),
                selectedTextColor = Color(0xFF00BCD4),
                indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            )
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == "settings",
            onClick = {
                navController.navigate("settings") {
                    popUpTo("dashboard")
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00BCD4),
                selectedTextColor = Color(0xFF00BCD4),
                indicatorColor = Color(0xFF00BCD4).copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun SimpleDashboardScreen(onNavigateToAI: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("🐾", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "PAWSYNC Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "AI-Powered Pet Health & Fitness Tracker",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Health Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Today's Health Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("❤️ Health Score: 92/100", fontWeight = FontWeight.Medium)
                Text("🏃 Activity: 7,543 steps today")
                Text("💧 Hydration: 800ml / 1000ml")
                Text("😊 Mood: Excellent")
                Text("🌡️ Temperature: 38.3°C (Normal)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI Doctor Button
        Button(
            onClick = onNavigateToAI,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "🤖 Ask AI Pet Doctor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatCard(
                icon = "🏃",
                label = "Steps",
                value = "7.5K",
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                icon = "❤️",
                label = "Heart",
                value = "85 BPM",
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                icon = "😊",
                label = "Mood",
                value = "8.5/10",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickStatCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SimpleHealthScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊", fontSize = 64.sp)
        Text(
            "Health History",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Charts and health trends will appear here",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SimpleActivityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏃", fontSize = 64.sp)
        Text(
            "Activity Tracker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Log and track your pet's activities",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Activity")
        }
    }
}

@Composable
fun SimpleVetScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👨‍⚕️", fontSize = 64.sp)
        Text(
            "Find Veterinarians",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Locate nearby vet clinics",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SimpleSettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚙️", fontSize = 64.sp)
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00BCD4)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "App preferences and pet management",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
