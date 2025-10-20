@file:OptIn(ExperimentalMaterial3Api::class)

package com.runanywhere.startup_hackathon20.presentation.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.runanywhere.startup_hackathon20.data.models.PetProfile

@Composable
fun PetProfileScreen(
    onProfileCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: PetProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = 5

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Top bar with back button and progress
            TopBarWithProgress(
                currentStep = currentStep,
                totalSteps = totalSteps,
                onBack = {
                    if (currentStep > 1) currentStep-- else onBack()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                }
            ) { step ->
                when (step) {
                    1 -> Step1PhotoPicker(
                        photoUri = uiState.photoUri,
                        onPhotoSelected = viewModel::updatePhotoUri
                    )

                    2 -> Step2BasicInfo(
                        name = uiState.name,
                        species = uiState.species,
                        breed = uiState.breed,
                        onNameChange = viewModel::updateName,
                        onSpeciesChange = viewModel::updateSpecies,
                        onBreedChange = viewModel::updateBreed
                    )

                    3 -> Step3PhysicalStats(
                        age = uiState.age,
                        weight = uiState.weight,
                        gender = uiState.gender,
                        onAgeChange = viewModel::updateAge,
                        onWeightChange = viewModel::updateWeight,
                        onGenderChange = viewModel::updateGender
                    )

                    4 -> Step4MedicalHistory(
                        vaccinations = uiState.vaccinations,
                        allergies = uiState.allergies,
                        chronicConditions = uiState.chronicConditions,
                        onVaccinationsChange = viewModel::updateVaccinations,
                        onAllergiesChange = viewModel::updateAllergies,
                        onChronicConditionsChange = viewModel::updateChronicConditions
                    )

                    5 -> Step5EmergencyContact(
                        ownerName = uiState.ownerName,
                        ownerPhone = uiState.ownerPhone,
                        emergencyContact = uiState.emergencyContact,
                        emergencyPhone = uiState.emergencyPhone,
                        vetClinicName = uiState.vetClinicName,
                        vetPhone = uiState.vetPhone,
                        onOwnerNameChange = viewModel::updateOwnerName,
                        onOwnerPhoneChange = viewModel::updateOwnerPhone,
                        onEmergencyContactChange = viewModel::updateEmergencyContact,
                        onEmergencyPhoneChange = viewModel::updateEmergencyPhone,
                        onVetClinicNameChange = viewModel::updateVetClinicName,
                        onVetPhoneChange = viewModel::updateVetPhone
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (currentStep == 1)
                    Arrangement.End else Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Previous")
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            viewModel.createProfile(onProfileCreated)
                        }
                    },
                    modifier = Modifier.weight(if (currentStep == 1) 2f else 1f),
                    enabled = viewModel.isStepValid(currentStep),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    )
                ) {
                    Text(
                        if (currentStep == totalSteps) "Create Profile" else "Next",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00BCD4))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Creating your pet's profile...")
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarWithProgress(
    currentStep: Int,
    totalSteps: Int,
    onBack: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF00BCD4)
                )
            }

            Text(
                text = "Pet Profile Setup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BCD4)
            )

            Text(
                text = "$currentStep/$totalSteps",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps },
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF00BCD4),
            trackColor = Color(0xFF00BCD4).copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun Step1PhotoPicker(
    photoUri: String?,
    onPhotoSelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📸 Add Your Pet's Photo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Help us recognize your furry friend",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Photo picker button
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5))
                .clickable {
                    // Mock photo selection
                    onPhotoSelected("mock_pet_photo_uri")
                },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap to add photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                // Mock photo display
                Text(
                    "🐕",
                    fontSize = 80.sp
                )
            }
        }

        if (photoUri != null) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { onPhotoSelected("mock_pet_photo_uri_2") }
            ) {
                Text("Change Photo")
            }
        }
    }
}

@Composable
private fun Step2BasicInfo(
    name: String,
    species: String,
    breed: String,
    onNameChange: (String) -> Unit,
    onSpeciesChange: (String) -> Unit,
    onBreedChange: (String) -> Unit
) {
    Column {
        Text(
            text = "🐾 Basic Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Pet Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("🏷️") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Species dropdown
        var expandedSpecies by remember { mutableStateOf(false) }
        val speciesOptions = listOf("Dog", "Cat", "Bird", "Rabbit", "Other")

        ExposedDropdownMenuBox(
            expanded = expandedSpecies,
            onExpandedChange = { expandedSpecies = it }
        ) {
            OutlinedTextField(
                value = species,
                onValueChange = {},
                readOnly = true,
                label = { Text("Species") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpecies) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = { Text("🐕") }
            )

            ExposedDropdownMenu(
                expanded = expandedSpecies,
                onDismissRequest = { expandedSpecies = false }
            ) {
                speciesOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSpeciesChange(option)
                            expandedSpecies = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = breed,
            onValueChange = onBreedChange,
            label = { Text("Breed") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("🧬") },
            placeholder = { Text("e.g., Golden Retriever") }
        )
    }
}

@Composable
private fun Step3PhysicalStats(
    age: Int,
    weight: Float,
    gender: String,
    onAgeChange: (Int) -> Unit,
    onWeightChange: (Float) -> Unit,
    onGenderChange: (String) -> Unit
) {
    Column {
        Text(
            text = "📏 Physical Stats",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Age slider
        Text("Age: $age years", fontWeight = FontWeight.Medium)
        Slider(
            value = age.toFloat(),
            onValueChange = { onAgeChange(it.toInt()) },
            valueRange = 0f..25f,
            steps = 24,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF00BCD4))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weight input
        OutlinedTextField(
            value = if (weight == 0f) "" else weight.toString(),
            onValueChange = {
                val newWeight = it.toFloatOrNull() ?: 0f
                onWeightChange(newWeight)
            },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("⚖️") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gender selection
        Text("Gender", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        val genderOptions = listOf("Male", "Female", "Neutered Male", "Spayed Female")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(100.dp)
        ) {
            items(genderOptions.size) { index ->
                val option = genderOptions[index]
                FilterChip(
                    selected = gender == option,
                    onClick = { onGenderChange(option) },
                    label = { Text(option, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun Step4MedicalHistory(
    vaccinations: List<String>,
    allergies: String,
    chronicConditions: String,
    onVaccinationsChange: (List<String>) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onChronicConditionsChange: (String) -> Unit
) {
    Column {
        Text(
            text = "🏥 Medical History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Vaccinations checklist
        Text("Vaccinations", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))

        val vaccinationOptions = listOf("Rabies", "DHPP", "Bordetella", "Lyme Disease")

        vaccinationOptions.forEach { vaccination ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = vaccinations.contains(vaccination),
                    onCheckedChange = { checked ->
                        val updated = if (checked) {
                            vaccinations + vaccination
                        } else {
                            vaccinations - vaccination
                        }
                        onVaccinationsChange(updated)
                    }
                )
                Text(vaccination)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = allergies,
            onValueChange = onAllergiesChange,
            label = { Text("Known Allergies") },
            placeholder = { Text("List any known allergies") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("🤧") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = chronicConditions,
            onValueChange = onChronicConditionsChange,
            label = { Text("Chronic Conditions") },
            placeholder = { Text("Any ongoing health conditions") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("💊") }
        )
    }
}

@Composable
private fun Step5EmergencyContact(
    ownerName: String,
    ownerPhone: String,
    emergencyContact: String,
    emergencyPhone: String,
    vetClinicName: String,
    vetPhone: String,
    onOwnerNameChange: (String) -> Unit,
    onOwnerPhoneChange: (String) -> Unit,
    onEmergencyContactChange: (String) -> Unit,
    onEmergencyPhoneChange: (String) -> Unit,
    onVetClinicNameChange: (String) -> Unit,
    onVetPhoneChange: (String) -> Unit
) {
    Column {
        Text(
            text = "📞 Emergency Contacts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Owner info
        Text("Owner Information", fontWeight = FontWeight.Medium, color = Color(0xFF00BCD4))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ownerName,
            onValueChange = onOwnerNameChange,
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("👤") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ownerPhone,
            onValueChange = onOwnerPhoneChange,
            label = { Text("Your Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("📱") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency contact
        Text("Emergency Contact", fontWeight = FontWeight.Medium, color = Color(0xFFFF7043))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = emergencyContact,
            onValueChange = onEmergencyContactChange,
            label = { Text("Emergency Contact Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("🚨") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = emergencyPhone,
            onValueChange = onEmergencyPhoneChange,
            label = { Text("Emergency Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Text("☎️") }
        )
    }
}