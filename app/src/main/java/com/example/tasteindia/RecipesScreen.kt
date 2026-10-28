package com.example.tasteindia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tasteindia.ui.theme.MyApplicationTheme

@Composable
fun RecipesScreen(
    uiState: RecipesUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    ascending: Boolean,
    onSortChange: (Boolean) -> Unit,
    filters: RecipeFilterState,
    filterChoices: FilterChoices,
    favouritesState: FavouritesUiState,
    onSetFavourite: (SavedMeal, Boolean) -> Unit,
    onOpenFavourites: () -> Unit,
    onLoadFilterChoices: () -> Unit,
    onApplyFilters: (String?, String?) -> Unit,
    onClearAll: () -> Unit,
    onRetry: () -> Unit,
    onLoadSample: () -> Unit,
    onOpenMeal: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var draftCategory by rememberSaveable { mutableStateOf("") }
    var draftIngredient by rememberSaveable { mutableStateOf("") }
    var favouritesOnly by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Controls are list items too, so the whole screen can scroll.
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        item(key = "heading") {
            Text(
                text = "TasteIndia",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            OutlinedButton(
                onClick = onOpenFavourites,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Open favourites")
            }

            favouritesState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        when (uiState) {
            is RecipesUiState.Loading -> {
                item(key = "loading") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text("Loading recipes...")
                    }
                }
            }

            is RecipesUiState.Success -> {
                val savedIds = favouritesState.meals
                    .filter { it.isSample == uiState.isSample }
                    .map { it.id }
                    .toSet()

                val visibleMeals = selectMeals(
                    meals = uiState.meals,
                    query = searchQuery,
                    ascending = ascending,
                    favouritesOnly = favouritesOnly,
                    favouriteIds = savedIds,
                    categoryIds = filters.categoryIds,
                    ingredientIds = filters.ingredientIds
                )
                item(key = "controls") {
                    if (uiState.isSample) {
                        Text(
                            text = "Sample data — offline review",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) { Text("Try live recipes") }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Search recipes") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    Button(
                        onClick = { onSortChange(!ascending) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(if (ascending) "Sort: A–Z" else "Sort: Z–A")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = favouritesOnly,
                                role = Role.Checkbox,
                                onValueChange = { favouritesOnly = it }
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = favouritesOnly,
                            onCheckedChange = null
                        )
                        Text(
                            text = "Favourites only",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (!showFilters) {
                                draftCategory = filters.category.orEmpty()
                                draftIngredient = filters.ingredient.orEmpty()
                                onLoadFilterChoices()
                            }
                            showFilters = !showFilters
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(if (showFilters) "Close filters" else "Filters")
                    }

                    if (showFilters) {
                        FilterControls(
                            choices = filterChoices,
                            draftCategory = draftCategory,
                            draftIngredient = draftIngredient,
                            onCategoryChange = { draftCategory = it },
                            onIngredientChange = { draftIngredient = it },
                            onRetryChoices = onLoadFilterChoices,
                            onApply = {
                                onApplyFilters(
                                    draftCategory.takeIf { it.isNotBlank() },
                                    draftIngredient.takeIf { it.isNotBlank() }
                                )
                                showFilters = false
                            }
                        )
                    }

                    val active = listOfNotNull(
                        filters.category?.let { "Category: $it" },
                        filters.ingredient?.let { "Ingredient: $it" },
                        searchQuery.takeIf { it.isNotBlank() }
                            ?.let { "Search: $it" },
                        if (favouritesOnly) "Favourites only" else null
                    )

                    if (active.isNotEmpty()) {
                        Text(
                            text = "Active: ${active.joinToString(" • ")}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    if (filters.isLoading) {
                        Text(
                            text = "Applying filters...",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    filters.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            draftCategory = ""
                            draftIngredient = ""
                            favouritesOnly = false
                            onClearAll()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text("Clear all")
                    }

                    Text(
                        text = "${visibleMeals.size} ${if (visibleMeals.size == 1) "recipe" else "recipes"}",
                        modifier = Modifier.padding(16.dp)
                    )

                    if (visibleMeals.isEmpty()) {
                        Text(
                            text = "No recipes match your filters.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                items(
                    items = visibleMeals,
                    key = { "meal:${uiState.isSample}:${it.id}" }
                ) { meal ->
                    val isSaved = meal.id in savedIds

                    RecipeRow(
                        meal = meal,
                        isFavourite = isSaved,
                        canSave = !favouritesState.isLoading && !favouritesState.isSaving,
                        onOpen = { onOpenMeal(meal.id, uiState.isSample) },
                        onToggleFavourite = {
                            onSetFavourite(
                                SavedMeal(meal.id, meal.name, meal.imageUrl, uiState.isSample),
                                !isSaved
                            )
                        }
                    )

                    HorizontalDivider()
                }
            }

            is RecipesUiState.Empty -> {
                item(key = "empty") {
                    MessageWithRetry(
                        message = "No Indian recipes were returned. Please try again.",
                        onRetry = onRetry,
                        onLoadSample = onLoadSample
                    )
                }
            }

            is RecipesUiState.Error -> {
                item(key = "error") {
                    MessageWithRetry(
                        message = uiState.message,
                        onRetry = onRetry,
                        onLoadSample = onLoadSample
                    )
                }
            }
        }
    }
}

