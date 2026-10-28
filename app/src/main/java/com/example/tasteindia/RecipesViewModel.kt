package com.example.tasteindia

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipesUiState {
    data object Loading : RecipesUiState
    data class Success(val meals: List<Meal>, val isSample: Boolean = false) : RecipesUiState
    data object Empty : RecipesUiState
    data class Error(val message: String) : RecipesUiState
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
    private val repository: MealRepository = AppRepositories.meals,
    private val samples: SampleSource? = null,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private val _uiState = MutableStateFlow<RecipesUiState>(RecipesUiState.Loading)
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()
    val searchQuery: StateFlow<String> = savedState.getStateFlow("query", "")
    val ascending: StateFlow<Boolean> = savedState.getStateFlow("ascending", true)
    private val _filters = MutableStateFlow(RecipeFilterState(
        category = savedState["category"], ingredient = savedState["ingredient"],
        categoryIds = savedState.get<ArrayList<String>>("categoryIds")?.toSet(),
        ingredientIds = savedState.get<ArrayList<String>>("ingredientIds")?.toSet()
    ))
    val filters: StateFlow<RecipeFilterState> = _filters.asStateFlow()
    private val _filterChoices = MutableStateFlow(FilterChoices())
    val filterChoices: StateFlow<FilterChoices> = _filterChoices.asStateFlow()
    private var loadJob: Job? = null
    private var filterJob: Job? = null
    private var choicesJob: Job? = null
    private var loadRevision = 0
    private var filterRevision = 0

    init { load(savedState["sample"] ?: false, samples) }

    fun updateSearchQuery(query: String) { savedState["query"] = query }
    fun setAscending(value: Boolean) { savedState["ascending"] = value }
    fun loadRecipes() = load(false, samples, forceRefresh = true)
    fun loadSampleRecipes(context: Context) = load(true, samples ?: AssetSampleSource(context))

    private fun load(sample: Boolean, source: SampleSource?, forceRefresh: Boolean = false) {
        loadJob?.cancel()
        filterJob?.cancel()
        choicesJob?.cancel()
        filterRevision++
        val revision = ++loadRevision
        if (sample != (savedState["sample"] ?: false)) clearFilters()
        savedState["sample"] = sample
        _filters.value = _filters.value.copy(isLoading = false, error = null)
        _filterChoices.value = FilterChoices()
        loadJob = viewModelScope.launch {
            _uiState.value = RecipesUiState.Loading
            try {
                val meals = if (sample) requireNotNull(source).meals() else repository.getIndianMeals(forceRefresh)
                ensureActive()
                if (revision == loadRevision) {
                    _uiState.value = if (meals.isEmpty()) RecipesUiState.Empty
                        else RecipesUiState.Success(meals, sample)
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (revision == loadRevision) _uiState.value = RecipesUiState.Error(
                    if (sample) "Could not load the bundled sample data." else recipeError(e)
                )
            }
        }
    }

    fun loadFilterChoices() {
        val current = _uiState.value as? RecipesUiState.Success ?: return
        if (_filterChoices.value.categories.isNotEmpty()) return
        choicesJob?.cancel()
        if (current.isSample) {
            _filterChoices.value = FilterChoices(SampleFilters.categories, SampleFilters.ingredients)
            return
        }
        val revision = loadRevision
        choicesJob = viewModelScope.launch {
            _filterChoices.value = FilterChoices(isLoading = true)
            try {
                val categories = repository.getCategoryNames()
                val ingredients = repository.getIngredientNames()
                ensureActive()
                if (revision == loadRevision) _filterChoices.value = FilterChoices(categories, ingredients)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (revision == loadRevision) _filterChoices.value = FilterChoices(error = recipeError(e))
            }
        }
    }

    fun applyFilters(category: String?, ingredient: String?) {
        val current = _uiState.value as? RecipesUiState.Success ?: return
        val selectedCategory = category?.trim()?.takeIf { it.isNotEmpty() }
        val selectedIngredient = ingredient?.trim()?.takeIf { it.isNotEmpty() }
        filterJob?.cancel()
        val revision = ++filterRevision
        if (selectedCategory == null && selectedIngredient == null) { clearFilters(); return }
        filterJob = viewModelScope.launch {
            _filters.value = _filters.value.copy(isLoading = true, error = null)
            try {
                val categoryIds = selectedCategory?.let {
                    if (current.isSample) SampleFilters.categoryIds(it) else repository.getCategoryMealIds(it)
                }
                val ingredientIds = selectedIngredient?.let {
                    if (current.isSample) SampleFilters.ingredientIds(it) else repository.getIngredientMealIds(it)
                }
                ensureActive()
                if (revision == filterRevision) {
                    val state = RecipeFilterState(selectedCategory, selectedIngredient, categoryIds, ingredientIds)
                    _filters.value = state
                    saveFilters(state)
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (revision == filterRevision) _filters.value = _filters.value.copy(
                    isLoading = false, error = recipeError(e)
                )
            }
        }
    }

    fun clearFilters() {
        filterJob?.cancel()
        filterRevision++
        _filters.value = RecipeFilterState()
        saveFilters(_filters.value)
    }

    private fun saveFilters(state: RecipeFilterState) {
        savedState["category"] = state.category
        savedState["ingredient"] = state.ingredient
        savedState["categoryIds"] = state.categoryIds?.let { ArrayList(it) }
        savedState["ingredientIds"] = state.ingredientIds?.let { ArrayList(it) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                RecipesViewModel(
                    samples = AssetSampleSource(checkNotNull(this[APPLICATION_KEY])),
                    savedState = createSavedStateHandle()
                )
            }
        }
    }
}
