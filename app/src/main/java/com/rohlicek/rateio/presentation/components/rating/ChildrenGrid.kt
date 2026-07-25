package com.rohlicek.rateio.presentation.components.rating

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeAggregateChildrenRating
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.components.RateBoxSizeDefaults
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucket
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getRatingColor


@Composable
fun ChildrenGrid(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    columnText: (Int) -> String,
    rowText: (Int) -> String,
    onChildClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
    inverted: Boolean = false,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.horizontalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(25.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(48.dp).padding(top = 34.dp),
            ) {
                val longestSeason = childrenGroups.values.maxBy { it.size }
                for (i in 1..longestSeason.size) {
                    Text(
                        text = rowText(i),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp),
                    )
                }
            }

            childrenGroups
                .entries
                .sortedBy { it.key?.id }
                .forEachIndexed { index, (_, children) ->
                    ChildrenColumn(
                        columnIndex = index + 1,
                        columnText = columnText,
                        children = children,
                        highlightedBucket = highlightedBucket,
                        nullIsLoading = nullIsLoading,
                        onChildClick = onChildClick,
                    )
                }
        }

        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(48.dp),
            ) {
                Text(
                    text = "Avg",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }

            childrenGroups
                .entries
                .sortedBy { it.key?.id }
                .forEach { (_, children) ->
                    val average = computeAggregateChildrenRating(children)
                    RateBox(
                        rating = average,
                        widthConstrained = true,
                        size = RateBoxSizeDefaults.REGULAR_GRID,
                        modifier = Modifier.darken(highlightedBucket != null && getRatingColor(average) != highlightedBucket)
                    )
                }
        }
    }
}

@Composable
private fun ChildrenColumn(
    columnIndex: Int,
    columnText: (Int) -> String,
    children: List<RateItem>?,
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
    onChildClick: (RateItem) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = columnText(columnIndex),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        children?.forEach { child ->
            RateBox(
                rating = child.rating,
                widthConstrained = true,
                size = RateBoxSizeDefaults.REGULAR_GRID,
                onClick = {
                    onChildClick(child)
                },
                isLoading = nullIsLoading && child.rating == null,
                modifier = Modifier.darken(highlightedBucket != null && getRatingColor(child.rating) != highlightedBucket)
            )
        }

        children?.size?.let {
            if (it <= 0) {
                RateBox(
                    rating = null,
                    widthConstrained = true,
                    size = RateBoxSizeDefaults.REGULAR_GRID,
                )
            }
        }
    }
}


@Composable
fun BucketLegendChips(
    modifier: Modifier,
    usedBuckets: List<RatingColorBucket> = emptyList(),
    rcb: RatingColorBuckets = getCurrentRatingColorBuckets(),
    selected: RatingColorBucket? = null,
    onSelect: ((RatingColorBucket?) -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    FlowRow(
        modifier = modifier,
    ) {
        (usedBuckets.ifEmpty { rcb.buckets }).forEach { bucket ->
            val isSelected = selected == bucket

            Card (
                modifier = Modifier
                    .padding(3.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .then(
                        if (onSelect != null) Modifier.clickable(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            onSelect(if (!isSelected) bucket else null)
                        }) else Modifier
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                border = if (isSelected) BorderStroke(1.75.dp, MaterialTheme.colorScheme.onSurfaceVariant) else null,
                colors = CardDefaults.cardColors().copy(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(bucket.backgroundColor),
                    )
                    Text(
                        modifier = Modifier.padding(end = 4.dp),
                        text = bucket.label ?: "Null",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun Modifier.darken(darken: Boolean): Modifier {
    val overlayColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)

    return this.drawWithContent {
        drawContent()
        if (darken) {
            drawRect(color = overlayColor)
        }
    }
}