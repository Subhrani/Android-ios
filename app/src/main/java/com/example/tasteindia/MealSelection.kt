package com.example.tasteindia

import java.util.Locale

/** Search is applied locally; the latest query is used on every state update. */
fun selectMeals(
    meals: List<Meal>,
    query: String,
    ascending: Boolean,
    categoryIds: Set<String>? = null,
    ingredientIds: Set<String>? = null,
    favouritesOnly: Boolean = false,
    favouriteIds: Set<String> = emptySet()
): List<Meal> {
    val matches = intersectMealFilters(meals, categoryIds, ingredientIds).filter {
        it.name.contains(query.trim(), ignoreCase = true) &&
            (!favouritesOnly || it.id in favouriteIds)
    }
    val comparator = compareBy<Meal> { it.name.lowercase(Locale.ROOT) }.thenBy { it.id }
    return matches.sortedWith(if (ascending) comparator else comparator.reversed())
}
