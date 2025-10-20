package com.runanywhere.startup_hackathon20.data.models

import java.util.*

data class NutritionPlan(
    val planId: String = UUID.randomUUID().toString(),
    val petId: String,
    val planName: String = "Custom Nutrition Plan",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val isActive: Boolean = true,

    // Daily Nutrition Goals
    val dailyCalories: Int,
    val dailyProteinGrams: Float,
    val dailyCarbsGrams: Float,
    val dailyFatGrams: Float,
    val dailyFiberGrams: Float = 0f,
    val dailyWaterMl: Int,

    // Meal Structure
    val mealsPerDay: Int = 2,
    val meals: List<MealPlan> = emptyList(),
    val treats: List<TreatPlan> = emptyList(),

    // Dietary Restrictions & Preferences
    val dietaryRestrictions: List<String> = emptyList(), // Grain-free, No chicken, etc.
    val specialDietary: List<DietaryRequirement> = emptyList(),
    val foodBrand: String? = null,
    val preferredFoodType: FoodType = FoodType.DRY_KIBBLE,

    // Feeding Schedule
    val feedingSchedule: List<FeedingTime> = emptyList(),
    val automaticFeeder: Boolean = false,
    val portionControl: Boolean = true,

    // Health Considerations
    val weightGoal: WeightGoal = WeightGoal.MAINTAIN,
    val activityLevel: NutritionActivityLevel = NutritionActivityLevel.MODERATE,
    val healthConditions: List<String> = emptyList(),
    val supplements: List<Supplement> = emptyList(),

    // Monitoring & Progress
    val currentWeight: Float? = null,
    val targetWeight: Float? = null,
    val weeklyWeightLog: List<WeightEntry> = emptyList(),
    val nutritionLog: List<NutritionEntry> = emptyList(),

    // Vet Recommendations
    val vetApproved: Boolean = false,
    val vetNotes: String? = null,
    val nextReviewDate: Long? = null,

    // Metadata
    val createdBy: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val version: Int = 1
)

data class MealPlan(
    val mealId: String = UUID.randomUUID().toString(),
    val mealName: String, // Breakfast, Lunch, Dinner
    val scheduledTime: String, // "07:00", "12:00", "18:00"
    val foodItems: List<FoodItem> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val portionSizeGrams: Float,
    val notes: String? = null
)

data class FoodItem(
    val itemId: String = UUID.randomUUID().toString(),
    val foodName: String,
    val brand: String? = null,
    val foodType: FoodType,
    val quantityGrams: Float,
    val caloriesPerGram: Float,
    val proteinPerGram: Float,
    val carbsPerGram: Float,
    val fatPerGram: Float,
    val ingredients: List<String> = emptyList(),
    val allergens: List<String> = emptyList(),
    val expiryDate: Long? = null
)

data class TreatPlan(
    val treatId: String = UUID.randomUUID().toString(),
    val treatName: String,
    val maxDailyAmount: Int, // number of treats
    val caloriesPerTreat: Int,
    val occasions: List<TreatOccasion> = emptyList(),
    val healthyAlternatives: List<String> = emptyList()
)

data class FeedingTime(
    val timeId: String = UUID.randomUUID().toString(),
    val time: String, // "07:30"
    val mealType: String, // "Breakfast", "Dinner"
    val portionSize: Float, // in grams
    val reminderEnabled: Boolean = true
)

data class Supplement(
    val supplementId: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: String,
    val frequency: String, // "Daily", "Twice daily", "Weekly"
    val purpose: String, // "Joint health", "Digestive support"
    val startDate: Long,
    val endDate: Long? = null,
    val vetPrescribed: Boolean = false
)

data class WeightEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val weight: Float, // in kg
    val timestamp: Long,
    val notes: String? = null,
    val measuredBy: String? = null
)

data class NutritionEntry(
    val entryId: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val mealType: String,
    val foodConsumed: List<FoodItem> = emptyList(),
    val actualCalories: Int,
    val waterIntakeMl: Int = 0,
    val notes: String? = null,
    val photoUri: String? = null
)

data class DietaryRequirement(
    val requirementId: String = UUID.randomUUID().toString(),
    val type: DietaryType,
    val description: String,
    val severity: RequirementSeverity = RequirementSeverity.MODERATE,
    val vetRecommended: Boolean = false
)

enum class FoodType {
    DRY_KIBBLE,
    WET_CANNED,
    RAW_DIET,
    HOMEMADE,
    FREEZE_DRIED,
    DEHYDRATED,
    TREATS,
    SUPPLEMENTS
}

enum class WeightGoal {
    LOSE_WEIGHT,
    GAIN_WEIGHT,
    MAINTAIN,
    MUSCLE_BUILDING
}

enum class TreatOccasion {
    TRAINING,
    REWARD,
    BEDTIME,
    DENTAL_HEALTH,
    ANXIETY_RELIEF,
    MEDICATION_HIDING
}

enum class DietaryType {
    ALLERGY,
    INTOLERANCE,
    MEDICAL_CONDITION,
    PREFERENCE,
    RELIGIOUS,
    ETHICAL
}

enum class RequirementSeverity {
    MILD,
    MODERATE,
    SEVERE,
    LIFE_THREATENING
}

enum class NutritionActivityLevel {
    LOW,
    MODERATE,
    HIGH
}

// Utility Data Classes
data class NutritionSummary(
    val date: Long,
    val targetCalories: Int,
    val actualCalories: Int,
    val targetProtein: Float,
    val actualProtein: Float,
    val targetWater: Int,
    val actualWater: Int,
    val mealsCompleted: Int,
    val totalMeals: Int
)

data class WeeklyNutritionReport(
    val weekStart: Long,
    val weekEnd: Long,
    val averageCaloriesPerDay: Int,
    val weightChange: Float,
    val goalsMetPercentage: Float,
    val topFoods: List<String>,
    val recommendations: List<String>
)