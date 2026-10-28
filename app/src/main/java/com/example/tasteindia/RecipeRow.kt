package com.example.tasteindia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun RecipeRow(
    meal: Meal,
    isFavourite: Boolean,
    canSave: Boolean,
    onOpen: () -> Unit,
    onToggleFavourite: () -> Unit,
    isSample: Boolean = false
) {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.fillMaxWidth().clickable(
            role = Role.Button, onClickLabel = "Open recipe details", onClick = onOpen
        )) {
            RecipeImage(
                imageUrl = meal.imageUrl,
                modifier = Modifier.fillMaxWidth(),
                sampleMealId = if (isSample) meal.id else null
            )
            Text(meal.name, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp))
        }
        OutlinedButton(enabled = canSave, onClick = onToggleFavourite) {
            Text(if (isFavourite) "Remove ${meal.name} from favourites"
                else "Save ${meal.name} to favourites")
        }
    }
}
