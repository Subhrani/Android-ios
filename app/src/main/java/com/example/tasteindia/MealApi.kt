package com.example.tasteindia

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String? = null
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
data class MealDetailsResponse(
    val meals: List<Map<String, Any?>>?
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
    @GET("lookup.php")
    suspend fun getMealDetails(
        @Query("i") mealId: String
    ): Response<MealDetailsResponse>
}

object MealApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    val service: MealApi = Retrofit.Builder()
        .client(httpClient)
        .baseUrl("https://www.themealdb.com/api/json/v1/1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(MealApi::class.java)
}