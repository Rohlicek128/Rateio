package com.example.rateio.presentation.components.rating

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.example.rateio.model.RateItem
import com.example.rateio.model.computeAggregateRating
import com.example.rateio.presentation.components.RateBox
import com.example.rateio.presentation.components.RateBoxSizeDefaults


@Composable
fun ChildrenGrid(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    columnText: (Int) -> String,
    rowText: (Int) -> String,
    onChildClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
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
                    AverageRatingBox(children)
                }
        }
    }
}

@Composable
private fun ChildrenColumn(
    columnIndex: Int,
    columnText: (Int) -> String,
    children: List<RateItem>?,
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

        //val width = 39.dp
        //val height = 4.dp
        children
            ?.forEach { child ->
                RateBox(
                    rating = child.rating,
                    widthConstrained = true,
                    size = RateBoxSizeDefaults.REGULAR_GRID,
                    onClick = {
                        onChildClick(child)
                    }
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
private fun AverageRatingBox(
    children: List<RateItem>,
) {
    RateBox(
        rating = computeAggregateRating(children),
        widthConstrained = true,
        size = RateBoxSizeDefaults.REGULAR_GRID,
    )
}