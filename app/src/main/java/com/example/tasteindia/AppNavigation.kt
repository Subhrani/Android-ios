package com.example.tasteindia

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Destinations {
    const val RECIPES = "recipes"
    const val FAVOURITES = "favourites"
    const val DETAILS = "details/{mealId}/{isSample}"

    fun details(mealId: String, isSample: Boolean): String {
        return "details/${Uri.encode(mealId)}/$isSample"
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext

    val store = remember(context) {
        DataStoreFavouritesStore(context)
    }

    val factory = remember(store) {
        FavouritesViewModelFactory(store)
    }

    // Created above the destinations so all screens share this instance.
    val favouritesViewModel: FavouritesViewModel = viewModel(
        factory = factory
    )

    val favouritesState by favouritesViewModel.uiState
        .collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Destinations.RECIPES,
        modifier = modifier
    ) {
        composable(Destinations.RECIPES) {
            RecipesRoute(
                favouritesState = favouritesState,
                onSetFavourite = favouritesViewModel::setFavourite,
                onOpenFavourites = {
                    navController.navigate(Destinations.FAVOURITES) {
                        launchSingleTop = true
                    }
                },
                onOpenMeal = { mealId, isSample ->
                    navController.navigate(
                        Destinations.details(mealId, isSample)
                    )
                }
            )
        }

        composable(Destinations.FAVOURITES) {
            FavouritesScreen(
                state = favouritesState,
                onBack = { navController.popBackStack() },
                onOpenMeal = { mealId, isSample ->
                    navController.navigate(
                        Destinations.details(mealId, isSample)
                    )
                },
                onRemove = { meal ->
                    favouritesViewModel.setFavourite(meal, false)
                }
            )
        }

        composable(
            route = Destinations.DETAILS,
            arguments = listOf(
                navArgument("mealId") {
                    type = NavType.StringType
                },
                navArgument("isSample") {
                    type = NavType.BoolType
                }
            )
        ) { entry ->
            val mealId = entry.arguments
                ?.getString("mealId").orEmpty()

            val isSample = entry.arguments
                ?.getBoolean("isSample") ?: false

            MealDetailsRoute(
                mealId = mealId,
                isSample = isSample,
                favouritesState = favouritesState,
                onSetFavourite = favouritesViewModel::setFavourite,
                onBack = { navController.popBackStack() }
            )
        }
    }
}