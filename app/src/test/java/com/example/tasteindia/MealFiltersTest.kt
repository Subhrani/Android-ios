package com.example.tasteindia

import org.junit.Assert.assertEquals
import org.junit.Test

class MealFiltersTest {

    private val indianMeals = listOf(
        Meal("1", "Meal One", ""),
        Meal("2", "Meal Two", ""),
        Meal("3", "Meal Three", "")
    )

    @Test
    fun `combined filters keep only matching Indian meals`() {
        val result = intersectMealFilters(
            indianMeals = indianMeals,
            categoryIds = setOf("2", "3", "4"),
            ingredientIds = setOf("3", "4", "5")
        )

        assertEquals(listOf("3"), result.map { it.id })
    }

    @Test
    fun `no active filters keep all Indian meals`() {
        val result = intersectMealFilters(
            indianMeals = indianMeals,
            categoryIds = null,
            ingredientIds = null
        )

        assertEquals(listOf("1", "2", "3"), result.map { it.id })
    }

    @Test
    fun `active filter with no matches returns empty list`() {
        val result = intersectMealFilters(
            indianMeals = indianMeals,
            categoryIds = emptySet(),
            ingredientIds = null
        )

        assertEquals(emptyList<Meal>(), result)
    }
}