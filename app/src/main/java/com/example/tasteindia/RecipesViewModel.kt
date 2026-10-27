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

data class RecipeFilterState(
    val category: String? = null,
    val ingredient: String? = null,
    val categoryIds: Set<String>? = null,
    val ingredientIds: Set<String>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FilterChoices(
    val categories: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RecipesViewModel(
    private val api: MealApi = MealApiClient.service
) : ViewModel() {

    private val repository = MealRepository(api)

    private val _uiState =
        MutableStateFlow<RecipesUiState>(RecipesUiState.Loading)

    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _ascending = MutableStateFlow(true)

    val ascending: StateFlow<Boolean> = _ascending.asStateFlow()

    private val _filters = MutableStateFlow(RecipeFilterState())

    val filters: StateFlow<RecipeFilterState> = _filters.asStateFlow()

    private val _filterChoices = MutableStateFlow(FilterChoices())

    val filterChoices: StateFlow<FilterChoices> =
        _filterChoices.asStateFlow()

    private var loadJob: Job? = null
    private var filterJob: Job? = null
    private var choicesJob: Job? = null

    init {
        loadRecipes()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAscending(value: Boolean) {
        _ascending.value = value
    }

    // Load the live Indian meal list from TheMealDB.
    fun loadRecipes() {
        loadJob?.cancel()
        filterJob?.cancel()
        choicesJob?.cancel()

        _filters.value = RecipeFilterState()
        _filterChoices.value = FilterChoices()

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
        filterJob?.cancel()
        choicesJob?.cancel()

        _filters.value = RecipeFilterState()
        _filterChoices.value = FilterChoices()

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

    // Load names that can be displayed in the filter controls.
    fun loadFilterChoices() {
        val currentList =
            _uiState.value as? RecipesUiState.Success ?: return

        choicesJob?.cancel()

        if (currentList.isSample) {
            _filterChoices.value = FilterChoices(
                categories = SampleFilters.categories,
                ingredients = SampleFilters.ingredients
            )
            return
        }

        choicesJob = viewModelScope.launch {
            _filterChoices.value = FilterChoices(isLoading = true)

            try {
                val categories = repository.getCategoryNames()
                val ingredients = repository.getIngredientNames()

                _filterChoices.value = FilterChoices(
                    categories = categories,
                    ingredients = ingredients
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _filterChoices.value = FilterChoices(
                    error = "Could not load filter choices. Please try again."
                )
            }
        }
    }

    // Find IDs matching the selected category and ingredient.
    fun applyFilters(category: String?, ingredient: String?) {
        val currentList =
            _uiState.value as? RecipesUiState.Success ?: return

        val selectedCategory = category
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val selectedIngredient = ingredient
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        filterJob?.cancel()

        // Sample mode uses the invented IDs in SampleFilters.
        if (currentList.isSample) {
            _filters.value = RecipeFilterState(
                category = selectedCategory,
                ingredient = selectedIngredient,
                categoryIds = selectedCategory?.let {
                    SampleFilters.categoryIds(it)
                },
                ingredientIds = selectedIngredient?.let {
                    SampleFilters.ingredientIds(it)
                }
            )
            return
        }

        // Live mode requests matching IDs from TheMealDB.
        filterJob = viewModelScope.launch {
            _filters.value = _filters.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val categoryIds = selectedCategory?.let {
                    repository.getCategoryMealIds(it)
                }

                val ingredientIds = selectedIngredient?.let {
                    repository.getIngredientMealIds(it)
                }

                _filters.value = RecipeFilterState(
                    category = selectedCategory,
                    ingredient = selectedIngredient,
                    categoryIds = categoryIds,
                    ingredientIds = ingredientIds
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                _filters.value = _filters.value.copy(
                    isLoading = false,
                    error = "Could not apply filters. Check your connection and try again."
                )
            } catch (exception: Exception) {
                _filters.value = _filters.value.copy(
                    isLoading = false,
                    error = "Could not apply filters. Please try again."
                )
            }
        }
    }

    fun clearFilters() {
        filterJob?.cancel()
        _filters.value = RecipeFilterState()
    }
}