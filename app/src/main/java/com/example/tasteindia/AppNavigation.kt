package com.example.tasteindia

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Destinations {
    const val RECIPES = "recipes"
    const val DETAILS = "details/{mealId}/{isSample}"

    fun details(mealId: String, isSample: Boolean): String {
        return "details/${Uri.encode(mealId)}/$isSample"
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.RECIPES,
        modifier = modifier
    ) {
        composable(Destinations.RECIPES) {
            RecipesRoute(
                onOpenMeal = { mealId, isSample ->
                    navController.navigate(
                        Destinations.details(mealId, isSample)
                    )
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
            val mealId = entry.arguments?.getString("mealId").orEmpty()
            val isSample = entry.arguments?.getBoolean("isSample") ?: false

            MealDetailsRoute(
                mealId = mealId,
                isSample = isSample,
                onBack = { navController.popBackStack() }
            )
        }
    }
}