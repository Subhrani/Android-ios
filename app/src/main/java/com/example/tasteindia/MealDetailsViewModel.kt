package com.example.tasteindia

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MealDetailsUiState {
    data object Loading : MealDetailsUiState
    data class Success(val details: MealDetails, val isSample: Boolean) : MealDetailsUiState
    data class Error(val message: String) : MealDetailsUiState
}

class MealDetailsViewModel(
    private val repository: MealRepository = AppRepositories.meals
) : ViewModel() {
    private val _uiState = MutableStateFlow<MealDetailsUiState>(MealDetailsUiState.Loading)
    val uiState: StateFlow<MealDetailsUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private var revision = 0

    fun loadDetails(mealId: String, isSample: Boolean, context: Context) {
        val source = AssetSampleSource(context.applicationContext)
        loadJob?.cancel()
        val request = ++revision
        loadJob = viewModelScope.launch {
            _uiState.value = MealDetailsUiState.Loading
            try {
                val details = if (isSample) source.details(mealId) else repository.getDetails(mealId)
                ensureActive()
                if (request == revision) _uiState.value = MealDetailsUiState.Success(details, isSample)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (request == revision) _uiState.value = MealDetailsUiState.Error(recipeError(e))
            }
        }
    }
}
