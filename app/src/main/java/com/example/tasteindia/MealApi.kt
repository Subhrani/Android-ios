package com.example.tasteindia

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String?
)

data class MealsResponse(
    val meals: List<MealDto>?
)
data class CategoryDto(
    val strCategory: String
)

data class CategoriesResponse(
    val meals: List<CategoryDto>?
)

data class IngredientDto(
    val strIngredient: String
)

data class IngredientsResponse(
    val meals: List<IngredientDto>?
)
interface MealApi {
    @GET("filter.php")
    suspend fun getMealsByArea(
        @Query("a") area: String
    ): Response<MealsResponse>
    @GET("filter.php")
    suspend fun getMealsByCategory(
        @Query("c") category: String
    ): Response<MealsResponse>

    @GET("filter.php")
    suspend fun getMealsByIngredient(
        @Query("i") ingredient: String
    ): Response<MealsResponse>
    @GET("list.php")
    suspend fun getCategories(
        @Query("c") list: String = "list"
    ): Response<CategoriesResponse>

    @GET("list.php")
    suspend fun getIngredients(
        @Query("i") list: String = "list"
    ): Response<IngredientsResponse>
}

object MealApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: MealApi = Retrofit.Builder()
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(MealApi::class.java)
}