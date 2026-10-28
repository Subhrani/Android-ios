package com.example.tasteindia

import org.junit.Assert.assertEquals
import org.junit.Test

class MealSelectionTest {
    private val meals = listOf(Meal("2", "Chana Masala", ""), Meal("1", "Biryani", ""))
    @Test fun `latest local query replaces previous results without remote work`() {
        assertEquals(listOf("1"), selectMeals(meals, "bir", true).map { it.id })
        assertEquals(listOf("2"), selectMeals(meals, " CHANA ", true).map { it.id })
        assertEquals(emptyList<Meal>(), selectMeals(meals, "missing", true))
        assertEquals(listOf("2", "1"), selectMeals(meals, "", false).map { it.id })
    }
    @Test fun `search category ingredient and favourites all intersect`() {
        val result = selectMeals(meals, "a", true, setOf("1", "2", "foreign"),
            setOf("2", "foreign"), true, setOf("2"))
        assertEquals(listOf("2"), result.map { it.id })
    }
}
