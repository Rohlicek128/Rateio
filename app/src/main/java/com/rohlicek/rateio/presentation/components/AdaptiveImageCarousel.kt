package com.rohlicek.rateio.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil3.compose.AsyncImage
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds


data class CarouselImage(
    val filePath: String,
    val aspectRatio: Float,
)

enum class ImageSize {
    MEDIUM,
    LARGE,
}

@Composable
fun AdaptiveImageCarousel(
    urlBuilder: (size: ImageSize, path: String) -> String,
    images:  List<CarouselImage>,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    itemWidth: Dp = 300.dp,
    itemHeight: Dp = 160.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onImageClick: (index: Int) -> Unit = {},
    maximizable: Boolean = false,
    supportingContent: @Composable (RowScope.(url: String?, onDismiss: () -> Unit) -> Unit)? = null,
) {
    val carouselState = rememberCarouselState { images.size }
    val coroutineScope = rememberCoroutineScope()
    var fullScreenUrl by remember { mutableStateOf<String?>(null) }
    var fullScreenIndex by remember { mutableStateOf<Int?>(null) }

    HorizontalUncontainedCarousel(
        state = carouselState,
        itemWidth = itemWidth,
        itemSpacing = 8.dp,
        contentPadding = contentPadding,
        modifier = modifier,
    ) { index ->
        AsyncImage(
            model = urlBuilder(ImageSize.MEDIUM, images[index].filePath),
            contentDescription = null,
            modifier = Modifier
                .height(itemHeight)
                //.aspectRatio(images[index].aspectRatio)
                .maskClip(shape)
                .clickable {
                    onImageClick(index)
                    fullScreenUrl = urlBuilder(ImageSize.LARGE, images[index].filePath)
                    fullScreenIndex = index
                },
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        )
    }

    if (maximizable && fullScreenUrl != null && fullScreenIndex != null) {
        FullScreenImageModal(
            imageUrl = fullScreenUrl!!,
            onDismiss = {
                fullScreenUrl = null
                fullScreenIndex = null
            },
            onPreviousClick = if (fullScreenIndex!! >= 1 && fullScreenIndex!! < images.size) {
                {
                    fullScreenIndex = fullScreenIndex!! - 1
                    fullScreenUrl = urlBuilder(ImageSize.LARGE, images[fullScreenIndex!!].filePath)
                    coroutineScope.launch {
                        carouselState.animateScrollToItem(fullScreenIndex!!)
                    }
                }
            } else null,
            onNextClick = if (fullScreenIndex!! >= 0 && fullScreenIndex!! < images.size - 1) {
                {
                    fullScreenIndex = fullScreenIndex!! + 1
                    fullScreenUrl = urlBuilder(ImageSize.LARGE, images[fullScreenIndex!!].filePath)
                    coroutineScope.launch {
                        carouselState.animateScrollToItem(fullScreenIndex!!)
                    }
                }
            } else null,
            supportingContent = supportingContent,
        )
    }
}


@Composable
fun FullScreenImageModal(
    imageUrl: String,
    onDismiss: () -> Unit,
    onPreviousClick: (() -> Unit)? = null,
    onNextClick: (() -> Unit)? = null,
    supportingContent: @Composable (RowScope.(url: String?, onDismiss: () -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var isLoading by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val windowProvider = LocalView.current.parent as DialogWindowProvider
        windowProvider.window.setDimAmount(0.85f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                if (isLoading) {
                    LoadingIndicator(
                        modifier = Modifier.size(90.dp),
                        color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.9f),
                    )
                }

                Surface(
                    modifier = Modifier.clip(MaterialTheme.shapes.large)
                ){
                    AsyncImage(
                        modifier = Modifier.fillMaxWidth(),
                        model = imageUrl,
                        contentDescription = "Full screen image",
                        contentScale = ContentScale.Fit,
                        onSuccess = {
                            isLoading = false
                        },
                    )
                }

                if (!isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (onPreviousClick != null || onNextClick != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                        isLoading = true
                                        onPreviousClick?.invoke()
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f)
                                    ),
                                    shapes = IconButtonDefaults.shapes(),
                                    enabled = onPreviousClick != null,
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous")
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                        isLoading = true
                                        onNextClick?.invoke()
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f),
                                    ),
                                    shapes = IconButtonDefaults.shapes(),
                                    enabled = onNextClick != null,
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
                                }
                            }
                        }

                        if (supportingContent != null) {
                            supportingContent(imageUrl, onDismiss)
                        }
                        else {
                            FilledTonalButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    copyTextToClipboard(context, imageUrl)
                                },
                                shapes = ButtonDefaults.shapes(),
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                Text(
                                    "Copy URL",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}


@Composable
fun HeroCarousel(
    items: List<RateItem>,
    subtitleBuilder: (RateItem) -> String?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    preferredItemWidth: Dp = 330.dp,
    itemHeight: Dp = 220.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    showOrderedRank: Boolean = false,
    autoScroll: Boolean = false,
    loop: Boolean = true,
    dotIndicator: Boolean = false,
    placeholderPageCount: Int = 20,
    spoilThumbnail: Boolean = true,
    spoilName: Boolean = true,
    spoilRated: Boolean = true,
    showNullRating: Boolean = true,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    customRatingTransform: (RateItem) -> Float? = { it.rating },
    onItemClick: (RateItem) -> Unit,
) {
    val realPageCount = if (items.isNotEmpty()) items.size else placeholderPageCount
    val shape = MaterialTheme.shapes.extraLarge

    val doLoop = loop && items.size > 1

    val pageCount = if (doLoop) 2.0.pow(12.0).toInt() else realPageCount
    val initialPage = if (doLoop) (pageCount / 2) - ((pageCount / 2) % realPageCount) else 0

    val carouselState = rememberCarouselState(
        initialItem = initialPage
    ) { pageCount }

    if (autoScroll && doLoop) {
        LaunchedEffect(carouselState) {
            snapshotFlow { carouselState.isScrollInProgress }
                .collect { isScrolling ->
                    if (!isScrolling) {
                        while (true) {
                            delay(7.seconds)
                            if (!carouselState.isScrollInProgress) {
                                carouselState.animateScrollToItem(
                                    item = carouselState.currentItem + 1,
                                    animationSpec = tween(
                                        durationMillis = 1250,
                                        easing = EaseInOut,
                                    )
                                )
                            } else {
                                break
                            }
                        }
                    }
                }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = preferredItemWidth,
            itemSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) { index ->
            val item = if (!isLoading && items.isNotEmpty()) items[index % realPageCount] else null

            HeroItemCard(
                clipShape = Modifier.maskClip(shape),
                title = item?.title,
                subtitle = if (item != null) subtitleBuilder(item) else null,
                rating = item?.let { customRatingTransform(it) },
                imagePath = item?.backdropImageUrl ?: item?.backdropImageLowUrl ?: item?.coverImageUrl ?: item?.coverImageLowUrl,
                itemHeight = itemHeight,
                rank = if (showOrderedRank) index + 1 else null,
                isLoading = isLoading,
                spoilThumbnail = spoilThumbnail,
                spoilName = spoilName,
                spoilRated = spoilRated,
                showNullRating = showNullRating,
                showContent = carouselState.currentItem == index,
                colorBucketsOverride = colorBucketsOverride,
                onItemClick = if (item != null) {
                    {
                        onItemClick(item)
                    }
                } else null,
            )
        }

        if (dotIndicator && items.size > 1) CarouselDotsIndicator(realPageCount, carouselState)
    }
}

@Composable
fun HeroItemCard(
    modifier: Modifier = Modifier,
    title: String?,
    subtitle: String?,
    rating: Float?,
    imagePath: String?,
    itemHeight: Dp = 220.dp,
    isLoading: Boolean = false,
    rank: Int? = null,
    spoilThumbnail: Boolean = true,
    spoilName: Boolean = true,
    spoilRated: Boolean = true,
    showNullRating: Boolean = true,
    showContent: Boolean = true,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    shape: Shape = MaterialTheme.shapes.extraLarge,
    clipShape: Modifier = Modifier.clip(shape),
    onItemClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .then(clipShape)
            .clickable {
                if (onItemClick != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onItemClick()
                }
            }
    ) {
        AsyncImage(
            model = imagePath,
            contentDescription = title,
            modifier = Modifier
                .height(itemHeight)
                .then(clipShape)
                .then(
                    if (!spoilThumbnail && (rating == null || !spoilRated)) Modifier.blur(24.dp)
                    else Modifier
                ),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
            error = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
        )

        if (!isLoading && title != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(clipShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.8f),
                            )
                        )
                    )
            )
        }

        if (!isLoading && title != null) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 16 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 16 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (rank != null) {
                            Text(
                                text = "${rank}.",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                lineHeight = 1.em,
                                maxLines = 1,
                            )
                        }

                        Column {
                            val hideName = (rating == null || !spoilRated) && !spoilName && !spoilThumbnail
                            val title = if (hideName) subtitle else title
                            Text(
                                text = title ?: "?",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                lineHeight = 1.em,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val subtitle = if (!hideName) subtitle else null
                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    if (rating != null || showNullRating) {
                        Spacer(modifier = Modifier.width(10.dp))

                        RateBox(
                            rating = rating,
                            colorBucketsOverride = colorBucketsOverride,
                        )
                    }
                }
            }
        }
        else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(clipShape)
            ) {
                ScreenLoading(size = 70.dp)
            }
        }
    }
}

@Composable
fun CarouselDotsIndicator(
    itemCount: Int,
    carouselState: CarouselState,
    clickable: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val currentIndex = carouselState.currentItem % itemCount
        repeat(itemCount) { index ->
            val isSelected = currentIndex == index

            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                label = "dotWidth"
            )
            val color = animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                label = "dotColor"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color.value)
                    .then(
                        if (clickable) {
                            Modifier.clickable {
                                if (!isSelected) {
                                    coroutineScope.launch {
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                        val difference = index - currentIndex
                                        val targetIndex = carouselState.currentItem + difference
                                        carouselState.animateScrollToItem(
                                            item = targetIndex,
                                            //animationSpec = tween(
                                            //    durationMillis = 750,
                                            //    easing = FastOutSlowInEasing,
                                            //)
                                        )
                                    }
                                }
                            }
                        } else Modifier
                    )
            )
        }
    }
}