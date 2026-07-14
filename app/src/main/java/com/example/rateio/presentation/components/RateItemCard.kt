package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.rateio.presentation.rating.display.RatingColorBuckets
import com.example.rateio.presentation.rating.display.getCurrentRatingColorBuckets


@Composable
fun RateItemCard(
    title: String,
    subtitle: String?,
    coverImagePath: String?,
    rating: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overlineText: String? = null,
    placeholderRatio: Float = 2f / 3f,
    padding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    tonalElevation: Dp = 1.dp,
    isLoading: Boolean = false,
    rank: Int? = null,
    rankText: (Int) -> String = { "${it}." },
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    bubbleText: String? = null,
    spoilers: Boolean = true,
    spoilName: Boolean = true,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    preciseRatings: Boolean = false,
    leadingRateBoxContent: @Composable (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    //val scope = rememberCoroutineScope()
    //var glowColor by remember { mutableStateOf(Color.Transparent) }

    ItemCard(
        modifier = modifier.padding(padding),
        shape = MaterialTheme.shapes.largeIncreased,
        tonalElevation = tonalElevation,
        contentPadding = PaddingValues(10.dp),
        leadingPadding = if (coverImagePath == null) 12.dp else 0.dp,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        },
        headlineContent = {
            Column(
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (rank != null && overlineText == null) {
                    Text(
                        text = rankText(rank),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondaryFixedDim,
                    )
                }

                Text(
                    title,
                    modifier = Modifier
                        .then(if (!spoilers && !spoilName)
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .blur(12.dp)
                        else Modifier
                        ),
                    style = titleStyle,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 1.em,
                    maxLines = when {
                        overlineText != null -> 2
                        rank != null -> 2
                        else -> 3
                    },
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        overlineContent = {
            overlineText?.let { Text(
                it,
                color = Color(0xFFF4D03F),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            ) }
        },
        subtitleContent = {
            subtitle?.let { Text(
                it,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 3,
                lineHeight = 1.em,
                overflow = TextOverflow.Ellipsis,
            ) }
        },
        leadingContent = if (coverImagePath != null) {
            {
                Box {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        AdaptiveAsyncImage(
                            model = coverImagePath,
                            contentDescription = "Cover image",
                            maxWidth = 120.dp,
                            maxHeight = 120.dp,
                            placeholderRatio = placeholderRatio,
                            blurred = !spoilers,
                            /*onSuccess = { state ->
                                scope.launch(Dispatchers.IO) {
                                    val hardwareBitmap = state.result.image.toBitmap()
                                    val bitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false)

                                    val palette = Palette.from(bitmap).generate()
                                    val argb = palette.dominantSwatch?.rgb
                                        ?: palette.vibrantSwatch?.rgb
                                        ?: palette.mutedSwatch?.rgb
                                        ?: palette.lightVibrantSwatch?.rgb

                                    withContext(Dispatchers.Main) {
                                        glowColor = argb?.let { Color(it) } ?: Color.Transparent
                                    }
                                }
                            }*/
                        )
                    }

                    if (bubbleText != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                            border = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.BottomStart),
                        ) {
                            Text(
                                bubbleText,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        }
                    }

                }
            }
        } else null,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 5.dp),
            ) {
                leadingRateBoxContent?.invoke()
                RateBox(
                    rating = rating,
                    roundedCorners = 12.dp,
                    width = 12.dp,
                    minWidth = 36.dp,
                    height = 5.dp,
                    textStyle = MaterialTheme.typography.headlineSmall,
                    isLoading = isLoading,
                    decimalOffset = if (preciseRatings) 1u else 0u,
                    colorBucketsOverride = colorBucketsOverride,
                )
            }
        },
    )

}


@Composable
fun RateItemGridCard(
    title: String,
    subtitle: String?,
    coverImagePath: String?,
    rating: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    placeholderRatio: Float = 2f / 3f,
    padding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    tonalElevation: Dp = 1.dp,
    isLoading: Boolean = false,
    showNullRatings: Boolean = true,
    rank: Int? = null,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    spoilers: Boolean = true,
    biggerTitle: Boolean = false,
    leadingRateBoxContent: @Composable (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val offset = 6.dp

    ListItem(
        overlineContent = {
            if (coverImagePath != null) {
                Box (
                    modifier = Modifier.padding(bottom = 6.dp),
                ) {
                    Row(
                        //modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Card(
                            shape = MaterialTheme.shapes.large,
                        ) {
                            AdaptiveAsyncImage(
                                model = coverImagePath,
                                contentDescription = "Cover image",
                                maxWidth = size,
                                maxHeight = size,
                                placeholderRatio = placeholderRatio,
                                blurred = !spoilers,
                            )
                        }
                    }

                    if (rating != null || showNullRatings) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f),
                            border = null,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.BottomEnd),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                leadingRateBoxContent?.invoke()

                                RateBox(
                                    rating = rating,
                                    roundedCorners = 10.dp,
                                    width = 12.dp,
                                    minWidth = 36.dp,
                                    height = 2.5.dp,
                                    textStyle = MaterialTheme.typography.headlineSmall,
                                    isLoading = isLoading,
                                    //decimalOffset = if (rank != null) 1u else 0u
                                    colorBucketsOverride = colorBucketsOverride,
                                )
                            }
                        }
                    }

                    if (rank != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(13.dp),
                            border = null,
                            modifier = Modifier
                                .padding(4.dp)
                                .align(Alignment.TopStart),
                        ) {
                            Row(
                                modifier = Modifier.widthIn(min = 46.dp),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "${rank}.",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
                                )
                            }
                        }
                    }

                }

            }
        },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    modifier = Modifier
                        .width(size * placeholderRatio - offset)
                        .offset(x = offset)
                        .then(if (!spoilers)
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .blur(12.dp)
                        else Modifier
                        ),
                    style = if (biggerTitle) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 1.em,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        supportingContent = {
            subtitle?.let { Text(
                it,
                modifier = Modifier.offset(x = offset),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            ) }
        },
        modifier = modifier
            .padding(padding)
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onClick()
            }),
        tonalElevation = tonalElevation
    )
}

@Composable
fun IndexedItem(
    index: Int,
    modifier: Modifier = Modifier,
    item: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

        item()
    }
}