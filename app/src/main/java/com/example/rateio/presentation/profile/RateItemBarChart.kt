package com.example.rateio.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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

data class BarChartEntry(
    val label: String,
    val itemCount: Int,
    val order: Float = 0.0f,
    val color: Color? = null,
)


@Composable
fun RatingsBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier,
    onSelect: ((String) -> Unit)? = null,
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
            Text(
                modifier = Modifier.padding(horizontal = 20.dp),
                text = "All Ratings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

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