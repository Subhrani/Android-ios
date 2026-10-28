package com.example.tasteindia

import org.junit.Assert.assertEquals
import org.junit.Test

class MealMetadataTest {
    @Test fun `retains image credits modification and additional meaningful fields`() {
        val details = mapMealDetails(mapOf(
            "strImageSource" to "Photographer credit", "dateModified" to "2026-10-28",
            "strCreativeCommonsConfirmed" to "Yes", "strServingSuggestion" to "Serve warm",
            "unused" to null, "strIngredient20" to "Salt"
        ))
        assertEquals("Photographer credit", details.imageSourceUrl)
        assertEquals("2026-10-28", details.dateModified)
        assertEquals("Yes", details.creativeCommons)
        assertEquals(mapOf("Serving Suggestion" to "Serve warm"), details.additionalFields)
        assertEquals("Salt", details.ingredients.single().ingredient)
    }
}
