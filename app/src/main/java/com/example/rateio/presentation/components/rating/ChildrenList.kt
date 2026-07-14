package com.example.rateio.presentation.components.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.example.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.example.rateio.model.RateItem
import com.example.rateio.model.computeAggregateRating
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.utils.formatTime
import kotlinx.serialization.json.Json


@Composable
fun ChildrenList(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    expandedParents: MutableSet<String?>,
    modifier: Modifier = Modifier,
    displayNotNullCounter: Boolean = false,
    sortedChildren: List<RateItem> = emptyList(),
    spoilers: Boolean = true,
    spoilName: Boolean = true,
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

            RateItemCard(
                modifier = Modifier.heightIn(min = 100.dp),
                title = parent.title,
                titleStyle = MaterialTheme.typography.headlineSmall,
                subtitle = parent.subtitle,
                coverImagePath = parent.coverImageUrl,
                rating = computeAggregateRating(children),
                padding = PaddingValues(vertical = 6.dp),
                tonalElevation = if (isExpanded) 4.dp else 1.dp,
                onClick = {
                    if (children.isNotEmpty()) {
                        if (isExpanded) expandedParents.remove(parent.title)
                        else expandedParents.add(parent.title)
                    }
                },
                leadingRateBoxContent = {
                    if (displayNotNullCounter && children.isNotEmpty() && flatRatings.isNotEmpty()) {
                        Text(
                            "${flatRatings.size}/${children.size}",
                            style = MaterialTheme.typography.titleMedium,
                            //color =  if (flatRatings.size >= season.episodeCount) MaterialTheme.colorScheme.secondary else Color.Unspecified,
                            fontWeight = if (flatRatings.size >= children.size) FontWeight.ExtraBold else null,
                            lineHeight = 1.em,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                    spoilers = spoilers,
                    spoilName = spoilName,
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
    spoilers: Boolean = true,
    spoilName: Boolean = true,
) {
    Column(
        modifier = modifier,
    ) {
        items.forEach { item ->
            val topIndex = sortedChildren.indexOf(item)
            RateItemCard(
                title = item.title,
                subtitle = item.subtitle,
                overlineText = if (topIndex != -1) "RATED #${topIndex + 1}" else null,
                coverImagePath = item.coverImageUrl,
                rating = item.rating,
                placeholderRatio = 16f / 9f,
                padding = PaddingValues(vertical = 6.dp),
                bubbleText = if (item.length != null) formatTime(item.length.toInt()) else null,
                onClick = { onChildClick(item) },
                spoilers = spoilers || item.rating != null,
                spoilName = spoilName,
            )
        }
    }
}