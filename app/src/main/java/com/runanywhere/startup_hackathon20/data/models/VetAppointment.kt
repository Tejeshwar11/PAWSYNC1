package com.runanywhere.startup_hackathon20.data.models

import java.util.*

data class VetAppointment(
    val appointmentId: String = UUID.randomUUID().toString(),
    val petId: String,
    val appointmentDateTime: Long, // Unix timestamp for appointment date/time

    // Veterinary Information
    val veterinarianName: String,
    val clinicName: String,
    val clinicAddress: String,
    val clinicPhone: String,
    val clinicEmail: String? = null,

    // Appointment Details
    val appointmentType: String = AppointmentType.CHECKUP.name,
    val reasonForVisit: String,
    val symptoms: List<String> = emptyList(),
    val duration: Int = 30, // in minutes
    val urgencyLevel: String = VetUrgencyLevel.ROUTINE.name,

    // Status and Tracking
    val status: String = AppointmentStatus.SCHEDULED.name,
    val isRecurring: Boolean = false,
    val recurringInterval: String? = null, // "monthly", "yearly", etc.

    // Preparation and Instructions
    val preparationInstructions: List<String> = emptyList(),
    val documentsTobring: List<String> = emptyList(),
    val fastingRequired: Boolean = false,
    val medicationsToStop: List<String> = emptyList(),

    // Cost and Insurance
    val estimatedCost: Float? = null,
    val insuranceProvider: String? = null,
    val insurancePolicy: String? = null,
    val copayAmount: Float? = null,

    // Follow-up and Results
    val diagnosis: String? = null,
    val treatment: String? = null,
    val prescriptions: List<String> = emptyList(),
    val nextAppointmentDate: Long? = null,
    val followUpInstructions: List<String> = emptyList(),

    // Documentation
    val veterinaryNotes: String? = null,
    val testResults: List<String> = emptyList(),
    val xrayImages: List<String> = emptyList(),
    val bloodworkResults: String? = null,

    // Billing and Payment
    val actualCost: Float? = null,
    val paymentStatus: String = PaymentStatus.PENDING.name,
    val invoiceNumber: String? = null,
    val insuranceClaimStatus: String? = null,

    // Reminders and Notifications
    val reminderSet: Boolean = true,
    val reminderTime: Long? = null, // Time before appointment to remind
    val notificationsSent: List<String> = emptyList(),

    // Quality and Feedback
    val clinicRating: Int? = null, // 1-5 stars
    val veterinarianRating: Int? = null, // 1-5 stars
    val experienceFeedback: String? = null,
    val wouldRecommend: Boolean? = null,

    // Administrative
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null, // User who created the appointment
    val cancellationReason: String? = null,
    val rescheduledFrom: Long? = null,

    // Integration Data
    val externalAppointmentId: String? = null, // From clinic's system
    val syncedWithCalendar: Boolean = false,
    val reminderMethodPreference: List<String> = listOf("PUSH_NOTIFICATION"), // EMAIL, SMS, PUSH_NOTIFICATION
    val isEmergency: Boolean = false
)

enum class AppointmentType {
    ROUTINE, CHECKUP, VACCINATION, EMERGENCY, SURGERY,
    DENTAL, GROOMING, FOLLOWUP, CONSULTATION, SPECIALIST,
    TELEHEALTH, LAB_WORK, IMAGING, THERAPY, OTHER
}

enum class AppointmentStatus {
    SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED,
    RESCHEDULED, NO_SHOW, WAITING
}

enum class PaymentStatus {
    PENDING, PAID, PARTIALLY_PAID, OVERDUE, DISPUTED, REFUNDED
}

enum class VetUrgencyLevel {
    ROUTINE, URGENT, EMERGENCY
}

// Data class for vet clinic information
data class VetClinic(
    val clinicId: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val phone: String,
    val email: String? = null,
    val website: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val operatingHours: String? = null,
    val specializations: List<String> = emptyList(),
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isEmergencyClinic: Boolean = false,
    val is24Hours: Boolean = false,
    val acceptsInsurance: Boolean = false,
    val services: List<String> = emptyList()
)

// Data class for veterinarian information
data class Veterinarian(
    val vetId: String = UUID.randomUUID().toString(),
    val name: String,
    val title: String, // "Dr.", "DVM"
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val yearsExperience: Int? = null,
    val clinicId: String,
    val phone: String? = null,
    val email: String? = null,
    val biography: String? = null,
    val photo: String? = null,
    val rating: Float = 0f,
    val consultationFee: Float? = null,
    val availableSlots: List<String> = emptyList(),
    val languages: List<String> = emptyList()
)