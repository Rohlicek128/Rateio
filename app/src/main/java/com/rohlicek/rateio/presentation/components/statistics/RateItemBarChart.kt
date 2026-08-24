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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.calculateStandardDeviation
import com.rohlicek.rateio.presentation.components.ConnectedButtonsExpressive
import com.rohlicek.rateio.presentation.components.HeroCarousel
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.RatingTransformation
import com.rohlicek.rateio.presentation.rating.display.getBucketDisplayText
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.rohlicek.rateio.presentation.rating.display.getRatingColor
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

data class BarChartEntry(
    val label: String,
    val itemCount: Int,
    val order: Float = 0.0f,
    val color: Color? = null,
)

enum class RatingBarChartType(override val displayName: String): HasDisplayName {
    RATINGS("Ratings"),
    BUCKETS("Buckets")
}

@Composable
fun RatingsBarChart(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<RateItem>,
    onSelect: ((String) -> Unit)? = null,
    type: RatingBarChartType,
    onTypeSelect: (RatingBarChartType) -> Unit,
    chartHeight: Dp = 236.dp,
    sortOrder: SortOrder = SortOrder.ASCENDING,
    startingFraction: Float? = null,
    showDetailCarousel: Boolean = false,
    onItemClick: ((RateItem) -> Unit)? = null,
) {
    val roundBellCurve = false

    val barChartEntries = remember(entries, type) {
        when (type) {
            RatingBarChartType.RATINGS -> ratingBarChartEntries(entries = entries.filter { it.rating != null }, sortOrder = sortOrder)
            RatingBarChartType.BUCKETS -> bucketBarChartEntries(entries = entries, sortOrder = sortOrder)
        }
    }
    val entriesCountMax = remember(barChartEntries) {
        barChartEntries.maxOf { it.itemCount }
    }

    val flatRatings = remember(entries) {
        entries.mapNotNull { it.rating }.sortedByDescending { it }
    }
    val mean = remember(flatRatings) {
        flatRatings.average().toFloat()
    }
    val deviation = remember(flatRatings, mean) {
        calculateStandardDeviation(flatRatings, mean) ?: 0f
    }

    val rtf = getCurrentRatingTransformations()

    val fraction = if (mean > 0f) startingFraction else 1f
    val startingItem = fraction?.let { round((barChartEntries.size - 1) * it).toInt() }
        ?: round((barChartEntries.size - 1) * mean).toInt()

    var selectedGroup by remember { mutableStateOf("") }

    BarChartCard(
        modifier = modifier,
        title = title,
        entries =  barChartEntries,
        onSelect = { selectedGroup = it },
        chartHeight = chartHeight,
        trailingTitleContent = {
            ConnectedButtonsExpressive(
                selectedIndex = RatingBarChartType.entries.indexOf(type),
                onSelectionChanged = {
                    RatingBarChartType.entries.getOrNull(it)?.let { selected ->
                        onTypeSelect(selected)
                    }
                },
                options = RatingBarChartType.entries.map { it.displayName },
            )
        },
        fillFunction = { x: Float ->
            val probableDensity = bellCurveFunction(x, mean, deviation) * (1f / rtf.stepCount.toFloat()) * flatRatings.size
            (if (roundBellCurve) round(probableDensity) else probableDensity) / entriesCountMax
        }.takeIf { type == RatingBarChartType.RATINGS && entries.size > 15 },
        startingItem = startingItem,
    )

    if (showDetailCarousel) {
        val ratingGroups = remember(entries) {
            entries
                .groupBy { if (it.rating != null) getTransformedRating(it.rating) else null }
                .mapValues { it.value.sortedByDescending { item -> item.rating ?: -1f } }
        }
        val bucketGroups = remember(entries) {
            entries
                .groupBy { getBucketDisplayText(it.rating?.let { rating -> getRatingColor(rating) }) }
                .mapValues { it.value.sortedByDescending { item -> item.rating ?: -1f } }
        }
        val selectedItems = when (type) {
            RatingBarChartType.RATINGS -> ratingGroups[selectedGroup]
            RatingBarChartType.BUCKETS -> bucketGroups[selectedGroup]
        }
        val globalIndexOffset = remember(type, selectedGroup) {
            selectedItems?.firstOrNull()?.rating?.let {
                val index = flatRatings.indexOfFirst { rating -> (rating - 0.000001f) < it }
                if (index != -1) index
                else flatRatings.size - 1
            }
        }

        if (!selectedItems.isNullOrEmpty()) {
            Column(
                modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    text = selectedGroup,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Black,
                )
                HeroCarousel(
                    padding = PaddingValues(bottom = 6.dp),
                    preferredItemWidth = 330.dp,
                    itemHeight = 186.dp,
                    items = selectedItems,
                    subtitleBuilder = { it.subtitle},
                    showOrderedRank = true,
                    globalRankOffset = globalIndexOffset,
                    autoScroll = false,
                    loop = false,
                    dotIndicator = true,
                    placeholderPageCount = 3,
                    onItemClick = onItemClick,
                )
            }
        }
    }
}

@Composable
private fun BarChartCard(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<BarChartEntry>,
    onSelect: ((String) -> Unit)? = null,
    chartHeight: Dp = 236.dp,
    fillFunction: ((x: Float) -> Float)? = null,
    startingItem: Int = entries.size - 1,
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (trailingTitleContent != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    trailingTitleContent()
                }
            }


            BarChart(
                entries = entries,
                onSelect = onSelect,
                chartHeight = chartHeight,
                fillFunction = fillFunction,
                startingItem = startingItem,
            )
        }
    }
}


@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 236.dp,
    fillFunction: ((x: Float) -> Float)? = null,
    onSelect: ((String) -> Unit)? = null,
    startingItem: Int = entries.size - 1,
) {
    val haptic = LocalHapticFeedback.current

    val regularColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val functionColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    val spacing = 6.dp
    val minItemWidth = 20.dp
    val maxItemWidth = 88.dp
    val labelMaxLines = 1
    val horizontalContentPadding = 16.dp

    val maxValueEntry = entries.maxBy { it.itemCount }

    val listState = rememberLazyListState()

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.scrollToItem(entries.size - 1)
        }
    }

    LaunchedEffect(startingItem) {
        if (startingItem in entries.indices) {
            val layoutInfo = listState.layoutInfo
            val containerHeight = layoutInfo.viewportSize.height
            val itemWidth = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0

            val centerOffset = -((containerHeight / 2) - (itemWidth / 2))
            listState.animateScrollToItem(startingItem, scrollOffset = centerOffset)
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
                                if (fillFunction != null && entry.order >= 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(fillFunction(entry.order))
                                            .clip(CircleShape)
                                            .background(functionColor)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(progress)
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


fun ratingBarChartEntries(
    entries: List<RateItem>,
    sortOrder: SortOrder,
    rtf: RatingTransformation = getCurrentRatingTransformations()
): List<BarChartEntry> {
    val groups = entries
        .sortedWith(compareByDescending(nullsLast()) { it.rating })
        .groupBy {
            it.rating?.let { rating ->
                getTransformedRating(rating)
            }
        }
    val groupEntries = groups.mapValues {
        BarChartEntry(
            label = it.key ?: "Null",
            itemCount = it.value.size.coerceAtLeast(0),
            order = it.value.firstOrNull()?.rating ?: -1.0f,
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
    return groupEntries.map { it.value }.sortedBy { it.order * (if (sortOrder == SortOrder.DESCENDING) -1f else 1f) }
}

fun bucketBarChartEntries(
    entries: List<RateItem>,
    sortOrder: SortOrder,
    rcb: RatingColorBuckets = getCurrentRatingColorBuckets()
): List<BarChartEntry> {
    val groups = entries
        .sortedWith(compareByDescending(nullsLast()) { it.rating })
        .groupBy {
            it.rating?.let { rating ->
                getRatingColor(rating)
            }
        }
    val groupEntries = groups.mapValues {
        BarChartEntry(
            label = getBucketDisplayText(it.key),
            itemCount = it.value.size.coerceAtLeast(0),
            order = it.key?.equalOrGreaterThen ?: -1.0f,
            color = it.key?.backgroundColor ?: rcb.nullBucket.backgroundColor
        )
    }.toMutableMap()
    for ((_, element) in rcb.buckets.withIndex()) {
        groupEntries.putIfAbsent(
            element, BarChartEntry(
                label = "≥${getTransformedRating(element.equalOrGreaterThen)}",
                itemCount = 0,
                order = element.equalOrGreaterThen ?: -1.0f,
            ))
    }
    return groupEntries.map { it.value }.sortedBy { it.order * (if (sortOrder == SortOrder.DESCENDING) -1f else 1f) }
}


fun bellCurveFunction(x: Float, mean: Float, deviation: Float): Float {
    return (Math.E.pow(-((x - mean).pow(2) / (2.0 * deviation.pow(2)))) / (deviation * sqrt(2.0 * Math.PI))).toFloat()
}

fun bellCurveProbability(density: Float, width: Float): Float {
    return density * width
}