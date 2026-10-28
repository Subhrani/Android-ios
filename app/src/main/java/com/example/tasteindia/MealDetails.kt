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
    val youtubeUrl: String,
    val imageSourceUrl: String = "",
    val creativeCommons: String = "",
    val dateModified: String = "",
    val additionalFields: Map<String, String> = emptyMap()
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

    val mapped = setOf("idMeal", "strMeal", "strMealThumb", "strCategory", "strArea",
        "strCountry", "strMealAlternate", "strInstructions", "strTags", "strSource", "strYoutube",
        "strImageSource", "strCreativeCommonsConfirmed", "dateModified") +
        (1..20).flatMap { listOf("strIngredient$it", "strMeasure$it") }

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
        youtubeUrl = text("strYoutube"),
        imageSourceUrl = text("strImageSource"),
        creativeCommons = text("strCreativeCommonsConfirmed"),
        dateModified = text("dateModified"),
        additionalFields = fields.filterKeys { it !in mapped }.mapNotNull { (key, value) ->
            if (value !is String && value !is Number && value !is Boolean) return@mapNotNull null
            val content = value.toString().trim()
            if (content.isBlank()) return@mapNotNull null
            val label = key.removePrefix("str").removePrefix("id")
                .replace(Regex("([a-z])([A-Z])"), "$1 $2")
                .replaceFirstChar { it.uppercaseChar() }
            label to content
        }.toMap()
    )
}