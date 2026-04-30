package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil3.compose.AsyncImage


@Composable
fun AdaptiveAsyncImage(
    model: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholderRatio: Float = 2f / 3f,
    minWidth: Dp = Dp.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    maxHeight: Dp = Dp.Unspecified,
) {
    var isLoading by remember { mutableStateOf(true) }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier
            .widthIn(min = minWidth, max = maxWidth)
            .heightIn(max = maxHeight)
            .then(
                if (isLoading) Modifier.aspectRatio(placeholderRatio)
                else Modifier
            ),
        contentScale = ContentScale.Fit,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
        onSuccess = { isLoading = false },
        onError   = { isLoading = false },
    )
}