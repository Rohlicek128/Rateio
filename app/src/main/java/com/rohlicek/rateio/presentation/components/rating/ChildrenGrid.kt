package com.rohlicek.rateio.presentation.components.rating

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeAggregateChildrenRating
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.components.RateBoxSizeDefaults
import com.rohlicek.rateio.presentation.components.calculateMaxWidthConstrained
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucket
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getRatingColor
import com.rohlicek.rateio.utils.shimmerLoading
import com.rohlicek.rateio.utils.transposeItems


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
fun ChildrenGridExpressive(
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
    val gridSpacing = 6.dp
    val rowTextWidth = 38.dp

    val sortedGroups = remember(childrenGroups, inverted) {
        val sorted = childrenGroups.entries.sortedBy { it.key?.id }.map { it.value }
        if (inverted) transposeItems(sorted) else sorted
    }

    val maxChildrenCount = remember(sortedGroups) {
        sortedGroups.maxOfOrNull { it.size } ?: 0
    }

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .horizontalScroll(horizontalScrollState)
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(gridSpacing)
    ) {
        Row(
            modifier = Modifier.padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(rowTextWidth))
            Row(
                horizontalArrangement = Arrangement.spacedBy(gridSpacing + 0.5.dp),
            ) {
                sortedGroups.forEachIndexed { parentIndex, _ ->
                    Text(
                        text = if (inverted) rowText(parentIndex + 1) else columnText(parentIndex + 1),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(calculateMaxWidthConstrained(RateBoxSizeDefaults.REGULAR_GRID)),
                    )
                }
            }
        }

        for (childIndex in 0 until maxChildrenCount) {
            val interactionSources = remember(sortedGroups.size) {
                List(sortedGroups.size) { MutableInteractionSource() }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (inverted) columnText(childIndex + 1) else rowText(childIndex + 1),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(rowTextWidth),
                )

                ButtonGroup(
                    expandedRatio = 0.45f,
                    overflowIndicator = {},
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                ) {
                    sortedGroups.forEachIndexed { parentIndex, childrenList ->
                        val interactionSource = interactionSources[parentIndex]
                        val child = childrenList.getOrNull(childIndex)

                        customItem(
                            buttonGroupContent = {
                                if (childIndex == 0 && childrenList.isEmpty()) {
                                    RateBox(
                                        rating = null,
                                        widthConstrained = true,
                                        size = RateBoxSizeDefaults.REGULAR_GRID,
                                        interactionSource = interactionSource,
                                        modifier = Modifier.animateWidth(interactionSource = interactionSource)
                                    )
                                }

                                if (child != null) {
                                    val isHighlighted = highlightedBucket == null ||
                                            getRatingColor(child.rating) == highlightedBucket

                                    RateBox(
                                        rating = child.rating,
                                        widthConstrained = true,
                                        size = RateBoxSizeDefaults.REGULAR_GRID,
                                        isLoading = nullIsLoading && child.rating == null,
                                        onClick = { onChildClick(child) },
                                        interactionSource = interactionSource,
                                        modifier = Modifier
                                            .animateWidth(interactionSource = interactionSource)
                                            .darken(!isHighlighted),
                                        glowBest = isHighlighted,
                                    )
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .requiredSize(
                                                width = calculateMaxWidthConstrained(RateBoxSizeDefaults.REGULAR_GRID),
                                                height = RateBoxSizeDefaults.REGULAR_GRID.height
                                            )
                                            .animateWidth(interactionSource = interactionSource)
                                    )
                                }
                            },
                            menuContent = {}
                        )
                    }
                }
            }
        }

        if (!inverted) {
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Avg",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(rowTextWidth),
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                ) {
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
    }
}

@Composable
fun ChildrenRowExpressive(
    modifier: Modifier = Modifier,
    children: List<RateItem?>,
    rowIndex: Int,
    itemSpacing: Dp = 6.dp,
    onChildClick: (RateItem) -> Unit,
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
) {
    val interactionSources = remember(children.size) {
        List(children.size) { MutableInteractionSource() }
    }

    ButtonGroup(
        modifier = modifier,
        expandedRatio = 0.45f,
        overflowIndicator = {},
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        children.forEachIndexed { parentIndex, child ->
            val interactionSource = interactionSources[parentIndex]

            customItem(
                buttonGroupContent = {
                    if (rowIndex == 0 && children.isEmpty()) {
                        RateBox(
                            rating = null,
                            widthConstrained = true,
                            size = RateBoxSizeDefaults.REGULAR_GRID,
                            interactionSource = interactionSource,
                            modifier = Modifier.animateWidth(interactionSource = interactionSource)
                        )
                    }

                    if (child != null) {
                        val isHighlighted = highlightedBucket == null || getRatingColor(child.rating) == highlightedBucket

                        RateBox(
                            rating = child.rating,
                            widthConstrained = true,
                            size = RateBoxSizeDefaults.REGULAR_GRID,
                            isLoading = nullIsLoading && child.rating == null,
                            onClick = { onChildClick(child) },
                            interactionSource = interactionSource,
                            modifier = Modifier
                                .animateWidth(interactionSource = interactionSource)
                                .darken(!isHighlighted),
                            glowBest = isHighlighted,
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .requiredSize(
                                    width = calculateMaxWidthConstrained(RateBoxSizeDefaults.REGULAR_GRID),
                                    height = RateBoxSizeDefaults.REGULAR_GRID.height
                                )
                                .animateWidth(interactionSource = interactionSource)
                        )
                    }
                },
                menuContent = {}
            )
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
                        if (isSelected) {
                            Modifier.shimmerLoading(highlightColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        } else Modifier
                    )
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
    val overlayColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)

    return this.drawWithContent {
        drawContent()
        if (darken) {
            drawRect(color = overlayColor)
        }
    }
}