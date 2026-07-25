package com.rohlicek.rateio.presentation.components.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeAggregateChildrenRating
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.utils.formatTime


@Composable
fun ChildrenList(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    expandedParents: MutableSet<String?>,
    modifier: Modifier = Modifier,
    sortedChildren: List<RateItem> = emptyList(),
    sortedTopLimit: Int = 1,
    spoilers: Boolean = true,
    spoilName: Boolean = true,
    showRanking: Boolean = false,
    showChildRatedCompletion: Boolean = false,
    nullIsLoading: Boolean = false,
) {
    Column(
        modifier = modifier,
    ) {
        childrenGroups.forEach { (parent, children) ->
            if (parent == null) {
                RateItemList(
                    items = children,
                    onChildClick = onChildClick,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 32.dp),
                )
                return@forEach
            }
            val isExpanded = parent.title in expandedParents
            val flatRatings = children.mapNotNull { it.rating }
            val averageRating = computeAggregateChildrenRating(children)

            RateItemCard(
                modifier = Modifier.heightIn(min = 100.dp),
                title = parent.title,
                titleStyle = MaterialTheme.typography.headlineSmall,
                subtitle = parent.subtitle,
                coverImagePath = parent.coverImageUrl,
                rating = averageRating,
                padding = PaddingValues(vertical = 6.dp),
                tonalElevation = if (isExpanded) 4.dp else 1.dp,
                isLoading = nullIsLoading && averageRating == null,
                onClick = {
                    if (children.isNotEmpty()) {
                        if (isExpanded) expandedParents.remove(parent.title)
                        else expandedParents.add(parent.title)
                    }
                },
                leadingRateBoxContent = {
                    if (showChildRatedCompletion) {
                        ParentCompletionText(
                            numberOfCompleted = flatRatings.size,
                            numberOfAll = children.size,
                        )
                    }
                },
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )),
                exit = shrinkVertically(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )),
            ) {
                RateItemList(
                    items = children,
                    onChildClick = onChildClick,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 32.dp),
                    sortedChildren = sortedChildren,
                    sortedTopLimit = sortedTopLimit,
                    spoilers = spoilers,
                    spoilName = spoilName,
                    showRanking = showRanking,
                    nullIsLoading = nullIsLoading
                )
            }
        }
    }
}

@Composable
fun RateItemList(
    items: List<RateItem>,
    onChildClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    sortedChildren: List<RateItem> = emptyList(),
    sortedTopLimit: Int = 1,
    reverseSorting: Boolean = false,
    spoilers: Boolean = true,
    spoilName: Boolean = true,
    showRanking: Boolean = false,
    nullIsLoading: Boolean = false,
) {
    Column(
        modifier = modifier,
    ) {
        items.forEach { item ->
            val index = sortedChildren.indexOf(item)
            val rank = when {
                index == -1 -> -1
                reverseSorting -> sortedChildren.size - index
                else -> index + 1
            }
            val isTop = rank <= sortedTopLimit && rank != -1
            RateItemCard(
                title = item.title,
                subtitle = item.subtitle,
                overlineText = if (isTop) "RATED #${rank}" else null,
                overlineTextColor = Color(0xFFF4D03F),
                rank = if (showRanking && rank != -1 && item.rating != null) rank else null,
                coverImagePath = item.coverImageUrl,
                rating = item.rating,
                placeholderRatio = 16f / 9f,
                padding = PaddingValues(vertical = 6.dp),
                bubbleText = if (item.length != null) formatTime(item.length.toInt()) else null,
                onClick = { onChildClick(item) },
                spoilers = spoilers || item.rating != null,
                spoilName = spoilName,
                isLoading = nullIsLoading && !(showRanking && rank != -1 && item.rating != null),
            )
        }
    }
}