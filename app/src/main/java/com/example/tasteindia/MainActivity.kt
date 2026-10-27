package com.example.tasteindia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
                    RecipeList(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeList(modifier: Modifier = Modifier) {
    val recipes = listOf(
        Meal(id = "1", name = "Biryani", imageUrl = ""),
        Meal(id = "2", name = "Butter Chicken", imageUrl = ""),
        Meal(id = "3", name = "Chana Masala", imageUrl = ""),
        Meal(id = "4", name = "Dosa", imageUrl = ""),
        Meal(id = "5", name = "Samosa", imageUrl = "")
    )

    Column(modifier = modifier) {
        Text(
            text = "TasteIndia",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn {
            items(
                items = recipes,
                key = { recipe -> recipe.id }
            ) { recipe ->
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                HorizontalDivider()
            }
        }
    }
}
data class Meal(
    val id: String,
    val name: String,
    val imageUrl: String
)
@Preview(showBackground = true)
@Composable
fun RecipeListPreview() {
    MyApplicationTheme {
        RecipeList()
    }
}