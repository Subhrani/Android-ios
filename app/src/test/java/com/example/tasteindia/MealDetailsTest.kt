package com.example.tasteindia

import org.junit.Assert.assertEquals
import org.junit.Test

class MealDetailsTest {

    @Test
    fun `pairs ingredients and measures and skips blank pairs`() {
        val fields = mapOf<String, Any?>(
            "idMeal" to "test-1",
            "strMeal" to "Sample Recipe",
            "strIngredient1" to "  Rice  ",
            "strMeasure1" to "  1 cup  ",
            "strIngredient2" to "",
            "strMeasure2" to " ",
            "strIngredient3" to "Salt",
            "strMeasure3" to null,
            "strIngredient4" to null,
            "strMeasure4" to "a pinch",
            "strTags" to " Dinner, , Spicy "
        )

        val details = mapMealDetails(fields)

        assertEquals("test-1", details.id)
        assertEquals("Sample Recipe", details.name)

        assertEquals(
            listOf(
                IngredientMeasure("Rice", "1 cup"),
                IngredientMeasure("Salt", ""),
                IngredientMeasure("", "a pinch")
            ),
            details.ingredients
        )

        assertEquals(listOf("Dinner", "Spicy"), details.tags)
    }

    @Test
    fun `preserves instruction line breaks`() {
        val fields = mapOf<String, Any?>(
            "strInstructions" to "  Step one.\nStep two.  "
        )

        val details = mapMealDetails(fields)

        assertEquals(
            "Step one.\nStep two.",
            details.instructions
        )
    }
}