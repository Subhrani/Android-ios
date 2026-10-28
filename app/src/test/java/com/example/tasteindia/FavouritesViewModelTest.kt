package com.example.tasteindia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private class MemoryStore : FavouritesStore {
        override val favourites = MutableStateFlow<List<SavedMeal>>(emptyList())
        override suspend fun save(meal: SavedMeal) { favourites.value = mergeFavourite(favourites.value, meal) }
        override suspend fun remove(mealId: String, isSample: Boolean) {
            favourites.value = removeFavourite(favourites.value, mealId, isSample)
        }
    }
    @Test fun `new view model restores saved records and removal updates both screens`() = runTest(main.dispatcher) {
        val store = MemoryStore()
        val owners = listOf(ViewModelStore(), ViewModelStore())
        try {
            val first = ViewModelProvider(owners[0], FavouritesViewModelFactory(store))[FavouritesViewModel::class.java]
            first.setFavourite(SavedMeal("1", "Biryani", "", false), true)
            advanceUntilIdle()
            val second = ViewModelProvider(owners[1], FavouritesViewModelFactory(store))[FavouritesViewModel::class.java]
            advanceUntilIdle()
            assertEquals("1", second.uiState.value.meals.single().id)
            second.setFavourite(second.uiState.value.meals.single(), false)
            advanceUntilIdle()
            assertEquals(emptyList<SavedMeal>(), first.uiState.value.meals)
        } finally { owners.forEach { it.clear() } }
    }
}
