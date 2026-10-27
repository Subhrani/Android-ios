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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
    onOpenMeal: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val ascending by viewModel.ascending.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val filterChoices by viewModel.filterChoices.collectAsStateWithLifecycle()

    val context = LocalContext.current

    RecipesScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::updateSearchQuery,
        ascending = ascending,
        onSortChange = viewModel::setAscending,
        filters = filters,
        filterChoices = filterChoices,
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

    // Keeps the list's scroll position as saveable screen state.
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "TasteIndia",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        when (uiState) {
            is RecipesUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()

                    Text(
                        text = "Loading recipes...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            is RecipesUiState.Success -> {
                val categoryAndIngredientMatches = intersectMealFilters(
                    indianMeals = uiState.meals,
                    categoryIds = filters.categoryIds,
                    ingredientIds = filters.ingredientIds
                )

                val matchingMeals = categoryAndIngredientMatches.filter { meal ->
                    meal.name.contains(
                        other = searchQuery.trim(),
                        ignoreCase = true
                    )
                }

                val visibleMeals = if (ascending) {
                    matchingMeals.sortedBy { it.name.lowercase() }
                } else {
                    matchingMeals.sortedByDescending { it.name.lowercase() }
                }

                if (uiState.isSample) {
                    Text(
                        text = "Sample data — offline review",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search recipes") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Button(
                    onClick = { onSortChange(!ascending) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (ascending) "Sort: A–Z" else "Sort: Z–A"
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

                val activeFilters = listOfNotNull(
                    filters.category?.let { "Category: $it" },
                    filters.ingredient?.let { "Ingredient: $it" },
                    searchQuery.takeIf { it.isNotBlank() }
                        ?.let { "Search: $it" }
                )

                if (activeFilters.isNotEmpty()) {
                    Text(
                        text = "Active: ${activeFilters.joinToString(" • ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (filters.isLoading) {
                    Text(
                        text = "Applying filters...",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                filters.error?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        draftCategory = ""
                        draftIngredient = ""
                        onClearAll()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Clear all")
                }

                Text(
                    text = "${visibleMeals.size} recipes",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                )

                if (visibleMeals.isEmpty()) {
                    Text(
                        text = "No recipes match your filters.",
                        modifier = Modifier.padding(16.dp)
                    )
                }

                LazyColumn(state = listState) {
                    items(
                        items = visibleMeals,
                        key = { meal -> meal.id }
                    ) { meal ->
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenMeal(meal.id, uiState.isSample)
                                }
                                .padding(16.dp)
                        )

                        HorizontalDivider()
                    }
                }
            }

            is RecipesUiState.Empty -> {
                MessageWithRetry(
                    message = "No Indian recipes were returned. Please try again.",
                    onRetry = onRetry,
                    onLoadSample = onLoadSample
                )
            }

            is RecipesUiState.Error -> {
                MessageWithRetry(
                    message = uiState.message,
                    onRetry = onRetry,
                    onLoadSample = onLoadSample
                )
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
            choices.isLoading -> {
                Text("Loading filter choices...")
            }

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
            Text(
                text = "$label: ${selected.ifBlank { "All" }}"
            )
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
        Text(text = message)

        Button(onClick = onRetry) {
            Text("Retry")
        }

        Button(onClick = onLoadSample) {
            Text("Try sample data")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipesScreenPreview() {
    MyApplicationTheme {
        RecipesScreen(
            uiState = RecipesUiState.Success(
                meals = listOf(
                    Meal("test-1", "Sample Biryani", ""),
                    Meal("test-2", "Sample Chana Masala", "")
                ),
                isSample = true
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            ascending = true,
            onSortChange = {},
            filters = RecipeFilterState(),
            filterChoices = FilterChoices(),
            onLoadFilterChoices = {},
            onApplyFilters = { _, _ -> },
            onClearAll = {},
            onRetry = {},
            onLoadSample = {},
            onOpenMeal = { _, _ -> }
        )
    }
}