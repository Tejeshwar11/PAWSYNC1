package com.runanywhere.startup_hackathon20.domain.usecases

import com.runanywhere.startup_hackathon20.data.models.*
import kotlin.math.roundToInt

class GenerateDietPlanUseCase {

    suspend fun invoke(
        petProfile: PetProfile,
        healthMetrics: HealthMetrics? = null,
        weightGoal: WeightGoal = WeightGoal.MAINTAIN,
        activityLevel: NutritionActivityLevel = NutritionActivityLevel.MODERATE
    ): NutritionPlan {

        // Calculate daily caloric needs based on pet characteristics
        val dailyCalories = calculateDailyCalories(
            petProfile = petProfile,
            activityLevel = activityLevel,
            weightGoal = weightGoal
        )

        // Calculate macronutrient distribution
        val macros = calculateMacronutrients(dailyCalories, petProfile.species)

        // Generate meal plans
        val meals = generateMealPlans(dailyCalories, petProfile)

        // Calculate water requirements
        val dailyWater = calculateWaterRequirements(petProfile.weight, activityLevel)

        // Generate feeding schedule
        val feedingSchedule = generateFeedingSchedule(meals.size)

        return NutritionPlan(
            petId = petProfile.id,
            planName = "Personalized Plan for ${petProfile.name}",
            dailyCalories = dailyCalories,
            dailyProteinGrams = macros.proteinGrams,
            dailyCarbsGrams = macros.carbsGrams,
            dailyFatGrams = macros.fatGrams,
            dailyFiberGrams = macros.fiberGrams,
            dailyWaterMl = dailyWater,
            mealsPerDay = meals.size,
            meals = meals,
            weightGoal = weightGoal,
            activityLevel = activityLevel,
            feedingSchedule = feedingSchedule,
            currentWeight = petProfile.weight,
            targetWeight = calculateTargetWeight(petProfile.weight, weightGoal),
            healthConditions = petProfile.chronicConditions
        )
    }

    private fun calculateDailyCalories(
        petProfile: PetProfile,
        activityLevel: NutritionActivityLevel,
        weightGoal: WeightGoal
    ): Int {
        // Base metabolic rate calculation
        val baseBMR = when (petProfile.species.lowercase()) {
            "dog" -> calculateDogBMR(petProfile.weight)
            "cat" -> calculateCatBMR(petProfile.weight)
            else -> calculateGenericBMR(petProfile.weight)
        }

        // Activity multiplier
        val activityMultiplier = when (activityLevel) {
            NutritionActivityLevel.LOW -> 1.2f
            NutritionActivityLevel.MODERATE -> 1.4f
            NutritionActivityLevel.HIGH -> 1.6f
        }

        // Weight goal adjustment
        val weightMultiplier = when (weightGoal) {
            WeightGoal.LOSE_WEIGHT -> 0.8f
            WeightGoal.GAIN_WEIGHT -> 1.2f
            WeightGoal.MAINTAIN -> 1.0f
            WeightGoal.MUSCLE_BUILDING -> 1.15f
        }

        // Age adjustment
        val ageMultiplier = when {
            petProfile.age < 1 -> 1.5f // Puppies/kittens need more
            petProfile.age > 7 -> 0.9f // Senior pets need less
            else -> 1.0f
        }

        return (baseBMR * activityMultiplier * weightMultiplier * ageMultiplier).roundToInt()
    }

    private fun calculateDogBMR(weightKg: Float): Float {
        // Dog BMR formula: 70 * (weight in kg)^0.75
        return 70f * Math.pow(weightKg.toDouble(), 0.75).toFloat()
    }

    private fun calculateCatBMR(weightKg: Float): Float {
        // Cat BMR formula: 70 * (weight in kg)^0.67
        return 70f * Math.pow(weightKg.toDouble(), 0.67).toFloat()
    }

    private fun calculateGenericBMR(weightKg: Float): Float {
        // Generic formula for other pets
        return 60f * Math.pow(weightKg.toDouble(), 0.7).toFloat()
    }

    private fun calculateMacronutrients(dailyCalories: Int, species: String): MacronutrientProfile {
        return when (species.lowercase()) {
            "dog" -> MacronutrientProfile(
                proteinGrams = (dailyCalories * 0.25f / 4f), // 25% protein, 4 cal/g
                fatGrams = (dailyCalories * 0.15f / 9f), // 15% fat, 9 cal/g
                carbsGrams = (dailyCalories * 0.50f / 4f), // 50% carbs, 4 cal/g
                fiberGrams = (dailyCalories * 0.05f / 2f) // 5% fiber, 2 cal/g
            )

            "cat" -> MacronutrientProfile(
                proteinGrams = (dailyCalories * 0.45f / 4f), // 45% protein (cats need more)
                fatGrams = (dailyCalories * 0.20f / 9f), // 20% fat
                carbsGrams = (dailyCalories * 0.25f / 4f), // 25% carbs (cats need less)
                fiberGrams = (dailyCalories * 0.03f / 2f) // 3% fiber
            )

            else -> MacronutrientProfile(
                proteinGrams = (dailyCalories * 0.20f / 4f),
                fatGrams = (dailyCalories * 0.10f / 9f),
                carbsGrams = (dailyCalories * 0.60f / 4f),
                fiberGrams = (dailyCalories * 0.05f / 2f)
            )
        }
    }

    private fun calculateWaterRequirements(
        weightKg: Float,
        activityLevel: NutritionActivityLevel
    ): Int {
        // Base water requirement: 50-60ml per kg body weight
        val baseWater = weightKg * 55f

        // Activity adjustment
        val activityMultiplier = when (activityLevel) {
            NutritionActivityLevel.LOW -> 1.0f
            NutritionActivityLevel.MODERATE -> 1.2f
            NutritionActivityLevel.HIGH -> 1.4f
        }

        return (baseWater * activityMultiplier).roundToInt()
    }

    private fun generateMealPlans(dailyCalories: Int, petProfile: PetProfile): List<MealPlan> {
        val mealsPerDay = when {
            petProfile.age < 1 -> 4 // Puppies/kittens eat more frequently
            petProfile.age > 7 -> 3 // Senior pets may need smaller, frequent meals
            else -> 2 // Adult pets typically eat twice daily
        }

        val caloriesPerMeal = dailyCalories / mealsPerDay
        val meals = mutableListOf<MealPlan>()

        val mealTimes = when (mealsPerDay) {
            4 -> listOf("07:00", "11:00", "15:00", "19:00")
            3 -> listOf("07:00", "13:00", "19:00")
            else -> listOf("07:00", "18:00")
        }

        val mealNames = listOf("Breakfast", "Lunch", "Afternoon Snack", "Dinner")

        for (i in 0 until mealsPerDay) {
            val foodItems = generateFoodItems(caloriesPerMeal, petProfile.species)
            val totalPortionSize = foodItems.sumOf { it.quantityGrams.toDouble() }.toFloat()

            meals.add(
                MealPlan(
                    mealName = mealNames.getOrElse(i) { "Meal ${i + 1}" },
                    scheduledTime = mealTimes.getOrElse(i) { "12:00" },
                    foodItems = foodItems,
                    totalCalories = caloriesPerMeal,
                    totalProtein = foodItems.sumOf { (it.proteinPerGram * it.quantityGrams).toDouble() }
                        .toFloat(),
                    totalCarbs = foodItems.sumOf { (it.carbsPerGram * it.quantityGrams).toDouble() }
                        .toFloat(),
                    totalFat = foodItems.sumOf { (it.fatPerGram * it.quantityGrams).toDouble() }
                        .toFloat(),
                    portionSizeGrams = totalPortionSize
                )
            )
        }

        return meals
    }

    private fun generateFoodItems(caloriesPerMeal: Int, species: String): List<FoodItem> {
        return when (species.lowercase()) {
            "dog" -> listOf(
                FoodItem(
                    foodName = "High-Quality Dry Kibble",
                    foodType = FoodType.DRY_KIBBLE,
                    quantityGrams = (caloriesPerMeal / 3.5f), // ~3.5 cal/g for quality kibble
                    caloriesPerGram = 3.5f,
                    proteinPerGram = 0.25f,
                    carbsPerGram = 0.45f,
                    fatPerGram = 0.12f
                )
            )

            "cat" -> listOf(
                FoodItem(
                    foodName = "Premium Cat Food",
                    foodType = FoodType.WET_CANNED,
                    quantityGrams = (caloriesPerMeal / 1.2f), // ~1.2 cal/g for wet food
                    caloriesPerGram = 1.2f,
                    proteinPerGram = 0.45f,
                    carbsPerGram = 0.25f,
                    fatPerGram = 0.20f
                )
            )

            else -> listOf(
                FoodItem(
                    foodName = "Balanced Pet Food",
                    foodType = FoodType.DRY_KIBBLE,
                    quantityGrams = (caloriesPerMeal / 3.0f),
                    caloriesPerGram = 3.0f,
                    proteinPerGram = 0.20f,
                    carbsPerGram = 0.50f,
                    fatPerGram = 0.10f
                )
            )
        }
    }

    private fun generateFeedingSchedule(mealsPerDay: Int): List<FeedingTime> {
        val times = when (mealsPerDay) {
            4 -> listOf("07:00", "11:00", "15:00", "19:00")
            3 -> listOf("07:00", "13:00", "19:00")
            else -> listOf("07:00", "18:00")
        }

        val mealTypes = listOf("Breakfast", "Lunch", "Afternoon Snack", "Dinner")

        return times.mapIndexed { index, time ->
            FeedingTime(
                time = time,
                mealType = mealTypes.getOrElse(index) { "Meal ${index + 1}" },
                portionSize = 100f, // This would be calculated based on meal plan
                reminderEnabled = true
            )
        }
    }

    private fun calculateTargetWeight(currentWeight: Float, weightGoal: WeightGoal): Float {
        return when (weightGoal) {
            WeightGoal.LOSE_WEIGHT -> currentWeight * 0.9f // 10% weight loss
            WeightGoal.GAIN_WEIGHT -> currentWeight * 1.1f // 10% weight gain
            WeightGoal.MAINTAIN -> currentWeight
            WeightGoal.MUSCLE_BUILDING -> currentWeight * 1.05f // 5% increase (muscle is denser)
        }
    }

    private data class MacronutrientProfile(
        val proteinGrams: Float,
        val fatGrams: Float,
        val carbsGrams: Float,
        val fiberGrams: Float
    )
}