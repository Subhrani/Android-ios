package com.example.tasteindia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    RecipesRoute(
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
    modifier: Modifier = Modifier,
    viewModel: RecipesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecipesScreen(
        uiState = uiState,
        onRetry = { viewModel.loadRecipes() },
        modifier = modifier
    )
}

@Composable
fun RecipesScreen(
    uiState: RecipesUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
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
                Text(
                    text = "${uiState.meals.size} recipes",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn {
                    items(
                        items = uiState.meals,
                        key = { meal -> meal.id }
                    ) { meal ->
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        HorizontalDivider()
                    }
                }
            }

            is RecipesUiState.Empty -> {
                MessageWithRetry(
                    message = "No Indian recipes were returned. Please try again.",
                    onRetry = onRetry
                )
            }

            is RecipesUiState.Error -> {
                MessageWithRetry(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
fun MessageWithRetry(
    message: String,
    onRetry: () -> Unit
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
            Text(text = "Retry")
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
                    Meal("1", "Sample Biryani", ""),
                    Meal("2", "Sample Chana Masala", "")
                )
            ),
            onRetry = {}
        )
    }
}