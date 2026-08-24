package com.rohlicek.rateio.presentation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp


@Composable
fun ScreenScaffold(
    title: String,
    onBackClick: () -> Unit,
    listState: LazyListState = rememberLazyListState(),
    actions: @Composable (RowScope.() -> Unit) = {},
    content: @Composable ((PaddingValues, LazyListState) -> Unit)
) {
    val scrollThresholdPx = with(LocalDensity.current) { 120.dp.toPx() }

    val rawFraction by remember {
        derivedStateOf {
            val offsetPx = if (listState.firstVisibleItemIndex == 0) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else {
                scrollThresholdPx
            }
            (offsetPx / scrollThresholdPx).coerceIn(0f, 1f)
        }
    }

    val fraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "topbar_scroll_fraction",
    )

    Scaffold(
        topBar = {
            ExpressiveTopBar(
                title = title,
                onBackClick = onBackClick,
                actions = actions,
                fraction = fraction,
            )
        }
    ) { padding ->
        content(padding, listState)
    }
}

@Composable
private fun ExpressiveTopBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    fraction: Float,
) {
    val haptic = LocalHapticFeedback.current

    val containerColor = lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceContainerHigh,
        fraction,
    )

    val titleFontWeight = lerp(
        FontWeight.Black.weight,
        FontWeight.Bold.weight,
        fraction,
    )

    val maxSize = MaterialTheme.typography.displaySmall.fontSize
    val minSize = MaterialTheme.typography.headlineSmall.fontSize

    TopAppBar(
        title = {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx().toInt() }

                val fittedMaxSize = rememberFittedTitleFontSize(
                    text = title,
                    maxWidthPx = maxWidthPx - 140,
                    maxFontSize = maxSize,
                    fontWeight = FontWeight.Black,
                )

                val collapsedTarget = minOf(minSize.value, fittedMaxSize.value).sp
                val titleFontSize = lerp(fittedMaxSize.value, collapsedTarget.value, fraction).sp

                Text(
                    text = title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight(titleFontWeight),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            FilledTonalIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    onBackClick()
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                ),
                shapes = IconButtonDefaults.shapes(),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
        ),
    )
}

@Composable
fun rememberFittedTitleFontSize(
    text: String,
    maxWidthPx: Int,
    maxFontSize: TextUnit,
    minFontSize: TextUnit = 11.sp,
    fontWeight: FontWeight = FontWeight.Bold,
): TextUnit {
    val textMeasurer = rememberTextMeasurer()

    return remember(text, maxWidthPx, maxFontSize, minFontSize) {
        if (maxWidthPx <= 0) return@remember maxFontSize

        var fontSize = maxFontSize
        while (fontSize > minFontSize) {
            val result = textMeasurer.measure(
                text = text,
                style = TextStyle(fontSize = fontSize, fontWeight = fontWeight),
                maxLines = 1,
                softWrap = false,
            )
            if (result.size.width <= maxWidthPx) break
            fontSize = (fontSize.value - 1f).sp
        }
        fontSize
    }
}