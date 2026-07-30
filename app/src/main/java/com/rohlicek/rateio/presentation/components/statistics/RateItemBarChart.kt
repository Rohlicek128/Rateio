package com.rohlicek.rateio.presentation.components.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.rohlicek.rateio.presentation.rating.display.getRatingColor
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating

data class BarChartEntry(
    val label: String,
    val itemCount: Int,
    val order: Float = 0.0f,
    val color: Color? = null,
)


@Composable
fun RatingsTransformationBarChart(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<RateItem>,
    onSelect: ((String) -> Unit)? = null,
    trailingTitleContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val rtf = getCurrentRatingTransformations()
    val barChartEntries = remember(entries) {
        val groups = entries.groupBy {
            it.rating?.let { rating ->
                getTransformedRating(rating)
            }
        }
        val groupEntries = groups.mapValues {
            BarChartEntry(
                label = it.key ?: "Null",
                itemCount = it.value.size.coerceAtLeast(0),
                order = it.value.first().rating ?: 0.0f,
                color = getRatingColor(it.value.last().rating).backgroundColor
            )
        }.toMutableMap()
        for (i in 0..rtf.stepCount.toInt()) {
            val ratingGroup = getTransformedRating(i.toFloat() / rtf.stepCount.toFloat())
            groupEntries.putIfAbsent(ratingGroup, BarChartEntry(
                label = ratingGroup,
                itemCount = 0,
                order = i.toFloat() / rtf.stepCount.toFloat(),
            ))
        }
        groupEntries.map { it.value }.sortedBy { it.order }
    }

    BardChartCard(
        modifier = modifier,
        title = title,
        entries =  barChartEntries,
        onSelect = onSelect,
        trailingTitleContent = trailingTitleContent,
    )
}

@Composable
fun RatingsColorBarChart(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<RateItem>,
    onSelect: ((String) -> Unit)? = null,
    trailingTitleContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val rcb = getCurrentRatingColorBuckets()
    val barChartEntries = remember(entries) {
        val groups = entries.groupBy {
            it.rating?.let { rating ->
                getRatingColor(rating)
            }
        }
        val groupEntries = groups.mapValues {
            BarChartEntry(
                label = it.key?.equalOrGreaterThen?.let { egt -> "≥${getTransformedRating(egt)}" } ?: "Null",
                itemCount = it.value.size.coerceAtLeast(0),
                order = it.key?.equalOrGreaterThen ?: 0.0f,
                color = it.key?.backgroundColor ?: rcb.nullBucket.backgroundColor
            )
        }.toMutableMap()
        for ((_, element) in rcb.buckets.withIndex()) {
            groupEntries.putIfAbsent(
                element, BarChartEntry(
                label = "≥${getTransformedRating(element.equalOrGreaterThen)}",
                itemCount = 0,
                order = element.equalOrGreaterThen ?: 0.0f,
            ))
        }
        groupEntries.map { it.value }.sortedBy { it.order }
    }

    BardChartCard(
        modifier = modifier,
        title = title,
        entries =  barChartEntries,
        onSelect = onSelect,
        trailingTitleContent = trailingTitleContent,
    )
}

@Composable
private fun BardChartCard(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<BarChartEntry>,
    onSelect: ((String) -> Unit)? = null,
    trailingTitleContent: @Composable (RowScope.() -> Unit)? = null,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (trailingTitleContent != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    trailingTitleContent()
                }
            }


            BarChart(entries = entries, onSelect = onSelect)
        }
    }
}


@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    onSelect: ((String) -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    val regularColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    val spacing = 6.dp
    val minItemWidth = 20.dp
    val maxItemWidth = 88.dp
    val chartHeight = 236.dp
    val labelMaxLines = 1
    val horizontalContentPadding = 16.dp

    val maxValueEntry = entries.maxBy { it.itemCount }

    val listState = rememberLazyListState()

    LaunchedEffect(key1 = entries.size) {
        if (entries.isNotEmpty()) {
            listState.scrollToItem(entries.size - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val itemCount = entries.size.coerceAtLeast(1)
            val innerWidth = (maxWidth - (horizontalContentPadding * 2)).coerceAtLeast(0.dp)
            val spacingTotal = spacing * (itemCount - 1).coerceAtLeast(0)
            val minimumChartWidth = (minItemWidth * itemCount) + spacingTotal
            val needsHorizontalScroll = minimumChartWidth > innerWidth
            val fittedItemWidth = ((innerWidth - spacingTotal) / itemCount).coerceIn(minItemWidth, maxItemWidth)
            val itemWidth = if (needsHorizontalScroll) minItemWidth else fittedItemWidth

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.Bottom,
                    userScrollEnabled = needsHorizontalScroll,
                    contentPadding = PaddingValues(horizontal = horizontalContentPadding)
                ) {
                    itemsIndexed(
                        items = entries,
                        key = { index, entry -> "${entry.label}-$index" }
                    ) { _, entry ->
                        val value = entry.itemCount.coerceAtLeast(0)
                        val progress = if (maxValueEntry.itemCount > 0.0) (value / maxValueEntry.itemCount.toFloat()).coerceIn(0f, 1f) else 0f

                        Column(
                            modifier = Modifier.width(itemWidth),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = entry.itemCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (entry.itemCount > 0) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(trackColor)
                                    .clickable(onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                        onSelect?.invoke(entry.label)
                                    }),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(if (progress == 0f) 0f else progress)
                                        .clip(CircleShape)
                                        .background(entry.color ?: regularColor)
                                )
                            }
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = labelMaxLines,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}