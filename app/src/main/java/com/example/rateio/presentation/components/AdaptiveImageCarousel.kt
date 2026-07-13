package com.example.rateio.presentation.components

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch


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
                .heightIn(max = itemHeight)
                .aspectRatio(images[index].aspectRatio)
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