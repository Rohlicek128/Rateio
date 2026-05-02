package com.example.rateio.presentation.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import com.example.rateio.presentation.components.AdaptiveAsyncImage
import com.example.rateio.presentation.components.RateBox


@Composable
fun RateItemDetailScreen(
    title: String,
    subtitle: String?,
    description: String?,
    coverImageUrl: String?,
    backdropImageUrl: String?,
    rating: Float?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    ratingLabel: String? = "N/A",
    extraContent: LazyListScope.() -> Unit = {},
    placeholderRatio: Float = 2f / 3f
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 56.dp,
                bottom = innerPadding.calculateBottomPadding(),
            )
        ) {
            // Cover + title header
            item {
                DetailHeader(
                    title = title,
                    subtitle = subtitle,
                    coverImageUrl = coverImageUrl,
                    placeholderRatio = placeholderRatio,
                    backdropImageUrl = backdropImageUrl,
                    rating = rating
                )
            }

            // Description
            if (!description.isNullOrBlank()) {
                item {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Divider before extra content
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) }

            // Type-specific content injected here
            extraContent()

            item { Spacer(modifier = Modifier.height(200.dp)) }
        }


        FilledTonalIconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    start = 12.dp,
                )
                .zIndex(1f),
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}

@Composable
fun PosterWithRating(
    imageUrl: String?,
    rating: Float?,
    modifier: Modifier = Modifier,
    placeholderRatio: Float = 2f / 3f,
) {
    val rateBoxOverhang = 38.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(unbounded = true),
    ) {
        Card(
            onClick = { },
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.align(Alignment.Center)
        ) {
            AdaptiveAsyncImage(
                model = imageUrl,
                placeholderRatio = placeholderRatio,
                maxHeight = 450.dp
            )
        }
        var ratingTest: Float? = null
        if (rating != null) {
            if (rating > 0f) ratingTest = rating
        }
        RateBox(
            rating = ratingTest,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = rateBoxOverhang),
            roundedCorners = 18.dp,
            width = 24.dp,
            minWidth = 42.dp,
            height = 6.dp,
            textStyle = MaterialTheme.typography.displayMedium,
            onClick = { }
        )
    }
}

@Composable
private fun DetailHeader(
    title: String,
    subtitle: String?,
    coverImageUrl: String?,
    placeholderRatio: Float = 2f / 3f,
    backdropImageUrl: String?,
    rating: Float?,
) {
    /*val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier.fillMaxWidth().offset(y = (-56).dp),
    ) {
        AdaptiveAsyncImage(
            model = backdropImageUrl,
            placeholderRatio = 16f / 9f,
            minHeight = 800.dp
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            backgroundColor.copy(alpha = 0.6f),
                            backgroundColor,
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    )
                )
        )

    }*/

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PosterWithRating(
            imageUrl = coverImageUrl,
            placeholderRatio = placeholderRatio,
            rating = rating
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.displaySmall,
                lineHeight = 1.1.em,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Float?, ratingLabel: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (rating != null) {
            // Show as x/10 for readability
            Text(
                text = "%.1f".format(rating * 10),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = ratingLabel ?: "/10",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = "Not yet rated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}