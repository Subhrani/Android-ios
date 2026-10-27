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

interface MealApi {
    @GET("filter.php")
    suspend fun getMealsByArea(
        @Query("a") area: String
    ): Response<MealsResponse>
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