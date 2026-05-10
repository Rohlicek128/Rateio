package com.example.rateio.presentation.rating

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import androidx.palette.graphics.Palette
import coil3.toBitmap
import com.example.rateio.presentation.components.AdaptiveAsyncImage
import com.example.rateio.presentation.components.RateBox
import com.example.rateio.presentation.components.RatingBottomSheet
import com.example.rateio.utils.formatCompact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    categoryName: String? = null,
    ratingLabel: String? = "N/A",
    ratingVotes: Int? = null,
    extraContent: LazyListScope.() -> Unit = {},
    placeholderRatio: Float = 2f / 3f,
    onRatingSaved: ((Float?) -> Unit)? = null,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            delay(2000)
            isRefreshing = false
        }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .pullToRefresh(
                    state = state,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                ),
            contentPadding = PaddingValues(
                top = 76.dp,
                bottom = innerPadding.calculateBottomPadding(),
            )
        ) {
            // Cover + title header
            item {
                DetailHeader(
                    title = title,
                    subtitle = subtitle,
                    category = categoryName,
                    coverImageUrl = coverImageUrl,
                    placeholderRatio = placeholderRatio,
                    backdropImageUrl = backdropImageUrl,
                    rating = rating,
                    ratingVotes = ratingVotes,
                    onRatingSaved = onRatingSaved,
                )
            }

            // Library
            item {
                var isSaved by remember { mutableStateOf(false) }
                if (isSaved) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        OutlinedToggleButton(
                            checked = isSaved,
                            onCheckedChange = { isSaved = !isSaved },
                            shapes = ToggleButtonDefaults.shapes(),
                        ) {
                            if (isSaved) {
                                Icon(
                                    Icons.Filled.Bookmark,
                                    contentDescription = "Remove from library",
                                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Add to library",
                                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                )
                            }

                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))

                            Text(
                                if (isSaved) "Remove from library" else "Add to library",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Description
            if (!description.isNullOrBlank() && !isRefreshing) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            PullToRefreshDefaults.LoadingIndicator(
                modifier = Modifier.zIndex(1f),
                state = state,
                isRefreshing = isRefreshing,
                maxDistance = 145.dp
            )
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
                contentDescription = "Back",
            )
        }

    }
}


@Composable
private fun DetailHeader(
    title: String,
    subtitle: String?,
    category: String?,
    coverImageUrl: String?,
    placeholderRatio: Float = 2f / 3f,
    backdropImageUrl: String?,
    rating: Float?,
    ratingVotes: Int?,
    onRatingSaved: ((Float?) -> Unit)? = null,
) {
    /*val backgroundColor = MaterialTheme.colorScheme.background
    val offset = (-62).dp

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        AdaptiveAsyncImage(
            model = backdropImageUrl,
            placeholderRatio = 16f / 9f,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .wrapContentWidth(unbounded = true),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                //.offset(y = offset)
                .background(Color.Black.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                //.offset(y = offset)
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

    if (!category.isNullOrBlank()) {
        Text(
            category.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.surfaceVariant,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().offset(y = 10.dp)
        )
    }

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
            rating = rating,
            ratingVotes = ratingVotes,
            onRatingSaved = onRatingSaved
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
private fun PosterWithRating(
    imageUrl: String?,
    rating: Float?,
    ratingVotes: Int?,
    modifier: Modifier = Modifier,
    placeholderRatio: Float = 2f / 3f,
    onRatingSaved: ((Float?) -> Unit)? = null,
) {
    var showRatingSheet by remember { mutableStateOf(false) }
    val rateBoxOverhang = 42.dp

    var ratingPer by remember { mutableStateOf(rating) }

    val scope = rememberCoroutineScope()
    var glowColor by remember { mutableStateOf(Color.Transparent) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(unbounded = true),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(4.dp),
        ) {
            Card(
                onClick = { },
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .drawBehind {
                        if (glowColor != Color.Transparent) {
                            drawIntoCanvas { canvas ->
                                val paint = Paint().apply {
                                    asFrameworkPaint().apply {
                                        isAntiAlias = true
                                        color = glowColor
                                            .copy(alpha = 0.8f)
                                            .toArgb()
                                        maskFilter = BlurMaskFilter(
                                            55.dp.toPx(),
                                            BlurMaskFilter.Blur.NORMAL,
                                        )
                                    }
                                }
                                val inflate = 8.dp.toPx()
                                canvas.drawRoundRect(
                                    left = -inflate,
                                    top = -inflate,
                                    right = size.width + inflate,
                                    bottom = size.height + inflate,
                                    radiusX = 28.dp.toPx(),
                                    radiusY = 28.dp.toPx(),
                                    paint = paint,
                                )
                            }
                        }
                    },
            ) {
                AdaptiveAsyncImage(
                    model = imageUrl,
                    placeholderRatio = placeholderRatio,
                    maxHeight = 450.dp,
                    //minWidth = 200.dp,
                    onSuccess = { state ->
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
                    }
                )
            }
        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = rateBoxOverhang),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RateBox(
                rating = if (onRatingSaved == null) rating else ratingPer,
                roundedCorners = 18.dp,
                width = 24.dp,
                minWidth = 42.dp,
                height = 4.dp,
                textStyle = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                loadingSize = 38.dp,
                onClick = {
                    showRatingSheet = true
                }.takeIf { onRatingSaved != null }
            )

            if (ratingVotes != null && ratingVotes > 0) {
                Text(
                    text = "${formatCompact(ratingVotes)} votes",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant ,
                )
            }
        }

    }

    if (showRatingSheet) {
        RatingBottomSheet(
            rating = ratingPer,
            onDismiss = { showRatingSheet = false },
            onValueChange = { rating ->
                ratingPer = rating
                onRatingSaved?.invoke(rating)
            }
        )
    }
}