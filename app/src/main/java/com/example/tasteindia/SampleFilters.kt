package com.example.tasteindia

/**
 * Invented filter data for the offline review examples.
 * These IDs do not represent live TheMealDB meals.
 */
object SampleFilters {

    val categories = listOf("Chicken", "Vegetarian")

    val ingredients = listOf("Chicken", "Chickpeas", "Rice")

    fun categoryIds(category: String): Set<String> {
        return when (category) {
            "Chicken" -> setOf("test-1")
            "Vegetarian" -> setOf("test-2")
            else -> emptySet()
        }
    }

    fun ingredientIds(ingredient: String): Set<String> {
        return when (ingredient) {
            "Chicken" -> setOf("test-1")
            "Chickpeas" -> setOf("test-2")
            "Rice" -> setOf("test-1")
            else -> emptySet()
        }
    }
}