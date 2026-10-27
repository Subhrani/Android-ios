package com.example.tasteindia

data class IngredientMeasure(
    val ingredient: String,
    val measure: String
)

data class MealDetails(
    val id: String,
    val name: String,
    val imageUrl: String,
    val category: String,
    val area: String,
    val country: String,
    val alternateName: String,
    val ingredients: List<IngredientMeasure>,
    val instructions: String,
    val tags: List<String>,
    val sourceUrl: String,
    val youtubeUrl: String
)

fun mapMealDetails(fields: Map<String, Any?>): MealDetails {
    fun text(key: String): String {
        return (fields[key] as? String)?.trim().orEmpty()
    }

    val ingredients = (1..20).mapNotNull { index ->
        val ingredient = text("strIngredient$index")
        val measure = text("strMeasure$index")

        if (ingredient.isEmpty() && measure.isEmpty()) {
            null
        } else {
            IngredientMeasure(
                ingredient = ingredient,
                measure = measure
            )
        }
    }

    val tags = text("strTags")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return MealDetails(
        id = text("idMeal"),
        name = text("strMeal"),
        imageUrl = text("strMealThumb"),
        category = text("strCategory"),
        area = text("strArea"),
        country = text("strCountry"),
        alternateName = text("strMealAlternate"),
        ingredients = ingredients,
        instructions = text("strInstructions"),
        tags = tags,
        sourceUrl = text("strSource"),
        youtubeUrl = text("strYoutube")
    )
}