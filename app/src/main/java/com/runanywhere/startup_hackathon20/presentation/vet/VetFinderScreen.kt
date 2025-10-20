package com.runanywhere.startup_hackathon20.presentation.vet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetFinderScreen(
    modifier: Modifier = Modifier,
    viewModel: VetFinderViewModel = viewModel(factory = VetFinderViewModel.Factory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color.White
                    )
                )
            )
    ) {
        // Top Bar with Search and Filters
        TopAppBar(
            title = {
                Text(
                    "🏥 Find Veterinarians",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.toggleMapView() }) {
                    Icon(
                        imageVector = if (uiState.isMapView) Icons.Default.List else Icons.Default.LocationOn,
                        contentDescription = if (uiState.isMapView) "List View" else "Map View"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search by name, specialty, or location...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VetSpecialty.values()) { specialty ->
                FilterChip(
                    selected = specialty in uiState.selectedSpecialties,
                    onClick = { viewModel.toggleSpecialtyFilter(specialty) },
                    label = { Text(specialty.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = specialty.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency SOS Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { viewModel.callEmergencyVet() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🚨 Emergency Vet Help",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "24/7 Emergency Line: Call Now",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            ErrorMessage(
                error = uiState.error!!,
                onRetry = { viewModel.loadVets() }
            )
        } else {
            if (uiState.isMapView) {
                MapView(
                    vets = uiState.filteredVets,
                    onVetClick = { viewModel.selectVet(it) }
                )
            } else {
                VetList(
                    vets = uiState.filteredVets,
                    onVetClick = { viewModel.selectVet(it) },
                    onCallClick = { viewModel.callVet(it) },
                    onNavigateClick = { viewModel.navigateToVet(it) },
                    onBookClick = { viewModel.bookAppointment(it) }
                )
            }
        }
    }

    // Selected Vet Details Dialog
    uiState.selectedVet?.let { selectedVet ->
        VetDetailsDialog(
            vet = selectedVet,
            onDismiss = { viewModel.clearSelection() },
            onCall = { viewModel.callVet(selectedVet) },
            onNavigate = { viewModel.navigateToVet(selectedVet) },
            onBook = { viewModel.bookAppointment(selectedVet) }
        )
    }
}

@Composable
private fun VetList(
    vets: List<VetClinic>,
    onVetClick: (VetClinic) -> Unit,
    onCallClick: (VetClinic) -> Unit,
    onNavigateClick: (VetClinic) -> Unit,
    onBookClick: (VetClinic) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(vets) { vet ->
            VetCard(
                vet = vet,
                onClick = { onVetClick(vet) },
                onCallClick = { onCallClick(vet) },
                onNavigateClick = { onNavigateClick(vet) },
                onBookClick = { onBookClick(vet) }
            )
        }
    }
}

@Composable
private fun VetCard(
    vet: VetClinic,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onNavigateClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = vet.address,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Distance
                    Text(
                        text = "${vet.distanceKm} km",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = vet.rating.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specialties
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(vet.specialties) { specialty ->
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = specialty.displayName,
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = specialty.icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status and Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Open status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (vet.isOpenNow) Color.Green else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (vet.isOpenNow) "Open Now" else "Closed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (vet.isOpenNow) Color.Green else Color.Red
                    )
                }

                Text(
                    text = vet.operatingHours,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onNavigateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate", fontSize = 12.sp)
                }

                Button(
                    onClick = onBookClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.BookOnline,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Book", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MapView(
    vets: List<VetClinic>,
    onVetClick: (VetClinic) -> Unit
) {
    // Placeholder for map implementation
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🗺️ Map View",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Showing ${vets.size} veterinary clinics near you",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🚧 Google Maps integration coming soon!\nFor now, use the list view below.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun VetDetailsDialog(
    vet: VetClinic,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onNavigate: () -> Unit,
    onBook: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(vet.name) },
        text = {
            Column {
                Text("📍 ${vet.address}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("⭐ Rating: ${vet.rating}/5.0")
                Spacer(modifier = Modifier.height(8.dp))
                Text("📞 ${vet.phone}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("🕒 ${vet.operatingHours}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Specialties:")
                vet.specialties.forEach { specialty ->
                    Text("• ${specialty.displayName}")
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onCall) { Text("Call") }
                TextButton(onClick = onNavigate) { Text("Navigate") }
                TextButton(onClick = onBook) { Text("Book") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// Data Models
data class VetClinic(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val rating: Float,
    val distanceKm: Float,
    val isOpenNow: Boolean,
    val operatingHours: String,
    val specialties: List<VetSpecialty>,
    val emergencyService: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class VetSpecialty(val displayName: String, val icon: ImageVector) {
    GENERAL("General Care", Icons.Default.Pets),
    EMERGENCY("Emergency", Icons.Default.Emergency),
    SURGERY("Surgery", Icons.Default.MedicalServices),
    DENTISTRY("Dentistry", Icons.Default.ContentCut),
    CARDIOLOGY("Cardiology", Icons.Default.Favorite),
    DERMATOLOGY("Skin Care", Icons.Default.Healing),
    ONCOLOGY("Cancer Care", Icons.Default.Science),
    NEUROLOGY("Neurology", Icons.Default.Psychology),
    ORTHOPEDICS("Bone & Joint", Icons.Default.Accessibility),
    EXOTIC("Exotic Pets", Icons.Default.BugReport)
}