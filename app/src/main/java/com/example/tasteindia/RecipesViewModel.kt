package com.example.tasteindia

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface RecipesUiState {
    data object Loading : RecipesUiState

    data class Success(
        val meals: List<Meal>,
        val isSample: Boolean = false
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

    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadRecipes()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Load the real Indian meal list from TheMealDB.
    fun loadRecipes() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
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

    // Load the saved sample JSON for offline review.
    fun loadSampleRecipes(context: Context) {
        val appContext = context.applicationContext

        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = RecipesUiState.Loading

            try {
                val meals = withContext(Dispatchers.IO) {
                    val json = appContext.assets
                        .open("indian_meals.json")
                        .bufferedReader()
                        .use { it.readText() }

                    val moshi = Moshi.Builder()
                        .addLast(KotlinJsonAdapterFactory())
                        .build()

                    val response = requireNotNull(
                        moshi.adapter(MealsResponse::class.java)
                            .fromJson(json)
                    )

                    response.meals.orEmpty().map { mealDto ->
                        Meal(
                            id = mealDto.idMeal,
                            name = mealDto.strMeal,
                            imageUrl = mealDto.strMealThumb.orEmpty()
                        )
                    }
                }

                _uiState.value = RecipesUiState.Success(
                    meals = meals,
                    isSample = true
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = RecipesUiState.Error(
                    "Could not load the sample data."
                )
            }
        }
    }
}