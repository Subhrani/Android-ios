package com.example.tasteindia

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MealRepositoryTest {
    @Test fun `concurrent same ID requests share successful cached lookup`() = runTest {
        val api = FakeMealApi()
        var calls = 0
        api.detail = { id ->
            calls++
            delay(100)
            Response.success(MealDetailsResponse(listOf(mapOf("idMeal" to id))))
        }
        val repository = MealRepository(api)
        val first = async { repository.getDetails("1") }
        val second = async { repository.getDetails("1") }
        assertEquals(first.await(), second.await())
        assertEquals(1, calls)
        repository.getDetails("1")
        assertEquals(1, calls)
    }
    @Test fun `different ID lookups are bounded to one at a time`() = runTest {
        val api = FakeMealApi()
        var active = 0
        var maximum = 0
        api.detail = { id ->
            active++
            maximum = maxOf(maximum, active)
            delay(100)
            active--
            Response.success(MealDetailsResponse(listOf(mapOf("idMeal" to id))))
        }
        val repository = MealRepository(api)
        val first = async { repository.getDetails("1") }
        val second = async { repository.getDetails("2") }
        first.await(); second.await()
        assertEquals(1, maximum)
    }
    @Test fun `HTTP failure is explicit and is not cached`() = runTest {
        val api = FakeMealApi()
        var calls = 0
        api.indian = {
            calls++
            if (calls == 1) Response.error(503, "unavailable".toResponseBody())
            else Response.success(MealsResponse(listOf(MealDto("1", "Biryani", null))))
        }
        val repository = MealRepository(api)
        val error = runCatching { repository.getIndianMeals() }.exceptionOrNull()
        assertTrue(error is MealHttpException)
        assertEquals(503, (error as MealHttpException).status)
        assertEquals("1", repository.getIndianMeals().single().id)
        assertEquals(2, calls)
    }
}
