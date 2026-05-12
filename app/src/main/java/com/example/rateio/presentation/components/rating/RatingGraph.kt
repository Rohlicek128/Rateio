package com.example.rateio.presentation.components.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.TmdbEpisodeSummary
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.utils.formatDateCompact
import com.example.rateio.utils.formatTime
import kotlin.math.abs


private data class EpisodeRatingPoint(
    val globalIndex: Int,
    val episode: TmdbEpisodeSummary,
    val rating: Float?,
)

@Composable
fun EpisodeRatingGraph(
    episodes: Map<Int, List<TmdbEpisodeSummary>>,
    imdbRatings: Map<Int, Map<Int, Float?>>,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
    episodeWidth: Dp = 6.dp,
) {
    val points = remember(episodes, imdbRatings) {
        var globalIdx = 0
        episodes.entries
            .sortedBy { it.key }
            .flatMap { (season, eps) ->
                eps.sortedBy { it.episodeNumber }.map { ep ->
                    EpisodeRatingPoint(
                        globalIndex = globalIdx++,
                        episode = ep,
                        rating = imdbRatings[season]?.get(ep.episodeNumber),
                    )
                }
            }
    }

    // Index of first episode of each season (except season 1) for divider lines
    val seasonDividerIndices = remember(points) {
        points
            .filter { it.episode.episodeNumber == 1 && it.episode.seasonNumber > 1 }
            .map { it.globalIndex }
    }

    if (points.isEmpty()) return

    val density = LocalDensity.current
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }

    val padL = 40.dp
    val padR = 16.dp
    val padT = 16.dp
    val padB = 0.dp


    val ratingColor = { r: Float -> getRatingColor(r).backgroundColor }
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dividerColor = MaterialTheme.colorScheme.outline
    val lineColor = MaterialTheme.colorScheme.secondaryFixedDim
    val background = MaterialTheme.colorScheme.background
    val labelStyle = MaterialTheme.typography.labelSmall

    var plotScale by rememberSaveable { mutableFloatStateOf(1f) }
    var widthScale by rememberSaveable { mutableFloatStateOf(1f) }

    val totalWidth = padL + (episodeWidth * widthScale) * points.size + padR

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
                .pointerInput(points) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val pos = event.changes.firstOrNull()?.position ?: continue
                            val padLpx = with(density) { padL.toPx() }
                            val epWpx = with(density) { (episodeWidth * widthScale).toPx() }

                            val closest = points.minByOrNull { ep ->
                                val x = padLpx + (ep.globalIndex + 0.5f) * epWpx
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
                val epWpx = (episodeWidth * widthScale).toPx()

                // X center of each episode slot
                fun xOf(i: Int) = padLpx + (i + 0.5f) * epWpx
                fun yOf(r: Float) = padTpx + plotH * (1f - r) * plotScale


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

                // Season divider lines
                seasonDividerIndices.forEach { divIdx ->
                    val x = padLpx + divIdx * epWpx   // left edge of slot, not center
                    drawLine(
                        color = dividerColor.copy(alpha = 0.4f),
                        start = Offset(x, padTpx),
                        end = Offset(x, padTpx + plotH),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(4.dp.toPx(), 3.dp.toPx())
                        ),
                    )
                    val seasonNum = points.first { it.globalIndex == divIdx }.episode.seasonNumber
                    drawContext.canvas.nativeCanvas.drawText(
                        "S$seasonNum",
                        x + 4.dp.toPx(),
                        padTpx + 12.dp.toPx(),
                        android.graphics.Paint().apply {
                            textSize = 10.dp.toPx()
                            color = labelColor.copy(alpha = 0.5f).toArgb()
                            isAntiAlias = true
                        }
                    )
                }

                // S1 label
                drawContext.canvas.nativeCanvas.drawText(
                    "S1",
                    padLpx + 4.dp.toPx(),
                    padTpx + 12.dp.toPx(),
                    android.graphics.Paint().apply {
                        textSize = 10.dp.toPx()
                        color = labelColor.copy(alpha = 0.5f).toArgb()
                        isAntiAlias = true
                    }
                )

                // Connecting line
                val ratedPoints = points.filter { it.rating != null }
                if (ratedPoints.size >= 2) {
                    val path = Path()
                    ratedPoints.forEachIndexed { i, ep ->
                        val x = xOf(ep.globalIndex)
                        val y = yOf(ep.rating!!)
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
                points.forEach { ep ->
                    val rating = ep.rating ?: return@forEach
                    val x = xOf(ep.globalIndex)
                    val y = yOf(rating)
                    val color = ratingColor(rating)
                    val isHovered = ep.globalIndex == hoveredIndex
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
        val hovered = hoveredIndex?.let { idx -> points.getOrNull(idx) }
        AnimatedVisibility(visible = hovered != null) {
            hovered?.let { episodePoint ->
                val episode = episodePoint.episode
                RateItemCard(
                    title = episode.name,
                    subtitle = "S${episode.seasonNumber}E${episode.episodeNumber}  |  ${formatDateCompact(episode.airDate)}",
                    coverImagePath = "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                    rating = episodePoint.rating,
                    bubbleText = if (episode.runtime > 0) formatTime(episode.runtime) else null,
                    placeholderRatio = 16f / 9f,
                    padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    onClick = { onEpisodeClick(
                        episode.seasonNumber,
                        episode.episodeNumber
                    ) },
                )
            }
        }

        Column(
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "Scale: " + "%.1f".format(plotScale))
            Slider(
                value = plotScale,
                onValueChange = { plotScale = it },
                valueRange = 1f..4f,
            )

            //Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Width: " + "%.1f".format(widthScale))
            Slider(
                value = widthScale,
                onValueChange = { widthScale = it },
                valueRange = 1f..5f,
            )
        }
    }
}
