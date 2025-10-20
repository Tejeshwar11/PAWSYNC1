package com.runanywhere.startup_hackathon20.presentation.vet

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class VetFinderUiState(
    val vets: List<VetClinic> = emptyList(),
    val filteredVets: List<VetClinic> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedSpecialties: Set<VetSpecialty> = emptySet(),
    val isMapView: Boolean = false,
    val selectedVet: VetClinic? = null
)

class VetFinderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VetFinderUiState())
    val uiState: StateFlow<VetFinderUiState> = _uiState.asStateFlow()

    init {
        loadVets()
    }

    fun loadVets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Simulate loading mock data
                val mockVets = generateMockVets()
                _uiState.value = _uiState.value.copy(
                    vets = mockVets,
                    filteredVets = mockVets,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load veterinary clinics"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterVets()
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
        filterVets()
    }

    fun toggleSpecialtyFilter(specialty: VetSpecialty) {
        val currentSpecialties = _uiState.value.selectedSpecialties.toMutableSet()
        if (specialty in currentSpecialties) {
            currentSpecialties.remove(specialty)
        } else {
            currentSpecialties.add(specialty)
        }
        _uiState.value = _uiState.value.copy(selectedSpecialties = currentSpecialties)
        filterVets()
    }

    fun toggleMapView() {
        _uiState.value = _uiState.value.copy(isMapView = !_uiState.value.isMapView)
    }

    fun selectVet(vet: VetClinic) {
        _uiState.value = _uiState.value.copy(selectedVet = vet)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedVet = null)
    }

    fun callVet(vet: VetClinic) {
        // This would typically open the phone dialer
        // For demo purposes, we'll simulate the action
    }

    fun navigateToVet(vet: VetClinic) {
        // This would typically open Google Maps with directions
        // For demo purposes, we'll simulate the action
    }

    fun bookAppointment(vet: VetClinic) {
        // This would typically open booking interface
        // For demo purposes, we'll simulate the action
    }

    fun callEmergencyVet() {
        // This would call emergency vet services
        // For demo purposes, we'll simulate the action
    }

    private fun filterVets() {
        val query = _uiState.value.searchQuery.lowercase()
        val specialties = _uiState.value.selectedSpecialties

        val filtered = _uiState.value.vets.filter { vet ->
            val matchesQuery = query.isEmpty() ||
                    vet.name.lowercase().contains(query) ||
                    vet.address.lowercase().contains(query) ||
                    vet.specialties.any { it.displayName.lowercase().contains(query) }

            val matchesSpecialty = specialties.isEmpty() ||
                    vet.specialties.any { it in specialties }

            matchesQuery && matchesSpecialty
        }

        _uiState.value = _uiState.value.copy(filteredVets = filtered)
    }

    private fun generateMockVets(): List<VetClinic> {
        return listOf(
            VetClinic(
                id = "1",
                name = "PawCare Veterinary Clinic",
                address = "123 Pet Street, Animal City, AC 12345",
                phone = "+1-555-0123",
                rating = 4.8f,
                distanceKm = 1.2f,
                isOpenNow = true,
                operatingHours = "8:00 AM - 8:00 PM",
                specialties = listOf(VetSpecialty.GENERAL, VetSpecialty.EMERGENCY),
                emergencyService = true
            ),
            VetClinic(
                id = "2",
                name = "Animal Health Center",
                address = "456 Veterinary Ave, Pet Town, PT 67890",
                phone = "+1-555-0456",
                rating = 4.6f,
                distanceKm = 2.5f,
                isOpenNow = true,
                operatingHours = "7:00 AM - 9:00 PM",
                specialties = listOf(
                    VetSpecialty.SURGERY,
                    VetSpecialty.CARDIOLOGY,
                    VetSpecialty.DENTISTRY
                )
            ),
            VetClinic(
                id = "3",
                name = "Companion Pet Hospital",
                address = "789 Care Road, Healing Hills, HH 11111",
                phone = "+1-555-0789",
                rating = 4.9f,
                distanceKm = 3.8f,
                isOpenNow = false,
                operatingHours = "9:00 AM - 6:00 PM",
                specialties = listOf(
                    VetSpecialty.ORTHOPEDICS,
                    VetSpecialty.NEUROLOGY,
                    VetSpecialty.ONCOLOGY
                )
            ),
            VetClinic(
                id = "4",
                name = "24/7 Emergency Vet",
                address = "999 Emergency Blvd, Urgent City, UC 99999",
                phone = "+1-555-0999",
                rating = 4.5f,
                distanceKm = 5.1f,
                isOpenNow = true,
                operatingHours = "24 Hours",
                specialties = listOf(VetSpecialty.EMERGENCY, VetSpecialty.SURGERY),
                emergencyService = true
            ),
            VetClinic(
                id = "5",
                name = "Exotic Pet Specialists",
                address = "321 Unique Pets Lane, Special City, SC 33333",
                phone = "+1-555-0321",
                rating = 4.7f,
                distanceKm = 4.2f,
                isOpenNow = true,
                operatingHours = "10:00 AM - 7:00 PM",
                specialties = listOf(VetSpecialty.EXOTIC, VetSpecialty.DERMATOLOGY)
            ),
            VetClinic(
                id = "6",
                name = "Family Pet Care Center",
                address = "654 Family Road, Home Town, HT 44444",
                phone = "+1-555-0654",
                rating = 4.4f,
                distanceKm = 6.7f,
                isOpenNow = false,
                operatingHours = "8:00 AM - 5:00 PM",
                specialties = listOf(VetSpecialty.GENERAL, VetSpecialty.DENTISTRY)
            )
        )
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VetFinderViewModel::class.java)) {
                return VetFinderViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}