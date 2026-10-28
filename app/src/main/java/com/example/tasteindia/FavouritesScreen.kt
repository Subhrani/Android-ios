package com.example.tasteindia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavouritesScreen(
    state: FavouritesUiState,
    onBack: () -> Unit,
    onOpenMeal: (String, Boolean) -> Unit,
    onRemove: (SavedMeal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("Back to recipes")
        }

        Text(
            text = "Favourites",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        state.error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        when {
            state.isLoading -> {
                CircularProgressIndicator()
                Text("Loading favourites...")
            }

            state.meals.isEmpty() -> {
                Text("No favourites saved yet.")
            }

            else -> {
                Text(
                    text = "${state.meals.size} saved recipes",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    items(
                        items = state.meals,
                        key = { meal ->
                            "${meal.isSample}:${meal.id}"
                        }
                    ) { meal ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().clickable {
                                onOpenMeal(meal.id, meal.isSample)
                            }) {
                                RecipeImage(meal.imageUrl, Modifier.fillMaxWidth())
                                Text(meal.name, style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 12.dp))
                            }

                            if (meal.isSample) {
                                Text(
                                    text = "Sample recipe",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Text(
                                text = if (meal.details != null) "Details saved for offline use"
                                    else "Open recipe while online to save its details",
                                style = MaterialTheme.typography.labelMedium
                            )
                            OutlinedButton(
                                onClick = { onRemove(meal) },
                                enabled = !state.isSaving
                            ) {
                                Text("Remove from favourites")
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}