package com.example.tasteindia

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealResponseTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `parses meal list from offline fixture`() {
        val fixture = requireNotNull(
            javaClass.getResourceAsStream("/fixtures/indian_meals.json")
        ).bufferedReader().use { it.readText() }

        val response = requireNotNull(
            moshi.adapter(MealsResponse::class.java).fromJson(fixture)
        )

        val meals = requireNotNull(response.meals)

        assertEquals(2, meals.size)
        assertEquals("test-1", meals[0].idMeal)
        assertEquals("Sample Biryani", meals[0].strMeal)
        assertNull(meals[0].strMealThumb)
    }

    @Test
    fun `parses null meals response`() {
        val response = requireNotNull(
            moshi.adapter(MealsResponse::class.java)
                .fromJson("""{"meals":null}""")
        )

        assertNull(response.meals)
    }
}