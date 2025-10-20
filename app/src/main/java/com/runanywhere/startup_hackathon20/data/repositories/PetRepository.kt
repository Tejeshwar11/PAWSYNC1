package com.runanywhere.startup_hackathon20.data.repositories

import com.runanywhere.startup_hackathon20.data.models.PetProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class PetRepository {

    // In-memory storage
    private val pets = ConcurrentHashMap<String, PetProfile>()
    private val _petsFlow = MutableStateFlow<List<PetProfile>>(emptyList())

    // Basic CRUD Operations
    suspend fun insertPet(pet: PetProfile): Long {
        pets[pet.id] = pet
        updatePetsFlow()
        return System.currentTimeMillis()
    }

    suspend fun updatePet(pet: PetProfile) {
        pets[pet.id] = pet
        updatePetsFlow()
    }

    suspend fun deletePet(pet: PetProfile) {
        pets.remove(pet.id)
        updatePetsFlow()
    }

    suspend fun deletePetById(petId: String) {
        pets.remove(petId)
        updatePetsFlow()
    }

    // Retrieve Operations
    suspend fun getPetById(petId: String): PetProfile? = pets[petId]

    fun getPetByIdFlow(petId: String): Flow<PetProfile?> =
        _petsFlow.asStateFlow().map { petsList -> petsList.find { it.id == petId } }

    suspend fun getAllPets(): Flow<List<PetProfile>> = _petsFlow.asStateFlow()

    suspend fun getAllPetsList(): List<PetProfile> = pets.values.toList()

    // Search Operations
    suspend fun searchPetsByName(query: String): List<PetProfile> =
        pets.values.filter { it.name.contains(query, ignoreCase = true) }

    suspend fun getPetsBySpecies(species: String): List<PetProfile> =
        pets.values.filter { it.species.equals(species, ignoreCase = true) }

    suspend fun getPetsByBreed(breed: String): List<PetProfile> =
        pets.values.filter { it.breed.contains(breed, ignoreCase = true) }

    // Statistics
    suspend fun getPetCount(): Int = pets.size

    suspend fun getPetsByOwner(ownerName: String): List<PetProfile> =
        pets.values.filter { it.ownerName.contains(ownerName, ignoreCase = true) }

    // Utility Methods
    suspend fun petExists(petId: String): Boolean = pets.containsKey(petId)

    suspend fun getOldestPet(): PetProfile? = pets.values.maxByOrNull { it.age }

    suspend fun getYoungestPet(): PetProfile? = pets.values.minByOrNull { it.age }

    suspend fun getPetsByAgeRange(minAge: Int, maxAge: Int): List<PetProfile> =
        pets.values.filter { it.age in minAge..maxAge }

    suspend fun getAverageAge(): Double =
        if (pets.isEmpty()) 0.0 else pets.values.map { it.age }.average()

    // Vaccination and Medical History
    suspend fun getPetsWithMedicalHistory(): List<PetProfile> =
        pets.values.filter { it.medicalHistory.isNotEmpty() }

    suspend fun getPetsWithVaccinations(): List<PetProfile> =
        pets.values.filter { it.vaccinationRecords.isNotEmpty() }

    suspend fun getPetsByMedicalCondition(condition: String): List<PetProfile> =
        pets.values.filter { pet ->
            pet.medicalHistory.any { it.contains(condition, ignoreCase = true) }
        }

    // Export/Import (for backup functionality)
    suspend fun exportAllPets(): List<PetProfile> = pets.values.toList()

    suspend fun importPets(petsList: List<PetProfile>) {
        pets.clear()
        petsList.forEach { pets[it.id] = it }
        updatePetsFlow()
    }

    suspend fun clearAllPets() {
        pets.clear()
        updatePetsFlow()
    }

    // Weight and Physical Stats
    suspend fun getAverageWeight(): Double =
        if (pets.isEmpty()) 0.0 else pets.values.map { it.weight }.average()

    suspend fun getHeaviestPet(): PetProfile? = pets.values.maxByOrNull { it.weight }

    suspend fun getLightestPet(): PetProfile? = pets.values.minByOrNull { it.weight }

    suspend fun getPetsByWeightRange(minWeight: Float, maxWeight: Float): List<PetProfile> =
        pets.values.filter { it.weight in minWeight..maxWeight }

    // Gender Statistics
    suspend fun getMalePets(): List<PetProfile> =
        pets.values.filter { it.gender.equals("Male", ignoreCase = true) }

    suspend fun getFemalePets(): List<PetProfile> =
        pets.values.filter { it.gender.equals("Female", ignoreCase = true) }

    suspend fun getGenderRatio(): Pair<Int, Int> {
        val males = getMalePets().size
        val females = getFemalePets().size
        return Pair(males, females)
    }

    // Emergency Contact Information
    suspend fun getPetsWithEmergencyContact(): List<PetProfile> =
        pets.values.filter { it.emergencyContact?.isNotEmpty() == true }

    suspend fun updateEmergencyContact(petId: String, emergencyContact: String) {
        pets[petId]?.let { pet ->
            val updatedPet = pet.copy(emergencyContact = emergencyContact)
            pets[petId] = updatedPet
            updatePetsFlow()
        }
    }

    // Microchip Operations
    suspend fun getPetByMicrochip(microchipId: String): PetProfile? =
        pets.values.find { it.microchipId == microchipId }

    suspend fun isPetMicrochipped(petId: String): Boolean =
        pets[petId]?.microchipId?.isNotEmpty() == true

    suspend fun getPetsWithMicrochip(): List<PetProfile> =
        pets.values.filter { it.microchipId?.isNotEmpty() == true }

    // Breed Statistics
    suspend fun getBreedDistribution(): Map<String, Int> =
        pets.values.groupBy { it.breed }.mapValues { it.value.size }

    suspend fun getMostPopularBreed(): String? =
        getBreedDistribution().maxByOrNull { it.value }?.key

    suspend fun getSpeciesDistribution(): Map<String, Int> =
        pets.values.groupBy { it.species }.mapValues { it.value.size }

    // Photo Operations
    suspend fun updatePetPhoto(petId: String, photoUri: String) {
        pets[petId]?.let { pet ->
            val updatedPet = pet.copy(photoUri = photoUri)
            pets[petId] = updatedPet
            updatePetsFlow()
        }
    }

    suspend fun getPetsWithPhotos(): List<PetProfile> =
        pets.values.filter { it.photoUri?.isNotEmpty() == true }

    suspend fun getPetsWithoutPhotos(): List<PetProfile> =
        pets.values.filter { it.photoUri?.isEmpty() == true }

    // Batch Operations
    suspend fun updateMultiplePets(petsToUpdate: List<PetProfile>) {
        petsToUpdate.forEach { pets[it.id] = it }
        updatePetsFlow()
    }

    suspend fun deleteMultiplePets(petIds: List<String>) {
        petIds.forEach { pets.remove(it) }
        updatePetsFlow()
    }

    // Validation
    suspend fun validatePetData(petId: String): List<String> {
        val pet = pets[petId] ?: return listOf("Pet not found")
        val errors = mutableListOf<String>()

        if (pet.name.isBlank()) errors.add("Pet name is required")
        if (pet.species.isBlank()) errors.add("Species is required")
        if (pet.breed.isBlank()) errors.add("Breed is required")
        if (pet.age < 0) errors.add("Age cannot be negative")
        if (pet.weight <= 0) errors.add("Weight must be positive")
        if (pet.ownerName.isBlank()) errors.add("Owner name is required")

        return errors
    }

    private fun updatePetsFlow() {
        _petsFlow.value = pets.values.toList()
    }
}