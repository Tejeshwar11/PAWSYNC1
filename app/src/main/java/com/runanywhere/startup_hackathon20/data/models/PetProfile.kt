package com.runanywhere.startup_hackathon20.data.models

import java.util.*

data class PetProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val breed: String,
    val species: String, // Dog, Cat, Other
    val age: Int, // in years
    val weight: Float, // in kg
    val gender: String, // Male, Female, Neutered Male, Spayed Female
    val photoUri: String? = null,
    val medicalHistory: List<String> = emptyList(),
    val vaccinationRecords: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val microchipId: String? = null,
    val ownerName: String,
    val ownerPhone: String,
    val emergencyContact: String? = null,
    val emergencyPhone: String? = null,
    val vetClinicName: String? = null,
    val vetPhone: String? = null,
    val birthDate: Long? = null,
    val adoptionDate: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PetSpecies {
    DOG, CAT, BIRD, RABBIT, OTHER
}

enum class PetGender {
    MALE, FEMALE, NEUTERED_MALE, SPAYED_FEMALE, UNKNOWN
}