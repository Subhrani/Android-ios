package com.example.tasteindia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

