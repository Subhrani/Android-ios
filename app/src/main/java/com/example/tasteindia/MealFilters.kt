package com.example.tasteindia

/**
 * Keeps meals from the Indian base list that match every active ID filter.
 *
 * A null set means that filter is not active.
 * An empty set means the active filter returned no matches.
 */
fun intersectMealFilters(
    indianMeals: List<Meal>,
    categoryIds: Set<String>?,
    ingredientIds: Set<String>?
): List<Meal> {
    return indianMeals.filter { meal ->
        val matchesCategory = categoryIds == null || meal.id in categoryIds
        val matchesIngredient = ingredientIds == null || meal.id in ingredientIds

        matchesCategory && matchesIngredient
    }
}