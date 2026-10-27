package com.example.tasteindia

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
    onBack: () -> Unit,
    viewModel: MealDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(mealId, isSample) {
        viewModel.loadDetails(mealId, isSample, context)
    }

    MealDetailsScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = {
            viewModel.loadDetails(mealId, isSample, context)
        }
    )
}

@Composable
fun MealDetailsScreen(
    uiState: MealDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Back to recipes")
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

                if (details.alternateName.isNotBlank()) {
                    Text("Also known as: ${details.alternateName}")
                }

                if (details.category.isNotBlank()) {
                    Text("Category: ${details.category}")
                }

                if (details.area.isNotBlank()) {
                    Text("Area: ${details.area}")
                }

                if (details.country.isNotBlank()) {
                    Text("Country: ${details.country}")
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
                        val row = listOf(item.ingredient, item.measure)
                            .filter { it.isNotBlank() }
                            .joinToString(" — ")

                        Text(text = row)
                    }
                }

                if (details.instructions.isNotBlank()) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(text = details.instructions)
                }

                RecipeLink(
                    label = "Open recipe source",
                    url = details.sourceUrl
                )

                RecipeLink(
                    label = "Watch recipe video",
                    url = details.youtubeUrl
                )
            }
        }
    }
}

@Composable
fun RecipeLink(
    label: String,
    url: String
) {
    val context = LocalContext.current
    val uri = runCatching { Uri.parse(url.trim()) }.getOrNull()

    val isValid = uri != null &&
            uri.scheme?.lowercase() in setOf("http", "https") &&
            !uri.host.isNullOrBlank()

    if (isValid && uri != null) {
        OutlinedButton(
            onClick = {
                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                } catch (exception: ActivityNotFoundException) {
                    // No app is available to open this link.
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label)
        }
    }
}