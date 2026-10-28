package com.example.tasteindia

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class MealHttpException(val status: Int) : IOException("Recipe service returned HTTP $status.")

/** Shared by destinations. No automatic per-row detail enrichment is performed. */
object AppRepositories {
    val meals by lazy { MealRepository(MealApiClient.service) }
}

class MealRepository(private val api: MealApi) {
    private data class Entry(val expiresAt: Long, val value: Any)
    // One detail lock deliberately bounds live lookups to one at a time.
    // Repeated callers recheck the cache after acquiring the same lock.
    private val detailLock = Mutex()
    private val listLock = Mutex()
    private val cache = object : LinkedHashMap<String, Entry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>?) = size > 100
    }

    private suspend fun <T : Any> cached(
        key: String, lock: Mutex, forceRefresh: Boolean = false, load: suspend () -> T
    ): T = lock.withLock {
        val hit = synchronized(cache) { cache[key] }
        if (!forceRefresh && hit != null && hit.expiresAt > System.nanoTime()) {
            @Suppress("UNCHECKED_CAST")
            return@withLock hit.value as T
        }
        val value = load()
        synchronized(cache) {
            cache[key] = Entry(System.nanoTime() + 5L * 60 * 1_000_000_000, value)
        }
        value
    }

    private fun <T> body(response: Response<T>): T {
        if (!response.isSuccessful) throw MealHttpException(response.code())
        return response.body() ?: throw IOException("The service returned an empty response.")
    }

    suspend fun getIndianMeals(forceRefresh: Boolean = false): List<Meal> = cached("indian", listLock, forceRefresh) {
        body(api.getMealsByArea("Indian")).meals.orEmpty().map {
            Meal(it.idMeal.trim(), it.strMeal.trim(), it.strMealThumb.orEmpty().trim())
        }.filter { it.id.isNotBlank() && it.name.isNotBlank() }.distinctBy { it.id }
    }

    suspend fun getCategoryNames(): List<String> = cached("categories", listLock) {
        body(api.getCategories()).meals.orEmpty().map { it.strCategory.trim() }
            .filter { it.isNotEmpty() }.distinct().sorted()
    }

    suspend fun getIngredientNames(): List<String> = cached("ingredients", listLock) {
        body(api.getIngredients()).meals.orEmpty().map { it.strIngredient.trim() }
            .filter { it.isNotEmpty() }.distinct().sorted()
    }

    suspend fun getCategoryMealIds(category: String): Set<String> =
        cached("category:${category.lowercase(Locale.ROOT)}", listLock) {
            body(api.getMealsByCategory(category)).meals.orEmpty().map { it.idMeal }.toSet()
        }

    suspend fun getIngredientMealIds(ingredient: String): Set<String> =
        cached("ingredient:${ingredient.lowercase(Locale.ROOT)}", listLock) {
            body(api.getMealsByIngredient(ingredient)).meals.orEmpty().map { it.idMeal }.toSet()
        }

    suspend fun getDetails(mealId: String): MealDetails = cached("detail:$mealId", detailLock) {
        val fields = body(api.getMealDetails(mealId)).meals.orEmpty()
            .firstOrNull { (it["idMeal"] as? String)?.trim() == mealId }
            ?: throw IOException("Recipe details were not found.")
        mapMealDetails(fields)
    }
}
