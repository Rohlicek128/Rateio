package com.example.rateio.presentation.components.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.toPath
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.presentation.rating.display.getRoundedRating
import com.example.rateio.presentation.rating.display.getTransformedRating
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import com.example.rateio.presentation.settings.SettingsValueText
import com.example.rateio.utils.dim
import com.example.rateio.utils.formatTime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random


private data class ChildPoint(
    val globalIndex: Int,
    val child: RateItem,
)

@Composable
fun ChildrenTimeline(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    minEpisodeWidth: Dp = 14.dp,
    placeholderRatio: Float = 16f / 9f,
    sortedChildren: List<RateItem> = emptyList(),
    spoilers: Boolean = true,
    spoilName: Boolean = true,
    trendline: Boolean = true,
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

    val basePaths = remember {
        listOf(
            MaterialShapes.Sunny.toPath().asComposePath(),
            MaterialShapes.VerySunny.toPath().asComposePath(),
            MaterialShapes.Cookie4Sided.toPath().asComposePath(),
            MaterialShapes.Cookie7Sided.toPath().asComposePath(),
            MaterialShapes.Cookie12Sided.toPath().asComposePath(),
            MaterialShapes.Clover4Leaf.toPath().asComposePath(),
            MaterialShapes.SoftBurst.toPath().asComposePath(),
        )

    }

    val density = LocalDensity.current

    var hoveredIndex by remember(childrenGroups) { mutableStateOf<Int?>(null) }
    var hoveredIndexChanged by remember { mutableStateOf(false) }
    var hoverPathIndex by remember { mutableIntStateOf(0) }
    val hoverScale = remember { Animatable(0.35f) }
    LaunchedEffect(hoveredIndex) {
        if (hoveredIndex != null) {
            hoverScale.snapTo(0.35f)
            hoverScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy, // Controls the overshoot/bounce
                    stiffness = Spring.StiffnessLow               // Controls how fast it snaps
                )
            )
        } else {
            hoverScale.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "timeline_spin")
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )


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
    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)

    val minRating = childrenGroups.values.flatten().minBy { it.rating ?: 1f }.rating ?: 1f
    var plotScale by rememberSaveable(childrenGroups) { mutableFloatStateOf(getInitialPlotScale(minRating)) }

    var childWidth by rememberSaveable(childrenGroups) { mutableFloatStateOf(
        max(minEpisodeWidth.value, 350f / flatPoints.size.toFloat())
    ) }
    val childRadius = remember(childrenGroups, childWidth) {
        max(4f, min(9f, (childWidth + 2f) / 3f))
    }
    var tension by rememberSaveable { mutableFloatStateOf(0.13f) }

    val totalWidth = padL + childWidth.dp * flatPoints.size + padR

    val movingAveragePoints = remember(flatPoints, trendline) {
        if (trendline) calculateMovingAverage(flatPoints, windowSize = 6)
        else emptyList()
    }

    val rtf = getCurrentRatingTransformations()

    val textMeasurer = rememberTextMeasurer()
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
                                val epWpx = with(density) { childWidth.dp.toPx() }

                                val closest = flatPoints.minByOrNull { child ->
                                    val x = padLpx + (child.globalIndex + 0.5f) * epWpx
                                    abs(pos.x - x)
                                }

                                if (closest?.globalIndex != hoveredIndex) {
                                    hoveredIndex = closest?.globalIndex
                                    hoveredIndexChanged = true
                                    if (hoveredIndex != null) {
                                        hoverPathIndex = Random.nextInt(0, basePaths.size)
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                    }
                                }
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
                val epWpx = childWidth.dp.toPx()

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
                                plotH
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
                    val parentTextResult = textMeasurer.measure(
                        text = parent?.title ?: "N/A",
                        style = labelStyle
                    )
                    drawText(
                        textLayoutResult = parentTextResult,
                        color = labelColor.copy(alpha = 0.5f),
                        topLeft = Offset(
                            x + 4.dp.toPx(),
                            padTpx + 2.dp.toPx()
                        ),
                    )
                    even = !even
                }
                // S1 label
                val parentTextResult = textMeasurer.measure(
                    text = points.keys.first()?.title ?: "N/A",
                    style = labelStyle
                )
                drawText(
                    textLayoutResult = parentTextResult,
                    color = labelColor.copy(alpha = 0.5f),
                    topLeft = Offset(
                        padLpx + 4.dp.toPx(),
                        padTpx + 2.dp.toPx()
                    ),
                )


                // Y-axis grid lines
                val vMin = rtf.offset / rtf.divider
                val vMax = (rtf.stepCount.toFloat() + rtf.offset) / rtf.divider
                val vTotalRange = vMax - vMin

                val vVisibleRange = vTotalRange / plotScale

                val minPxBetweenLines = 45.dp.toPx()
                val desiredSteps = max(2, (plotH / minPxBetweenLines).toInt())

                val roughStep = vVisibleRange / desiredSteps
                val stepExponent = floor(log10(roughStep + 1e-9f))
                val fraction = roughStep / 10f.pow(stepExponent)
                val niceFraction = when {
                    fraction <= 1.5f -> 1f
                    fraction <= 3f -> 2f
                    fraction <= 7f -> 5f
                    else -> 10f
                }
                val niceVStep = niceFraction * 10f.pow(stepExponent)

                val vMinAllowedStep = 1f / rtf.divider
                val clampedVStep = max(niceVStep, vMinAllowedStep)

                val requiredDecimals = max(0, -stepExponent.toInt())
                val currentDecimals = rtf.decimalPlaces.toInt()
                val decimalOffset = max(0, requiredDecimals - currentDecimals).toUInt()

                val startV = ceil(vMin / clampedVStep) * clampedVStep
                var currentV = startV

                while (currentV <= vMax + 1e-5f) {
                    val r = (currentV * rtf.divider - rtf.offset) / rtf.stepCount.toFloat()
                    val y = yOf(r)

                    if (y in -50f..(h + 50f) && y <= h) {
                        drawLine(
                            color = gridColor,
                            start = Offset(padLpx, y),
                            end = Offset(w - padRpx, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                        val textLayoutResult = textMeasurer.measure(
                            text = getTransformedRating(r, decimalOffset, rtf),
                            style = labelStyle
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            color = labelColor.copy(alpha = 0.6f),
                            topLeft = Offset(
                                x = padLpx - textLayoutResult.size.width - 6.dp.toPx(),
                                y = y - (textLayoutResult.size.height / 2f)
                            )
                        )
                    }
                    currentV += clampedVStep
                }


                // Draw Moving Average path
                if (trendline && movingAveragePoints.size >= 2) {
                    val trendPath = Path().apply {
                        moveTo(xOf(movingAveragePoints.first().first), yOf(movingAveragePoints.first().second))
                        for (i in 1 until movingAveragePoints.size) {
                            lineTo(xOf(movingAveragePoints[i].first), yOf(movingAveragePoints[i].second))
                        }
                    }

                    drawPath(
                        path = trendPath,
                        color = lineColor.copy(alpha = 0.65f),
                        style = Stroke(
                            width = (max(3f, childRadius * 0.5f)).dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10f, 25f),
                                0f
                            )
                        )
                    )
                    /*drawPath(
                        path = trendPath,
                        color = lineColor.copy(alpha = 0.65f),
                        style = Stroke(
                            width = (max(3f, childRadius * 0.45f)).dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        )
                    )*/
                }

                // Connecting line
                /*val ratedPoints = flatPoints.filter { it.child.rating != null }
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
                }*/
                val ratedPoints = flatPoints.filter { it.child.rating != null }
                if (ratedPoints.size >= 2) {
                    // A tension factor: 0.1f to 0.3f gives a beautiful, natural curve.
                    // Higher values make it bend more aggressively; 0f results in straight lines.
                    //val tension = 0.15f

                    for (i in 0 until ratedPoints.size - 1) {
                        val current = ratedPoints[i]
                        val next = ratedPoints[i + 1]

                        val x1 = xOf(current.globalIndex)
                        val y1 = yOf(current.child.rating!!)
                        val x2 = xOf(next.globalIndex)
                        val y2 = yOf(next.child.rating!!)

                        // Neighbors
                        val prev = if (i > 0) ratedPoints[i - 1] else current
                        val x0 = xOf(prev.globalIndex)
                        val y0 = yOf(prev.child.rating!!)

                        val post = if (i + 2 < ratedPoints.size) ratedPoints[i + 2] else next
                        val x3 = xOf(post.globalIndex)
                        val y3 = yOf(post.child.rating!!)

                        // C1
                        val cp1x = x1 + (x2 - x0) * tension
                        val cp1y = y1 + (y2 - y0) * tension

                        // C2
                        val cp2x = x2 - (x3 - x1) * tension
                        val cp2y = y2 - (y3 - y1) * tension

                        val segmentPath = Path().apply {
                            moveTo(x1, y1)
                            cubicTo(
                                x1 = cp1x, y1 = cp1y,
                                x2 = cp2x, y2 = cp2y,
                                x3 = x2, y3 = y2
                            )
                        }

                        val color1 = ratingColor(current.child.rating)
                        val color2 = ratingColor(next.child.rating)
                        val segmentBrush = Brush.linearGradient(
                            colors = listOf(color1, color2),
                            start = Offset(x1, y1),
                            end = Offset(x2, y2)
                        )

                        drawPath(
                            path = segmentPath,
                            brush = segmentBrush,
                            style = Stroke(
                                width = (childRadius * 0.5f).dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Episode dots
                points.forEach { (_, children) ->
                    children.forEach { item ->
                        if (item.globalIndex == hoveredIndex) return@forEach

                        val rating = item.child.rating ?: return@forEach
                        val x = xOf(item.globalIndex)
                        val y = yOf(rating)
                        val color = ratingColor(rating)
                        val radius = childRadius.dp.toPx()

                        drawCircle(color = color, radius = radius, center = Offset(x, y))
                    }
                }
                if (hoveredIndex != null && hoveredIndex!! < flatPoints.size) {
                    val hoveredItem = flatPoints[hoveredIndex!!]
                    if (hoveredItem.child.rating != null) {
                        val rating = hoveredItem.child.rating
                        val x = xOf(hoveredItem.globalIndex)
                        val y = yOf(rating)
                        val color = ratingColor(rating)
                        val radius = childRadius.dp.toPx()

                        // Glow ring on hover
                        /*drawCircle(
                            color = color.copy(alpha = 0.25f),
                            radius = radius * 1.75f,
                            center = Offset(x, y),
                        )*/
                        val hoverRadius = radius * 2f * (1f + 1f * hoverScale.value * (if (hoveredIndexChanged) 0f else 1f))
                        translate(left = x, top = y) {
                            rotate(degrees = rotationDegrees, pivot = Offset.Zero) {
                                scale(scaleX = hoverRadius, scaleY = hoverRadius, pivot = Offset(0.5f, 0.5f)) {
                                    drawPath(
                                        path = basePaths[hoverPathIndex],
                                        color = color.dim(0.2f, alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        val regularRadius = radius * 1.5f * (1f + 1f * hoverScale.value * (if (hoveredIndexChanged) 0f else 1f))
                        translate(left = x, top = y) {
                            rotate(degrees = rotationDegrees, pivot = Offset.Zero) {
                                scale(scaleX = regularRadius, scaleY = regularRadius, pivot = Offset(0.5f, 0.5f)) {
                                    drawPath(
                                        path = basePaths[hoverPathIndex],
                                        color = color
                                    )
                                }
                            }
                        }
                        if (hoveredIndexChanged) hoveredIndexChanged = false
                    }
                }

            }

        }

        // Tooltip below graph for hovered episode
        val hovered = hoveredIndex?.let { idx -> flatPoints.getOrNull(idx) }
        AnimatedVisibility(visible = hovered != null) {
            hovered?.let { point ->
                val child = point.child
                val topIndex = sortedChildren.indexOf(child)
                RateItemCard(
                    title = child.title,
                    subtitle = child.subtitle,
                    overlineText = if (topIndex != -1) "RATED #${topIndex + 1}" else null,
                    coverImagePath = child.coverImageUrl,
                    rating = child.rating,
                    bubbleText = if (child.length != null && child.length > 0f) formatTime(child.length.toInt()) else null,
                    placeholderRatio = placeholderRatio,
                    padding = PaddingValues(16.dp),
                    onClick = { onChildClick(child) },
                    spoilers = spoilers || child.rating != null,
                    spoilName = spoilName,
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
                description = "Adjusts how zoomed in the timeline is.",
                position = ListItemPosition.START,
                supportingContent = {
                    Slider(
                        plotScale,
                        onValueChange = { value ->
                            if (plotScale != value) {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                plotScale = value
                            }
                        },
                        valueRange = 1f..9.99f
                    )
                },
                trailingContent = {
                    SettingsValueText("%.1fx".format(rtf.locale, plotScale))
                },
            )
            SettingListItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Space width",
                description = "Sets the width between each dot in DP.",
                position = ListItemPosition.END,
                supportingContent = {
                    Slider(
                        childWidth,
                        onValueChange = { value ->
                            if (childWidth != value) {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                childWidth = value
                            }
                        },
                        valueRange = max(1f, 350f / flatPoints.size.toFloat())..max(50f, 1000f / flatPoints.size.toFloat())
                    )
                },
                trailingContent = {
                    SettingsValueText("%.1f dp".format(rtf.locale, childWidth))
                },
            )
            /*SettingListItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Tension",
                description = "Tension of the trendline.",
                position = ListItemPosition.END,
                supportingContent = {
                    Slider(
                        tension,
                        onValueChange = { value ->
                            if (tension != value) {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                tension = value
                            }
                        },
                        valueRange = 0f..1.5f
                    )
                },
                trailingContent = {
                    SettingsValueText("%.2f dp".format(rtf.locale, tension))
                },
            )*/
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


private fun calculateMovingAverage(points: List<ChildPoint>, windowSize: Int = 3): List<Pair<Int, Float>> {
    val rated = points.filter { it.child.rating != null }
    if (rated.size < 3) return emptyList()
    val wSize = min(rated.size, windowSize)

    val averages = mutableListOf<Pair<Int, Float>>()

    for (i in rated.indices) {
        // Define sliding window boundaries centered around the current index
        val start = (i - wSize / 2).coerceAtLeast(0)
        val end = (start + wSize).coerceAtMost(rated.size)

        val window = rated.subList(start, end)
        val averageRating = window.map { it.child.rating!! }.average().toFloat()

        averages.add(rated[i].globalIndex to averageRating)
    }
    return averages
}