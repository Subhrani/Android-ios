package com.example.tasteindia

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FavouritesRecordsTest {
    @Test fun `summary update retains offline details and avoids duplicates`() {
        val details = mapMealDetails(mapOf("idMeal" to "1", "strMeal" to "Biryani"))
        val saved = SavedMeal("1", "Biryani", "", false, details)
        val updated = mergeFavourite(listOf(saved), saved.copy(details = null))
        assertEquals(1, updated.size)
        assertEquals(details, updated.single().details)
    }
    @Test fun `live and sample identities remain independent when removing`() {
        val live = SavedMeal("1", "Live", "", false)
        val sample = SavedMeal("1", "Sample", "", true)
        val records = mergeFavourite(listOf(live), sample)
        assertEquals(listOf(sample), removeFavourite(records, "1", false))
    }
    @Test fun `persisted JSON restores complete offline details`() {
        val adapter = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            .adapter<List<SavedMeal>>(Types.newParameterizedType(List::class.java, SavedMeal::class.java))
        val saved = SavedMeal("1", "Biryani", "", false,
            mapMealDetails(mapOf("idMeal" to "1", "strIngredient1" to "Rice", "strMeasure1" to "1 cup")))
        val restored = requireNotNull(adapter.fromJson(adapter.toJson(listOf(saved))))
        assertEquals(saved, restored.single())
        assertNotNull(restored.single().details)
    }
}
