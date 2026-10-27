package com.example.tasteindia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// The possible states of the recipe list.
sealed interface RecipesUiState {
    data object Loading : RecipesUiState

    data class Success(
        val meals: List<Meal>
    ) : RecipesUiState

    data object Empty : RecipesUiState

    data class Error(
        val message: String
    ) : RecipesUiState
}

class RecipesViewModel(
    private val api: MealApi = MealApiClient.service
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<RecipesUiState>(RecipesUiState.Loading)

    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = RecipesUiState.Loading

            try {
                val response = api.getMealsByArea("Indian")

                if (!response.isSuccessful) {
                    _uiState.value = RecipesUiState.Error(
                        "The recipe service returned an error. Please try again."
                    )
                    return@launch
                }

                val body = response.body()

                if (body == null) {
                    _uiState.value = RecipesUiState.Error(
                        "The recipe service returned an empty response. Please try again."
                    )
                    return@launch
                }

                val meals = body.meals.orEmpty().map { mealDto ->
                    Meal(
                        id = mealDto.idMeal,
                        name = mealDto.strMeal,
                        imageUrl = mealDto.strMealThumb.orEmpty()
                    )
                }

                _uiState.value = if (meals.isEmpty()) {
                    RecipesUiState.Empty
                } else {
                    RecipesUiState.Success(meals)
                }
            } catch (exception: CancellationException) {
                // Let the coroutine cancel normally.
                throw exception
            } catch (exception: IOException) {
                _uiState.value = RecipesUiState.Error(
                    "Could not connect. Check your internet and try again."
                )
            } catch (exception: Exception) {
                _uiState.value = RecipesUiState.Error(
                    "Could not load recipes. Please try again."
                )
            }
        }
    }
}