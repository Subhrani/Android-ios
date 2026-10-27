package com.example.tasteindia

import java.io.IOException

class MealRepository(
    private val api: MealApi
) {

    suspend fun getCategoryNames(): List<String> {
        val response = api.getCategories()

        if (!response.isSuccessful) {
            throw IOException("Could not load categories.")
        }

        val body = response.body()
            ?: throw IOException("The category response was empty.")

        return body.meals.orEmpty()
            .map { it.strCategory.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    suspend fun getIngredientNames(): List<String> {
        val response = api.getIngredients()

        if (!response.isSuccessful) {
            throw IOException("Could not load ingredients.")
        }

        val body = response.body()
            ?: throw IOException("The ingredient response was empty.")

        return body.meals.orEmpty()
            .map { it.strIngredient.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }

    suspend fun getCategoryMealIds(category: String): Set<String> {
        val response = api.getMealsByCategory(category)

        if (!response.isSuccessful) {
            throw IOException("Could not filter by category.")
        }

        val body = response.body()
            ?: throw IOException("The category filter response was empty.")

        return body.meals.orEmpty()
            .map { it.idMeal }
            .toSet()
    }

    suspend fun getIngredientMealIds(ingredient: String): Set<String> {
        val response = api.getMealsByIngredient(ingredient)

        if (!response.isSuccessful) {
            throw IOException("Could not filter by ingredient.")
        }

        val body = response.body()
            ?: throw IOException("The ingredient filter response was empty.")

        return body.meals.orEmpty()
            .map { it.idMeal }
            .toSet()
    }
}