package com.example.tasteindia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RecipesRoute(
    favouritesState: FavouritesUiState,
    onSetFavourite: (SavedMeal, Boolean) -> Unit,
    onOpenFavourites: () -> Unit,
    onOpenMeal: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = viewModel(factory = RecipesViewModel.Factory)
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

