package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.rateio.data.remote.TmdbImage


data class CarouselImage(
    val filePath: String,
    val aspectRatio: Float,
)

@Composable
fun AdaptiveImageCarousel(
    baseUrl: String,
    images:  List<CarouselImage>,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    itemWidth: Dp = 300.dp,
    itemHeight: Dp = 160.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onImageClick: (index: Int) -> Unit = {}
) {
    val state = rememberCarouselState { images.size }

    HorizontalUncontainedCarousel(
        state = state,
        itemWidth = itemWidth,
        itemSpacing = 8.dp,
        contentPadding = contentPadding,
        modifier = modifier,
    ) { index ->
        AsyncImage(
            model = baseUrl + images[index].filePath,
            contentDescription = null,
            modifier = Modifier
                .heightIn(max = itemHeight)
                .aspectRatio(images[index].aspectRatio)
                .maskClip(shape)
                .clickable(onClick = { onImageClick(index) }),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}