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

sealed interface MealDetailsUiState {
    data object Loading : MealDetailsUiState

    data class Success(
        val details: MealDetails,
        val isSample: Boolean
    ) : MealDetailsUiState

    data class Error(
        val message: String
    ) : MealDetailsUiState
}

class MealDetailsViewModel(
    private val api: MealApi = MealApiClient.service
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<MealDetailsUiState>(MealDetailsUiState.Loading)

    val uiState: StateFlow<MealDetailsUiState> =
        _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadDetails(
        mealId: String,
        isSample: Boolean,
        context: Context
    ) {
        val appContext = context.applicationContext

        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            _uiState.value = MealDetailsUiState.Loading

            try {
                val fields = if (isSample) {
                    loadSampleFields(mealId, appContext)
                } else {
                    loadLiveFields(mealId)
                }

                if (fields == null) {
                    _uiState.value = MealDetailsUiState.Error(
                        "Recipe details were not found."
                    )
                    return@launch
                }

                _uiState.value = MealDetailsUiState.Success(
                    details = mapMealDetails(fields),
                    isSample = isSample
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                _uiState.value = MealDetailsUiState.Error(
                    "Could not load details. Check your connection and try again."
                )
            } catch (exception: Exception) {
                _uiState.value = MealDetailsUiState.Error(
                    "Could not load recipe details. Please try again."
                )
            }
        }
    }

    private suspend fun loadLiveFields(
        mealId: String
    ): Map<String, Any?>? {
        val response = api.getMealDetails(mealId)

        if (!response.isSuccessful) {
            throw IOException("The recipe service returned an error.")
        }

        val body = response.body()
            ?: throw IOException("The recipe service returned an empty response.")

        return body.meals.orEmpty()
            .firstOrNull { it["idMeal"] == mealId }
    }

    private suspend fun loadSampleFields(
        mealId: String,
        context: Context
    ): Map<String, Any?>? {
        return withContext(Dispatchers.IO) {
            val json = context.assets
                .open("sample_meal_details.json")
                .bufferedReader()
                .use { it.readText() }

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val response = requireNotNull(
                moshi.adapter(MealDetailsResponse::class.java)
                    .fromJson(json)
            )

            response.meals.orEmpty()
                .firstOrNull { it["idMeal"] == mealId }
        }
    }
}