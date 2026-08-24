package com.rohlicek.rateio.presentation.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.palette.graphics.Palette
import coil3.toBitmap
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getRatingColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.floor
import kotlin.math.log10


@Composable
fun RateItemCard(
    title: String,
    subtitle: String?,
    coverImagePath: String?,
    rating: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    overlineText: String? = null,
    overlineTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
    placeholderRatio: Float = 2f / 3f,
    padding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    tonalElevation: Dp = 1.dp,
    isLoading: Boolean = false,
    rank: Int? = null,
    rankText: (Int) -> String = { "${it}." },
    rankHighlighted: Boolean = false,
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
    //var coverPalette by remember { mutableStateOf<Palette?>(null) }
    //val coverColorSwatch = coverPalette?.lightVibrantSwatch ?: coverPalette?.vibrantSwatch
    //val ratingColor = getRatingColor(rating, buckets = colorBucketsOverride)

    val coverImageSize = 120.dp

    RankedItemWrapper(
        modifier = modifier,
        rank = rank,
        rankText = rankText,
        shape = MaterialTheme.shapes.largeIncreased,
        //containerColor = coverColorSwatch?.rgb?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer,
        //contentColor = coverColorSwatch?.titleTextColor?.let { Color(it) } ?: MaterialTheme.colorScheme.onPrimaryContainer,
        containerColor = if (rankHighlighted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = if (rankHighlighted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
        //containerColor = MaterialTheme.colorScheme.primaryContainer,
        //contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        //containerColor = ratingColor.backgroundColor,
        //contentColor = ratingColor.foregroundColor,
        forcedHeight = if (placeholderRatio < 1f)
            coverImageSize + padding.calculateTopPadding() + padding.calculateBottomPadding()
        else null,
        padding = padding,
    ) { rankedModifier ->
        ItemCard(
            modifier = rankedModifier,
            shape = MaterialTheme.shapes.largeIncreased,
            tonalElevation = tonalElevation,
            contentPadding = PaddingValues(10.dp),
            leadingPadding = if (coverImagePath == null) 12.dp else 0.dp,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onClick()
            },
            headlineContent = {
                Text(
                    title,
                    modifier = Modifier.then(
                        if (!spoilers && !spoilName)
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
                        else -> 3
                    },
                    overflow = TextOverflow.Ellipsis,
                )
            },
            overlineContent = {
                overlineText?.let { Text(
                    it,
                    color = overlineTextColor,
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
                                modifier = Modifier.fillMaxHeight(),
                                model = coverImagePath,
                                contentDescription = "Cover image",
                                maxWidth = coverImageSize,
                                maxHeight = coverImageSize,
                                placeholderRatio = placeholderRatio,
                                blurred = !spoilers,
                                /*onSuccess = { state ->
                                    scope.launch(Dispatchers.IO) {
                                        val hardwareBitmap = state.result.image.toBitmap()
                                        val bitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, false)

                                        val palette = Palette.from(bitmap).generate()
                                        withContext(Dispatchers.Main) {
                                            coverPalette = palette
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
                        isLoading = isLoading,
                        decimalOffset = if (preciseRatings) 1u else 0u,
                        colorBucketsOverride = colorBucketsOverride,
                    )
                }
            },
        )

    }
}


@Composable
fun RateItemGridCard(
    title: String,
    subtitle: String?,
    coverImagePath: String?,
    rating: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
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
    val textOffset = 6.dp

    ItemCard(
        modifier = modifier.sizeIn(maxWidth = size)
            .padding(padding),
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(12.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        },
        tonalElevation = tonalElevation,
        overlineContent = {
            if (coverImagePath != null) {
                Box(modifier = Modifier.padding(bottom = 6.dp)) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                    ) {
                        AdaptiveAsyncImage(
                            model = coverImagePath,
                            contentDescription = "Cover image",
                            minHeight = size,
                            //maxHeight = size,
                            placeholderRatio = placeholderRatio,
                            blurred = !spoilers,
                        )
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
            Text(
                text = title,
                modifier = Modifier
                    .padding(start = textOffset)
                    .then(
                        if (!spoilers)
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .blur(12.dp)
                        else Modifier
                    ),
                style = if (biggerTitle) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                lineHeight = 1.em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            subtitle?.let { Text(
                text = it,
                modifier = Modifier.padding(start = textOffset),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 3,
                lineHeight = 1.em,
                overflow = TextOverflow.Ellipsis,
            ) }
        },
    )
}

@Composable
fun RateItemGridCardOld(
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
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.BottomEnd),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            leadingRateBoxContent?.invoke()

                            RateBox(
                                rating = rating,
                                isLoading = isLoading,
                                //decimalOffset = if (rank != null) 1u else 0u
                                colorBucketsOverride = colorBucketsOverride,
                            )
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
                        .then(
                            if (!spoilers)
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

@Composable
fun RankedItemWrapper(
    rank: Int?,
    shape: CornerBasedShape,
    modifier: Modifier = Modifier,
    rankText: (Int) -> String = { "${it}." },
    padding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    forcedHeight: Dp? = null,
    content: @Composable (modifier: Modifier) -> Unit,
) {
    val rankWidth = if (rank != null) {
        40.dp + floor(log10(rank.toFloat())).toInt().dp * 14
    } else {
        0.dp
    }

    val animatedStartPadding by animateDpAsState(
        targetValue = rankWidth,
        animationSpec = tween(durationMillis = 250),
        label = "RankRevealAnimation"
    )

    if (rank != null || animatedStartPadding.value > 0) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            shape = shape,
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (forcedHeight != null) Modifier.height(forcedHeight)
                        else Modifier.height(IntrinsicSize.Max)
                    )
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(rankWidth + 32.dp),
                        //.fillMaxHeight(0.995f),
                    shape = shape.copy(
                        topEnd = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    ),
                    color = containerColor,
                    contentColor = contentColor,
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f),
                                        Color.Black.copy(alpha = 0.9f),
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .width(rankWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (rank != null) {
                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = rankText(rank),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                maxLines = 2,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }
                }

                content(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = animatedStartPadding)
                )
            }
        }
    }
    else {
        content(modifier.padding(padding))
    }
}