package com.example.tasteindia

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SampleSource {
    suspend fun meals(): List<Meal>
    suspend fun details(id: String): MealDetails
}

class AssetSampleSource(context: Context) : SampleSource {
    private val assets = context.applicationContext.assets
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    override suspend fun meals(): List<Meal> = withContext(Dispatchers.IO) {
        val json = assets.open("indian_meals.json").bufferedReader().use { it.readText() }
        requireNotNull(moshi.adapter(MealsResponse::class.java).fromJson(json))
            .meals.orEmpty().map { Meal(it.idMeal, it.strMeal, it.strMealThumb.orEmpty()) }
    }

    override suspend fun details(id: String): MealDetails = withContext(Dispatchers.IO) {
        val json = assets.open("sample_meal_details.json").bufferedReader().use { it.readText() }
        val response = requireNotNull(moshi.adapter(MealDetailsResponse::class.java).fromJson(json))
        val fields = requireNotNull(response.meals.orEmpty().firstOrNull { it["idMeal"] == id })
        mapMealDetails(fields)
    }
}
