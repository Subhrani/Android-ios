package com.example.tasteindia

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.tasteindia.ui.theme.MyApplicationTheme

@Preview(showBackground = true)
@Composable
fun MissingImageRowPreview() {
    MyApplicationTheme {
        RecipeRow(Meal("sample", "Sample Biryani", ""), false, true, {}, {})
    }
}

@Preview(showBackground = true, fontScale = 1.6f)
@Composable
fun LongNameRowPreview() {
    MyApplicationTheme {
        RecipeRow(Meal("sample", "A longer sample recipe name for large text review", ""), true, true, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsLoadingPreview() {
    MyApplicationTheme {
        MealDetailsScreen(MealDetailsUiState.Loading, false,
            FavouritesUiState(isLoading = false), {}, {}, { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsErrorPreview() {
    MyApplicationTheme {
        MealDetailsScreen(MealDetailsUiState.Error("Could not connect. Please try again."), false,
            FavouritesUiState(isLoading = false), {}, {}, { _, _ -> })
    }
}
