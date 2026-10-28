package com.example.tasteindia

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MealDetailsRoute(
    mealId: String,
    isSample: Boolean,
    favouritesState: FavouritesUiState,
    onSetFavourite: (SavedMeal, Boolean) -> Unit,
    onBack: () -> Unit,
    viewModel: MealDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val networkState by viewModel.uiState.collectAsStateWithLifecycle()

    val savedMeal = favouritesState.meals.firstOrNull {
        it.id == mealId && it.isSample == isSample
    }

    val cachedDetails = savedMeal?.details

    LaunchedEffect(
        mealId,
        isSample,
        favouritesState.isLoading,
        cachedDetails != null
    ) {
        if (!favouritesState.isLoading && cachedDetails == null) {
            viewModel.loadDetails(mealId, isSample, context)
        }
    }

    val displayState = when {
        favouritesState.isLoading -> MealDetailsUiState.Loading
        cachedDetails != null -> MealDetailsUiState.Success(
            details = cachedDetails,
            isSample = isSample
        )
        else -> networkState
    }

    val loaded = networkState as? MealDetailsUiState.Success
    LaunchedEffect(savedMeal?.id, savedMeal?.details, loaded?.details) {
        if (savedMeal != null && savedMeal.details == null && loaded != null &&
            loaded.details.id == mealId && loaded.isSample == isSample &&
            favouritesState.error == null) {
            onSetFavourite(savedMeal.copy(details = loaded.details), true)
        }
    }

    MealDetailsScreen(
        uiState = displayState,
        isFavourite = savedMeal != null,
        favouritesState = favouritesState,
        onBack = onBack,
        onRetry = {
            viewModel.loadDetails(mealId, isSample, context)
        },
        onSetFavourite = onSetFavourite
    )
}

@Composable
fun MealDetailsScreen(
    uiState: MealDetailsUiState,
    isFavourite: Boolean,
    favouritesState: FavouritesUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSetFavourite: (SavedMeal, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Back")
        }

        favouritesState.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        when (uiState) {
            is MealDetailsUiState.Loading -> {
                CircularProgressIndicator()
                Text("Loading recipe details...")
            }

            is MealDetailsUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }

            is MealDetailsUiState.Success -> {
                val details = uiState.details

                if (uiState.isSample) {
                    Text(
                        text = "Sample data — offline review",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = details.name.ifBlank { "Recipe" },
                    style = MaterialTheme.typography.headlineMedium
                )

                RecipeImage(
                    imageUrl = details.imageUrl,
                    modifier = Modifier.fillMaxWidth(),
                    hero = true
                )

                Button(
                    enabled = !favouritesState.isLoading &&
                            !favouritesState.isSaving,
                    onClick = {
                        onSetFavourite(
                            SavedMeal(
                                id = details.id,
                                name = details.name,
                                imageUrl = details.imageUrl,
                                isSample = uiState.isSample,
                                details = details
                            ),
                            !isFavourite
                        )
                    }
                ) {
                    Text(
                        if (isFavourite) {
                            "Remove from favourites"
                        } else {
                            "Save to favourites"
                        }
                    )
                }

                // A list-saved favourite may initially have only a summary.
                val storedDetails = favouritesState.meals.firstOrNull {
                    it.id == details.id &&
                            it.isSample == uiState.isSample
                }?.details

                if (isFavourite && storedDetails == null) {
                    OutlinedButton(
                        enabled = !favouritesState.isSaving,
                        onClick = {
                            onSetFavourite(
                                SavedMeal(
                                    id = details.id,
                                    name = details.name,
                                    imageUrl = details.imageUrl,
                                    isSample = uiState.isSample,
                                    details = details
                                ),
                                true
                            )
                        }
                    ) {
                        Text("Save details for offline use")
                    }
                }

                DetailField("Also known as", details.alternateName)
                DetailField("Category", details.category)
                DetailField("Area", details.area)
                DetailField("Country", details.country)
                DetailField("Creative Commons confirmed", details.creativeCommons)
                DetailField("Last modified", details.dateModified)
                details.additionalFields.forEach { (label, value) ->
                    if (validWebUrl(value) != null) RecipeLink("Open $label", value)
                    else DetailField(label, value)
                }

                if (details.tags.isNotEmpty()) {
                    Text("Tags: ${details.tags.joinToString(", ")}")
                }

                if (details.ingredients.isNotEmpty()) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleLarge
                    )

                    details.ingredients.forEach { item ->
                        Text(
                            listOf(item.ingredient, item.measure)
                                .filter { it.isNotBlank() }
                                .joinToString(" — ")
                        )
                    }
                }

                if (details.instructions.isNotBlank()) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(details.instructions)
                }

                RecipeLink("Open recipe source", details.sourceUrl)
                RecipeLink("Watch recipe video", details.youtubeUrl)
                RecipeLink("Open image source", details.imageSourceUrl)
                if (details.imageSourceUrl.isNotBlank() && validWebUrl(details.imageSourceUrl) == null) {
                    DetailField("Image credit", details.imageSourceUrl)
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    if (value.isNotBlank()) {
        Text("$label: $value")
    }
}

@Composable
fun RecipeLink(label: String, url: String) {
    val context = LocalContext.current
    val uri = validWebUrl(url)?.let(Uri::parse)

    if (
        uri != null &&
        !uri.host.isNullOrBlank()
    ) {
        OutlinedButton(
            onClick = {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                } catch (exception: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "No app is available to open this link.",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (exception: SecurityException) {
                    Toast.makeText(
                        context,
                        "This link could not be opened.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label)
        }
    }
}