package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CakeCategory
import com.example.data.model.CakeItemEntity

/**
 * Returns the fallback drawable resource for a cake based on its name and category.
 */
fun getCakeFallbackDrawable(cake: CakeItemEntity): Int {
    return when {
        cake.name.contains("Berry", ignoreCase = true) || cake.name.contains("Fruit", ignoreCase = true) || cake.category == CakeCategory.CHEESECAKE.name -> R.drawable.ic_category_pies
        cake.name.contains("Cupcake", ignoreCase = true) || cake.category == CakeCategory.SEASONAL_SPECIAL.name -> R.drawable.ic_category_cupcakes
        cake.name.contains("Choco", ignoreCase = true) || cake.name.contains("Truffle", ignoreCase = true) || cake.category == CakeCategory.CHOCOLATE_TRUFFLE.name -> R.drawable.ic_category_cakes
        cake.category == CakeCategory.MULTI_TIER.name -> R.drawable.ic_category_croissants
        cake.localDrawableName == "img_custom_studio_banner" -> R.drawable.img_custom_studio_banner
        else -> R.drawable.img_hero_banner
    }
}

/**
 * Seamlessly renders custom photos (content:// or file:// or web URLs) or fallback drawable assets.
 */
@Composable
fun CakeThumbnailImage(
    cake: CakeItemEntity,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackRes = getCakeFallbackDrawable(cake)
    val isCustomPhoto = cake.localDrawableName.startsWith("content://") ||
            cake.localDrawableName.startsWith("file://") ||
            cake.localDrawableName.startsWith("http://") ||
            cake.localDrawableName.startsWith("https://")

    if (isCustomPhoto) {
        AsyncImage(
            model = cake.localDrawableName,
            contentDescription = cake.name,
            placeholder = painterResource(id = fallbackRes),
            error = painterResource(id = fallbackRes),
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = cake.name,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
