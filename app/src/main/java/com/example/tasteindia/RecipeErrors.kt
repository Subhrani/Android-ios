package com.example.tasteindia

import java.io.IOException
import java.io.InterruptedIOException

fun recipeError(error: Exception): String = when (error) {
    is MealHttpException -> "The recipe service returned HTTP ${error.status}. Please try again."
    is InterruptedIOException -> "The request timed out. Please try again."
    is IOException -> "Could not connect. Check your internet and try again."
    else -> "Could not read the recipe data. Please try again."
}
