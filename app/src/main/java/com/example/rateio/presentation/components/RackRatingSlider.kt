package com.example.rateio.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.rating.display.getRatingColor
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun RackRatingSlider(
    rating: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    stepCount: Int = 100,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
    tickSpacing: Dp = 10.dp,
    majorTickFrequency: Int = 5,
    minorTickHeightFraction: Float = 0.38f,
    majorTickHeightFraction: Float = 0.62f,
    minorTickWidth: Dp = 1.5.dp,
    majorTickWidth: Dp = 2.5.dp,
    indicatorWidth: Dp = 2.dp,
    hardPart: Float = 0.96f,
    fadeWidthFraction: Float = 0.3f,
) {
    val haptic = LocalHapticFeedback.current

    // Clamp and snap the incoming value to the nearest step
    val clampedValue = rating.coerceIn(0f, 1f)
    val currentStep = (clampedValue * stepCount).roundToInt()

    // Track which step we were on before this drag gesture started so we can
    // fire a haptic on every step boundary crossing.
    val lastHapticStep = remember { mutableIntStateOf(currentStep) }

    // dragOffset accumulates raw pixel drag distance within a single gesture.
    // It resets to 0 at the start of each new drag.
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // pixelsPerStep: computed inside the Canvas via a captured state so we can
    // convert drag distance → step delta outside the Canvas scope.
    var tickSpacingPx by remember { mutableFloatStateOf(0f) }

    // Animated value for smooth visual rendering (does NOT affect reported value)
    val animatedStep by animateFloatAsState(
        targetValue = currentStep.toFloat(),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "rackStep"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(
                if (enabled) Modifier.pointerInput(stepCount) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragOffset = 0f
                            //lastHapticStep.intValue = (value.coerceIn(0f, 1f) * stepCount).roundToInt()
                        },
                        onDragEnd = {
                            //haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (tickSpacingPx <= 0f) return@detectHorizontalDragGestures

                            // Dragging LEFT → value increases (rack scrolls right → higher value)
                            // Dragging RIGHT → value decreases
                            dragOffset -= dragAmount // invert: left drag = positive step

                            val rawStepDelta = dragOffset / tickSpacingPx
                            val stepDelta = rawStepDelta.roundToInt()

                            if (stepDelta != 0) {
                                val newStep = (lastHapticStep.intValue + stepDelta)
                                    .coerceIn(0, stepCount)
                                val newValue = (newStep.toFloat() / stepCount).coerceIn(0f, 1f)

                                // Haptic: limit hit
                                if ((newStep == 0 && lastHapticStep.intValue != 0) ||
                                    (newStep == stepCount && lastHapticStep.intValue != stepCount)
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }

                                // Haptic: step boundary
                                if (newStep != lastHapticStep.intValue) {
                                    if (newStep != stepCount) {
                                        if (newStep >= stepCount * hardPart) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        else if (newStep % majorTickFrequency == 0) {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                        }
                                        else if (newStep != 0) {
                                            haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                        }
                                    }

                                    lastHapticStep.intValue = newStep
                                    dragOffset -= stepDelta * tickSpacingPx
                                    onValueChange(newValue)
                                }
                            }
                        }
                    )
                } else Modifier
            )
    ) {
        val tickSpacingPxCapture = with(androidx.compose.ui.platform.LocalDensity.current) {
            tickSpacing.toPx()
        }

        // Capture for use in gesture handler (read outside Canvas)
        SideEffect { tickSpacingPx = tickSpacingPxCapture }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val pxPerTick = tickSpacingPxCapture
            val fadeWidth = canvasWidth * fadeWidthFraction

            val minorH = canvasHeight * minorTickHeightFraction
            val majorH = canvasHeight * majorTickHeightFraction
            val minorW = minorTickWidth.toPx()
            val majorW = majorTickWidth.toPx()

            // How many ticks fit on screen (plus a buffer so edges are always filled)
            val visibleTicks = (canvasWidth / pxPerTick).toInt() + 4

            // The "camera" is locked at center. The rack offset is how many pixels
            // the rack has scrolled. animatedStep drives the visual position.
            // tick index 0 = value 0, tick index stepCount = value 1.
            val rackOffsetPx = animatedStep * pxPerTick

            // First tick index that might appear on screen
            val firstTickIndex = ((rackOffsetPx - centerX) / pxPerTick).toInt() - 2

            // Draw ticks
            for (i in firstTickIndex..(firstTickIndex + visibleTicks + 4)) {
                if (i !in 0..stepCount) continue

                val screenX = centerX + (i * pxPerTick - rackOffsetPx)
                if (screenX < -pxPerTick || screenX > canvasWidth + pxPerTick) continue

                val isPassed = i <= animatedStep.toInt()
                //var baseColor = if (isPassed) activeColor else inactiveColor
                //if (isMajor) baseColor = getColorSchemeImdbEpisodesNC(i / 100f).first
                val baseColor = getRatingColor(i / stepCount.toFloat()).backgroundColor

                // Fade alpha near edges
                val distFromEdge = minOf(screenX, canvasWidth - screenX)
                val fadeAlpha = (distFromEdge / fadeWidth).coerceIn(0f, 1f)

                val distanceChange = sin((distFromEdge / fadeWidth + 0.2f).coerceIn(0f, 1f) * (PI / 2)).toFloat()


                val tickColor = baseColor.copy(alpha = baseColor.alpha * fadeAlpha * (when {
                    !isPassed -> 0.35f
                    else -> 1f
                }))

                val isMajor = (i % majorTickFrequency == 0)
                val tickH = if (isMajor) majorH else minorH
                val tickW = (if (isMajor) majorW else minorW) * distanceChange

                val topY = (canvasHeight - tickH) / 2f
                val bottomY = topY + tickH

                drawLine(
                    color = tickColor,
                    start = Offset(screenX, topY),
                    end = Offset(screenX, bottomY),
                    strokeWidth = tickW,
                    cap = StrokeCap.Round
                )
            }


            // Center indicator — full height, always activeColor
            val indicatorW = indicatorWidth.toPx()
            drawLine(
                color = activeColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, canvasHeight),
                strokeWidth = indicatorW,
                cap = StrokeCap.Round
            )
        }
    }
}
