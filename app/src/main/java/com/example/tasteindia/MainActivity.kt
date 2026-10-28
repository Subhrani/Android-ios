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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class Meal(
    val id: String,
    val name: String,
    val imageUrl: String
)

@Composable
fun RecipesRoute(
    favouritesState: FavouritesUiState,
    onSetFavourite: (SavedMeal, Boolean) -> Unit,
    onOpenFavourites: () -> Unit,
    onOpenMeal: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val ascending by viewModel.ascending.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val choices by viewModel.filterChoices.collectAsStateWithLifecycle()
    val context = LocalContext.current

    RecipesScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::updateSearchQuery,
        ascending = ascending,
        onSortChange = viewModel::setAscending,
        filters = filters,
        filterChoices = choices,
        favouritesState = favouritesState,
        onSetFavourite = onSetFavourite,
        onOpenFavourites = onOpenFavourites,
        onLoadFilterChoices = viewModel::loadFilterChoices,
        onApplyFilters = viewModel::applyFilters,
        onClearAll = {
            viewModel.clearFilters()
            viewModel.updateSearchQuery("")
            viewModel.setAscending(true)
        },
        onRetry = viewModel::loadRecipes,
        onLoadSample = { viewModel.loadSampleRecipes(context) },
        onOpenMeal = onOpenMeal,
        modifier = modifier
    )
}

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

                val matches = intersectMealFilters(
                    indianMeals = uiState.meals,
                    categoryIds = filters.categoryIds,
                    ingredientIds = filters.ingredientIds
                ).filter { meal ->
                    meal.name.contains(
                        searchQuery.trim(),
                        ignoreCase = true
                    ) && (!favouritesOnly || meal.id in savedIds)
                }

                val visibleMeals = if (ascending) {
                    matches.sortedBy { it.name.lowercase() }
                } else {
                    matches.sortedByDescending { it.name.lowercase() }
                }

                item(key = "controls") {
                    if (uiState.isSample) {
                        Text(
                            text = "Sample data — offline review",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(16.dp)
                        )
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
                        text = "${visibleMeals.size} recipes",
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
                    key = { "meal:${it.id}" }
                ) { meal ->
                    val isSaved = meal.id in savedIds

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenMeal(meal.id, uiState.isSample)
                                }
                                .padding(vertical = 12.dp)
                        )

                        OutlinedButton(
                            enabled = !favouritesState.isLoading &&
                                    !favouritesState.isSaving,
                            onClick = {
                                onSetFavourite(
                                    SavedMeal(
                                        id = meal.id,
                                        name = meal.name,
                                        imageUrl = meal.imageUrl,
                                        isSample = uiState.isSample
                                    ),
                                    !isSaved
                                )
                            }
                        ) {
                            Text(
                                if (isSaved) {
                                    "Remove ${meal.name} from favourites"
                                } else {
                                    "Save ${meal.name} to favourites"
                                }
                            )
                        }
                    }

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

@Composable
fun FilterControls(
    choices: FilterChoices,
    draftCategory: String,
    draftIngredient: String,
    onCategoryChange: (String) -> Unit,
    onIngredientChange: (String) -> Unit,
    onRetryChoices: () -> Unit,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            choices.isLoading -> Text("Loading filter choices...")

            choices.error != null -> {
                Text(
                    text = choices.error,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(onClick = onRetryChoices) {
                    Text("Retry filter choices")
                }
            }

            else -> {
                FilterDropdown(
                    label = "Category",
                    selected = draftCategory,
                    options = choices.categories,
                    onSelect = onCategoryChange
                )
                FilterDropdown(
                    label = "Main ingredient",
                    selected = draftIngredient,
                    options = choices.ingredients,
                    onSelect = onIngredientChange
                )
                Button(onClick = onApply) {
                    Text("Apply filters")
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: ${selected.ifBlank { "All" }}")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onSelect("")
                    expanded = false
                }
            )

            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MessageWithRetry(
    message: String,
    onRetry: () -> Unit,
    onLoadSample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(message)
        Button(onClick = onRetry) {
            Text("Retry")
        }
        Button(onClick = onLoadSample) {
            Text("Try sample data")
        }
    }
}