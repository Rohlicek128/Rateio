package com.example.rateio.presentation.components.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.presentation.rating.display.getRoundedRating
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import kotlin.math.abs
import kotlin.math.max


private data class ChildPoint(
    val globalIndex: Int,
    val child: RateItem,
)

@Composable
fun ChildrenTimeline(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    minEpisodeWidth: Dp = 7.dp,
) {
    val haptic = LocalHapticFeedback.current

    val points = remember(childrenGroups) {
        var globalIdx = 0
        childrenGroups
            .mapValues { (_, children) ->
                children.map { child ->
                    ChildPoint(
                        globalIndex = globalIdx++,
                        child = child,
                    )
                }
            }
    }
    val flatPoints = points.flatMap { it.value }

    if (flatPoints.isEmpty()) return

    val density = LocalDensity.current
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }

    val padL = 40.dp
    val padR = 16.dp
    val padT = 16.dp
    val padB = 0.dp


    val ratingColor = { r: Float -> getRatingColor(getRoundedRating(r)).backgroundColor }
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outline
    val dividerBackColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val lineColor = MaterialTheme.colorScheme.secondaryFixedDim


    val minRating = childrenGroups.values.flatten().minBy { it.rating ?: 1f }.rating ?: 1f
    var plotScale by rememberSaveable(childrenGroups) { mutableFloatStateOf(getInitialPlotScale(minRating)) }

    var episodeWidth by rememberSaveable(childrenGroups) { mutableFloatStateOf(
        max(minEpisodeWidth.value, 350f / flatPoints.size.toFloat())
    ) }

    val totalWidth = padL + episodeWidth.dp * flatPoints.size + padR

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(250.dp)
                    .pointerInput(flatPoints) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val pos = event.changes.firstOrNull()?.position ?: continue
                                val padLpx = with(density) { padL.toPx() }
                                val epWpx = with(density) { episodeWidth.dp.toPx() }

                                val closest = flatPoints.minByOrNull { child ->
                                    val x = padLpx + (child.globalIndex + 0.5f) * epWpx
                                    abs(pos.x - x)
                                }
                                hoveredIndex = closest?.globalIndex
                            }
                        }
                    }
            ) {
                val w = totalWidth.toPx()
                val h = size.height
                val padLpx = padL.toPx()
                val padRpx = padR.toPx()
                val padTpx = padT.toPx()
                val padBpx = padB.toPx()
                val plotH = h - padTpx - padBpx
                val epWpx = episodeWidth.dp.toPx()

                // X center of each episode slot
                fun xOf(i: Int) = padLpx + (i + 0.5f) * epWpx
                fun yOf(r: Float) = padTpx + plotH * (1f - r) * plotScale


                // Season divider lines
                var isFirst = true
                var even = true
                points.forEach { (parent, childPoints) ->
                    if (isFirst || childPoints.isEmpty()) {
                        isFirst = false
                        return@forEach
                    }
                    val firstIdx = childPoints.first().globalIndex
                    val nextIdx = childPoints.last().globalIndex + 1
                    val x = padLpx + firstIdx * epWpx   // left edge of slot, not center

                    if (even) {
                        drawRect(
                            //color = dividerBackColor.copy(alpha = 1f),
                            color = dividerColor.copy(alpha = 0.05f),
                            topLeft = Offset(x, padTpx),
                            size = Size(
                                (nextIdx - firstIdx) * epWpx,
                                padTpx + plotH
                            )
                        )
                    }
                    drawLine(
                        color = dividerColor.copy(alpha = 0.4f),
                        start = Offset(x, padTpx),
                        end = Offset(x, padTpx + plotH),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                        ),
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        parent?.title ?: "N/A",
                        x + 4.dp.toPx(),
                        padTpx + 12.dp.toPx(),
                        android.graphics.Paint().apply {
                            textSize = 10.dp.toPx()
                            color = labelColor.copy(alpha = 0.5f).toArgb()
                            isAntiAlias = true
                        }
                    )
                    even = !even
                }

                // S1 label
                drawContext.canvas.nativeCanvas.drawText(
                    points.keys.first()?.title ?: "N/A",
                    padLpx + 4.dp.toPx(),
                    padTpx + 12.dp.toPx(),
                    android.graphics.Paint().apply {
                        textSize = 10.dp.toPx()
                        color = labelColor.copy(alpha = 0.5f).toArgb()
                        isAntiAlias = true
                    }
                )

                // Y-axis grid lines
                for (r in 0..10) {
                    val y = yOf(r / 10f)
                    drawLine(
                        color = gridColor,
                        start = Offset(padLpx, y),
                        end = Offset(w - padRpx, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        r.toString(),
                        padLpx - 6.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            textSize = 10.dp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                            color = labelColor.copy(alpha = 0.6f).toArgb()
                            isAntiAlias = true
                        }
                    )
                }


                // Connecting line
                val ratedPoints = flatPoints.filter { it.child.rating != null }
                if (ratedPoints.size >= 2) {
                    val path = Path()
                    ratedPoints.forEachIndexed { i, item ->
                        val x = xOf(item.globalIndex)
                        val y = yOf(item.child.rating!!)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        )
                    )
                }

                // Episode dots
                points.forEach { (_, children) ->
                    children.forEach { item ->
                        val rating = item.child.rating ?: return@forEach
                        val x = xOf(item.globalIndex)
                        val y = yOf(rating)
                        val color = ratingColor(rating)
                        val isHovered = item.globalIndex == hoveredIndex
                        val radius = 4.dp.toPx()

                        // Glow ring on hover
                        if (isHovered) {
                            drawCircle(
                                color = color.copy(alpha = 0.25f),
                                radius = radius * 1.75f,
                                center = Offset(x, y),
                            )
                        }
                        drawCircle(color = color, radius = radius, center = Offset(x, y))
                    }
                }

                // X axis labels
                /*points.forEach { ep ->
                    drawContext.canvas.nativeCanvas.drawText(
                        ep.episode.episodeNumber.toString(),
                        xOf(ep.globalIndex),
                        h - 8.dp.toPx(),
                        android.graphics.Paint().apply {
                            textSize = 11.dp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            color = labelColor.copy(alpha = 0.5f).toArgb()
                            isAntiAlias = true
                        }
                    )
                }*/
            }

        }

        // Tooltip below graph for hovered episode
        val hovered = hoveredIndex?.let { idx -> flatPoints.getOrNull(idx) }
        AnimatedVisibility(visible = hovered != null) {
            hovered?.let { point ->
                val child = point.child
                RateItemCard(
                    title = child.title,
                    subtitle = child.subtitle,
                    coverImagePath = child.coverImageUrl,
                    rating = child.rating,
                    //bubbleText = if (child.runtime > 0) formatTime(child.runtime) else null,
                    placeholderRatio = 16f / 9f,
                    padding = PaddingValues(16.dp),
                    onClick = { onChildClick(child) },
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SettingListItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Scale",
                description = "Value: ${"%.1f".format(plotScale)}",
                position = ListItemPosition.START,
                supportingContent = {
                    Slider(
                        plotScale,
                        onValueChange = { value ->
                            //haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                            plotScale = value
                        },
                        valueRange = 1f..5f
                    )
                }
            )
            SettingListItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Episode width",
                description = "Value: ${"%.1f".format(episodeWidth)}",
                position = ListItemPosition.END,
                supportingContent = {
                    Slider(
                        episodeWidth,
                        onValueChange = { value ->
                            //haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                            episodeWidth = value
                        },
                        valueRange = 1f..50f
                    )
                }
            )
        }
    }
}

private fun getInitialPlotScale(minRating: Float): Float {
    val initialMin = 0.5
    val initialMax = 1f
    val targetMin = 1.0
    val targetMax = 4.0

    val mapped = targetMin + (minRating - initialMin) * (targetMax - targetMin) / (initialMax - initialMin)
    return mapped.coerceIn(targetMin, targetMax).toFloat()
}
