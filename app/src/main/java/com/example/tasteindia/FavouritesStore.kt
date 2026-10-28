package com.example.tasteindia

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declare this once so all screens use the same DataStore.
private val Context.favouritesDataStore by preferencesDataStore(
    name = "tasteindia_favourites"
)

data class SavedMeal(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isSample: Boolean,
    val details: MealDetails? = null
)

interface FavouritesStore {
    val favourites: Flow<List<SavedMeal>>

    suspend fun save(meal: SavedMeal)

    suspend fun remove(mealId: String, isSample: Boolean)
}

class DataStoreFavouritesStore(
    context: Context
) : FavouritesStore {

    private val dataStore =
        context.applicationContext.favouritesDataStore

    private val favouritesKey =
        stringPreferencesKey("saved_meals_json")

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(
        List::class.java,
        SavedMeal::class.java
    )

    private val adapter =
        moshi.adapter<List<SavedMeal>>(listType)

    override val favourites: Flow<List<SavedMeal>> =
        dataStore.data.map { preferences ->
            decode(preferences[favouritesKey])
        }

    override suspend fun save(meal: SavedMeal) {
        dataStore.edit { preferences ->
            val current = decode(preferences[favouritesKey])

            val existing = current.firstOrNull {
                it.id == meal.id && it.isSample == meal.isSample
            }

            // Preserve cached details if the list screen saves only a summary.
            val updatedMeal = meal.copy(
                details = meal.details ?: existing?.details
            )

            val updated = current.filterNot {
                it.id == meal.id && it.isSample == meal.isSample
            } + updatedMeal

            preferences[favouritesKey] = adapter.toJson(updated)
        }
    }

    override suspend fun remove(
        mealId: String,
        isSample: Boolean
    ) {
        dataStore.edit { preferences ->
            val current = decode(preferences[favouritesKey])

            val updated = current.filterNot {
                it.id == mealId && it.isSample == isSample
            }

            preferences[favouritesKey] = adapter.toJson(updated)
        }
    }

    private fun decode(json: String?): List<SavedMeal> {
        if (json == null) {
            return emptyList()
        }

        return requireNotNull(adapter.fromJson(json)) {
            "Saved favourites could not be read."
        }
    }
}