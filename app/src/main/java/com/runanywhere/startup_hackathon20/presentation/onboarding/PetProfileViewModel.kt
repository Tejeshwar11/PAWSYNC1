package com.runanywhere.startup_hackathon20.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.models.PetProfile
import com.runanywhere.startup_hackathon20.data.repositories.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PetProfileUiState(
    val photoUri: String? = null,
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val gender: String = "",
    val vaccinations: List<String> = emptyList(),
    val allergies: String = "",
    val chronicConditions: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val vetClinicName: String = "",
    val vetPhone: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class PetProfileViewModel(
    private val petRepository: PetRepository = PetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetProfileUiState())
    val uiState: StateFlow<PetProfileUiState> = _uiState.asStateFlow()

    fun updatePhotoUri(uri: String) {
        _uiState.value = _uiState.value.copy(photoUri = uri)
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updateSpecies(species: String) {
        _uiState.value = _uiState.value.copy(species = species)
    }

    fun updateBreed(breed: String) {
        _uiState.value = _uiState.value.copy(breed = breed)
    }

    fun updateAge(age: Int) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun updateWeight(weight: Float) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateVaccinations(vaccinations: List<String>) {
        _uiState.value = _uiState.value.copy(vaccinations = vaccinations)
    }

    fun updateAllergies(allergies: String) {
        _uiState.value = _uiState.value.copy(allergies = allergies)
    }

    fun updateChronicConditions(conditions: String) {
        _uiState.value = _uiState.value.copy(chronicConditions = conditions)
    }

    fun updateOwnerName(name: String) {
        _uiState.value = _uiState.value.copy(ownerName = name)
    }

    fun updateOwnerPhone(phone: String) {
        _uiState.value = _uiState.value.copy(ownerPhone = phone)
    }

    fun updateEmergencyContact(contact: String) {
        _uiState.value = _uiState.value.copy(emergencyContact = contact)
    }

    fun updateEmergencyPhone(phone: String) {
        _uiState.value = _uiState.value.copy(emergencyPhone = phone)
    }

    fun updateVetClinicName(name: String) {
        _uiState.value = _uiState.value.copy(vetClinicName = name)
    }

    fun updateVetPhone(phone: String) {
        _uiState.value = _uiState.value.copy(vetPhone = phone)
    }

    fun isStepValid(step: Int): Boolean {
        val state = _uiState.value
        return when (step) {
            1 -> true // Photo is optional
            2 -> state.name.isNotBlank() && state.species.isNotBlank() && state.breed.isNotBlank()
            3 -> state.age > 0 && state.weight > 0 && state.gender.isNotBlank()
            4 -> true // Medical history is optional
            5 -> state.ownerName.isNotBlank() && state.ownerPhone.isNotBlank()
            else -> false
        }
    }

    fun createProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val state = _uiState.value
                val petProfile = PetProfile(
                    name = state.name,
                    breed = state.breed,
                    species = state.species,
                    age = state.age,
                    weight = state.weight,
                    gender = state.gender,
                    photoUri = state.photoUri,
                    medicalHistory = if (state.chronicConditions.isNotBlank())
                        listOf(state.chronicConditions) else emptyList(),
                    vaccinationRecords = state.vaccinations,
                    allergies = if (state.allergies.isNotBlank())
                        state.allergies.split(",").map { it.trim() } else emptyList(),
                    ownerName = state.ownerName,
                    ownerPhone = state.ownerPhone,
                    emergencyContact = state.emergencyContact.ifBlank { null },
                    emergencyPhone = state.emergencyPhone.ifBlank { null },
                    vetClinicName = state.vetClinicName.ifBlank { null },
                    vetPhone = state.vetPhone.ifBlank { null }
                )

                petRepository.insertPet(petProfile)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create profile: ${e.message}"
                )
            }
        }
    }
}