package com.example.tasteindia

import retrofit2.Response

class FakeMealApi : MealApi {
    var category: suspend (String) -> Response<MealsResponse> = { Response.success(MealsResponse(emptyList())) }
    var detail: suspend (String) -> Response<MealDetailsResponse> = { id ->
        Response.success(MealDetailsResponse(listOf(mapOf("idMeal" to id, "strMeal" to "Recipe $id"))))
    }
    var indian: suspend () -> Response<MealsResponse> = {
        Response.success(MealsResponse(listOf(MealDto("1", "Biryani", null), MealDto("2", "Chana", null))))
    }
    override suspend fun getMealsByArea(area: String) = indian()
    override suspend fun getMealsByCategory(category: String) = this.category(category)
    override suspend fun getMealsByIngredient(ingredient: String) = Response.success(MealsResponse(emptyList()))
    override suspend fun getCategories(list: String) = Response.success(CategoriesResponse(emptyList()))
    override suspend fun getIngredients(list: String) = Response.success(IngredientsResponse(emptyList()))
    override suspend fun getMealDetails(mealId: String) = detail(mealId)
}
