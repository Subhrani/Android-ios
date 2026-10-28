package com.example.tasteindia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Context
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest

private object RecipeImages {
    private var instance: ImageLoader? = null
    @Synchronized fun loader(context: Context): ImageLoader = instance ?: ImageLoader.Builder(
        context.applicationContext
    ).okHttpClient(MealApiClient.httpClient).build().also { instance = it }
}

/** Coil owns image IO and caching; rows never request meal details. */
@Composable
fun RecipeImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    sampleMealId: String? = null
) {
    // Use bundled images only for explicitly labelled sample records.
    // Looking up the ID at render time also fixes favourites saved before this update.
    val sampleImage = when (sampleMealId) {
        "test-1" -> R.drawable.sample_biryani
        "test-2" -> R.drawable.sample_chana_masala
        else -> null
    }
    var loading by remember(imageUrl, sampleImage) { mutableStateOf(sampleImage != null || imageUrl.isNotBlank()) }
    var failed by remember(imageUrl, sampleImage) { mutableStateOf(sampleImage == null && imageUrl.isBlank()) }
    val context = LocalContext.current
    val imageLoader = remember(context) { RecipeImages.loader(context) }
    val request = remember(context, imageUrl, sampleImage) {
        ImageRequest.Builder(context).data(sampleImage ?: imageUrl.takeIf { it.isNotBlank() })
            .crossfade(true).build()
    }
    Box(
        modifier = modifier.aspectRatio(if (hero) 1.5f else 2.4f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Decorative image: the adjacent heading already names the meal.
        AsyncImage(
            model = request,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onLoading = { loading = true; failed = false },
            onSuccess = { loading = false; failed = false },
            onError = { loading = false; failed = true }
        )
        if (loading) CircularProgressIndicator()
        if (sampleImage != null && !failed) {
            Text(
                text = "AI-generated sample image",
                modifier = Modifier.align(Alignment.BottomStart)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (failed) Text(
            text = if (imageUrl.isBlank()) "No recipe photograph" else "Recipe photograph unavailable",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
