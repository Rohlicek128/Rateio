package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter


@Composable
fun AdaptiveAsyncImage(
    model: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    placeholderRatio: Float = 2f / 3f,
    minWidth: Dp = Dp.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    minHeight: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
) {
    var imageState by remember { mutableStateOf(AsyncImageState.Loading) }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier
            .widthIn(min = minWidth, max = maxWidth)
            .heightIn(min = minHeight, max = maxHeight)
            .then(
                when (imageState) {
                    AsyncImageState.Success -> Modifier
                    else -> Modifier.aspectRatio(placeholderRatio)
                }
            )
            .then(
                if (placeholderRatio < 1f) Modifier.fillMaxHeight()
                else Modifier.fillMaxWidth()
            ),
        contentScale = contentScale,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = ColorPainter(MaterialTheme.colorScheme.surfaceBright),
        onSuccess = {
            imageState = AsyncImageState.Success
            onSuccess?.invoke(it)
        },
        onError = { imageState = AsyncImageState.Error },
    )
}

enum class AsyncImageState { Loading, Success, Error }