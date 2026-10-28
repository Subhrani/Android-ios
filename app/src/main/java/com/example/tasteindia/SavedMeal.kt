package com.example.tasteindia

data class SavedMeal(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isSample: Boolean,
    val details: MealDetails? = null
)
