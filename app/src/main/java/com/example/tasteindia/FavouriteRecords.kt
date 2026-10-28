package com.example.tasteindia

/** The sample/live namespace prevents fixture IDs from colliding with real IDs. */
fun mergeFavourite(current: List<SavedMeal>, meal: SavedMeal): List<SavedMeal> {
    val previous = current.firstOrNull { it.id == meal.id && it.isSample == meal.isSample }
    return current.filterNot { it.id == meal.id && it.isSample == meal.isSample } +
        meal.copy(details = meal.details ?: previous?.details)
}

fun removeFavourite(current: List<SavedMeal>, id: String, isSample: Boolean): List<SavedMeal> =
    current.filterNot { it.id == id && it.isSample == isSample }
