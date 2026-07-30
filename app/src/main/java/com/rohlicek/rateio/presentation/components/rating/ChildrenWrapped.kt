package com.rohlicek.rateio.presentation.components.rating

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeAggregateChildrenRating
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.components.RateBoxSizeDefaults
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucket
import com.rohlicek.rateio.presentation.rating.display.getRatingColor
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating


@Composable
fun ChildrenWrapped(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.horizontalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        childrenGroups
            .entries
            .sortedBy { it.key?.id }
            .forEach { (parent, children) ->
                ChildrenSection(
                    parent = parent,
                    children = children,
                    columns = columns.coerceAtLeast(1),
                    onChildClick = onChildClick,
                    modifier = Modifier.padding(contentPadding),
                    highlightedBucket = highlightedBucket,
                    nullIsLoading = nullIsLoading,
                )
            }
    }
}

@Composable
private fun ChildrenSection(
    parent: RateItem?,
    children: List<RateItem>,
    columns: Int,
    onChildClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
) {
    val gridGap = 6.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gridGap)
    ) {
        ParentSectionHeader(parent, children)

        val rows = (children.size + columns - 1) / columns

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < children.size) {
                        RateBox(
                            rating = children[index].rating,
                            widthConstrained = true,
                            size = RateBoxSizeDefaults.REGULAR_GRID,
                            onClick = {
                                onChildClick(children[index])
                            },

                            isLoading = nullIsLoading && children[index].rating == null,
                            modifier = Modifier.darken(highlightedBucket != null && getRatingColor(children[index].rating) != highlightedBucket)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentSectionHeader(
    parent: RateItem?,
    children: List<RateItem>,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = parent?.title ?: "N/A",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        val avg = computeAggregateChildrenRating(children)
        if (avg != null) {
            Spacer(modifier = Modifier.width(8.dp))

            val display = getTransformedRating(avg)
            Text(
                text = "(avg. ${display})",
                style = MaterialTheme.typography.titleMedium,
                //fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun ChildrenWrappedExpressive(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    columns: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.horizontalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        childrenGroups
            .entries
            .sortedBy { it.key?.id }
            .forEach { (parent, children) ->
                ChildrenSectionExpressive(
                    modifier = Modifier.padding(contentPadding),
                    parent = parent,
                    children = children,
                    columns = columns.coerceAtLeast(1),
                    onChildClick = onChildClick,
                    highlightedBucket = highlightedBucket,
                    nullIsLoading = nullIsLoading,
                )
            }
    }
}

@Composable
private fun ChildrenSectionExpressive(
    modifier: Modifier = Modifier,
    parent: RateItem?,
    children: List<RateItem>,
    columns: Int,
    onChildClick: (RateItem) -> Unit,
    highlightedBucket: RatingColorBucket? = null,
    nullIsLoading: Boolean = false,
) {
    val gridGap = 6.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gridGap)
    ) {
        ParentSectionHeader(parent, children)

        val rows = (children.size + columns - 1) / columns

        for (row in 0 until rows) {
            val from = row * columns
            val to = row * columns + columns
            ChildrenRowExpressive(
                children = children.subList(from, kotlin.math.min(children.size, to)),
                rowIndex = row,
                itemSpacing = gridGap,
                highlightedBucket = highlightedBucket,
                nullIsLoading = nullIsLoading,
                onChildClick = onChildClick,
            )
        }
    }
}