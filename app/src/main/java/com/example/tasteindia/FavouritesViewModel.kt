package com.example.tasteindia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class FavouritesUiState(
    val meals: List<SavedMeal> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

class FavouritesViewModel(
    private val store: FavouritesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavouritesUiState())

    val uiState: StateFlow<FavouritesUiState> =
        _uiState.asStateFlow()

    private val writeMutex = Mutex()

    init {
        observeFavourites()
    }

    private fun observeFavourites() {
        viewModelScope.launch {
            try {
                store.favourites.collect { meals ->
                    _uiState.update { it.copy(meals = meals, isLoading = false) }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Could not read saved favourites.") }
            }
        }
    }

    fun setFavourite(
        meal: SavedMeal,
        shouldSave: Boolean
    ) {
        viewModelScope.launch {
            writeMutex.withLock {
                _uiState.update { it.copy(isSaving = true, error = null) }

                try {
                    if (shouldSave) {
                        store.save(meal)
                    } else {
                        store.remove(
                            mealId = meal.id,
                            isSample = meal.isSample
                        )
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _uiState.update { it.copy(error = "Could not update favourites. Please try again.") }
                } finally {
                    _uiState.update { it.copy(isSaving = false) }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

class FavouritesViewModelFactory(
    private val store: FavouritesStore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(FavouritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavouritesViewModel(store) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}