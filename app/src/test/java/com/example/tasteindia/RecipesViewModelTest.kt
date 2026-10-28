package com.example.tasteindia

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RecipesViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun `older filter response cannot replace latest selection`() = runTest(main.dispatcher) {
        val api = FakeMealApi()
        api.category = { category ->
            if (category == "Old") withContext(NonCancellable) { delay(500) }
            Response.success(MealsResponse(listOf(MealDto(if (category == "Old") "1" else "2", category, null))))
        }
        val viewModel = RecipesViewModel(repository = MealRepository(api))
        advanceUntilIdle()
        viewModel.applyFilters("Old", null)
        runCurrent()
        viewModel.applyFilters("Latest", null)
        advanceUntilIdle()
        assertEquals("Latest", viewModel.filters.value.category)
        assertEquals(setOf("2"), viewModel.filters.value.categoryIds)
    }

    @Test fun `saved state restores query sort and applied filters`() = runTest(main.dispatcher) {
        val handle = SavedStateHandle(mapOf(
            "query" to "biryani", "ascending" to false,
            "category" to "Chicken", "categoryIds" to arrayListOf("1")
        ))
        val viewModel = RecipesViewModel(repository = MealRepository(FakeMealApi()), savedState = handle)
        advanceUntilIdle()
        assertEquals("biryani", viewModel.searchQuery.value)
        assertEquals(false, viewModel.ascending.value)
        assertEquals("Chicken", viewModel.filters.value.category)
        assertEquals(setOf("1"), viewModel.filters.value.categoryIds)
        viewModel.updateSearchQuery("chana")
        assertEquals("chana", handle.get<String>("query"))
    }
}
