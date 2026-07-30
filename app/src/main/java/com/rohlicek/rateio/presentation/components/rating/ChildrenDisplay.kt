package com.rohlicek.rateio.presentation.components.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.WrapText
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.components.ConnectedButtonsExpressive
import com.rohlicek.rateio.presentation.components.ConnectedItemSelector
import com.rohlicek.rateio.presentation.components.OutlinedConnectedButtonsExpressive
import com.rohlicek.rateio.presentation.components.ModalSortableEnumSelector
import com.rohlicek.rateio.presentation.components.SortByButton
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucket
import com.rohlicek.rateio.presentation.rating.display.getRatingColor
import com.rohlicek.rateio.presentation.rating.tmdb.SortModeShow
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsValueText
import kotlinx.serialization.json.Json
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.round


enum class DisplayMode {
    LIST,
    GRID,
    WRAPPED,
    TIMELINE
}

@Composable
fun ChildrenDisplay(
    modifier: Modifier = Modifier,
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    columnText: (Int) -> String,
    rowText: (Int) -> String,
    subtitleBuilder: (RateItem, DisplayMode) -> String? = { item, _ -> item.subtitle },
    selectedDisplayMode: DisplayMode,
    onDisplayModeSelect: (DisplayMode) -> Unit,
    selectedSortMode: SortModeShow,
    onSortModeSelect: (SortModeShow) -> Unit,
    selectedOrder: SortOrder,
    onOrderChange: (SortOrder) -> Unit,
    expandedParents: MutableSet<String?>,
    isLoading: Boolean = false,
    isLoadingRatings: Boolean = false,
    spoilers: Boolean = true,
    spoilName: Boolean = true,
    expandIfSingleGroup: Boolean = true,
    showChildRatedCompletion: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current

    val sortedChildrenBest = remember(childrenGroups) {
        childrenGroups
            .flatMap { (_, episodes) -> episodes }
            .sortedByDescending { episode ->
                episode.rating ?: -1f
            }
    }
    val sortedChildrenBestRated = sortedChildrenBest.filter { it.rating != null }
    val sortedTopLimit = getTopRatedLimit(sortedChildrenBestRated.size)
    val sortedChildrenTop = if (sortedTopLimit > 0) sortedChildrenBestRated.take(sortedTopLimit) else emptyList()

    val sortedChildren = remember(childrenGroups, selectedSortMode) {
        childrenGroups
            .flatMap { (_, episodes) -> episodes }
            .let { episodes ->
                when (selectedSortMode) {
                    SortModeShow.RUNTIME -> episodes.sortedBy { episode ->
                        val metadata = episode.metadataJSON?.let {
                            runCatching {
                                Json.decodeFromString<TmdbEpisodeMetadata>(it)
                            }.getOrNull()
                        }
                        episode.length?.toInt() ?: metadata?.runtime
                    }
                    SortModeShow.NAME -> episodes.sortedBy { episode ->
                        episode.title
                    }
                    else -> sortedChildrenBest.asReversed()
                }
            }
    }

    val showGridLegend = true
    val usedBuckets = remember(childrenGroups) {
        if (showGridLegend) {
            childrenGroups
                .flatMap { it.value }
                .filter { it.rating != null }
                .groupBy { getRatingColor(it.rating) }
                .keys.toList().sortedByDescending { it.equalOrGreaterThen }
        }
        else emptyList()
    }
    var selectedBucket by remember { mutableStateOf<RatingColorBucket?>(null) }

    if (expandIfSingleGroup && childrenGroups.keys.size == 1)
        expandedParents.add(childrenGroups.keys.first()?.title)

    var showSortBySheet by remember { mutableStateOf(false) }
    var invertedGrid by remember { mutableStateOf(false) }
    var columnsWrapped by remember { mutableFloatStateOf(4f) }
    var trendline by remember { mutableStateOf(true) }

    val timelineCategories = if (childrenGroups.values.isNotEmpty() && childrenGroups.keys.isNotEmpty()) {
        listOfNotNull(
            childrenGroups.values.first().first().externalSource,
            if (childrenGroups.keys.size > 1) childrenGroups.keys.filterNotNull()
                .first().externalSource else null,
        )
    } else emptyList()
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val parentItem = RateItem(
        id = 100000000,
        categoryId = 0,
        title = if (childrenGroups.keys.isNotEmpty()) childrenGroups.keys.first()?.externalSource?.displayName ?: "Unknown" else "Unknown",
    )
    val parentGroups = mapOf<RateItem?, List<RateItem>>(parentItem to childrenGroups.keys.filterNotNull().toList())

    Column (
        modifier = modifier,
    ) {
        OutlinedConnectedButtonsExpressive(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            selectedIndex = DisplayMode.entries.indexOf(selectedDisplayMode),
            onSelectionChanged = { onDisplayModeSelect(DisplayMode.entries[it]) },
            options = listOf("List", "Grid", "Wrapped", "Timeline"),
            textStyle = MaterialTheme.typography.labelSmall,
            unCheckedIcons = listOf(Icons.AutoMirrored.Outlined.List, Icons.Outlined.GridOn, Icons.AutoMirrored.Outlined.WrapText, Icons.Outlined.Timeline),
            checkedIcons = listOf(Icons.AutoMirrored.Filled.List, Icons.Filled.GridOn, Icons.AutoMirrored.Filled.WrapText, Icons.Filled.Timeline),
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    CircularWavyProgressIndicator()
                }
            }
            childrenGroups.isNotEmpty() -> {
                when (selectedDisplayMode) {
                    DisplayMode.LIST -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            SortByButton(onClick = { showSortBySheet = true })
                            if (showSortBySheet) {
                                ModalSortableEnumSelector(
                                    selectedOption = selectedSortMode,
                                    onOptionSelected = onSortModeSelect,
                                    selectedOrder = selectedOrder,
                                    onOrderChange = onOrderChange,
                                    onDismiss = { showSortBySheet = false },
                                )
                            }

                            AnimatedVisibility(
                                visible = selectedSortMode == SortModeShow.SEASON,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            childrenGroups.keys.forEach { parent ->
                                                expandedParents.add(parent?.title)
                                            }
                                        },
                                        shapes = ButtonDefaults.shapes(),
                                    ) {
                                        Icon(
                                            Icons.Default.UnfoldMore,
                                            contentDescription = "Expand",
                                            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                        Text(
                                            "Expand",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            childrenGroups.keys.forEach { parent ->
                                                expandedParents.remove(parent?.title)
                                            }
                                        },
                                        shapes = ButtonDefaults.shapes(),
                                    ) {
                                        Icon(
                                            Icons.Default.UnfoldLess,
                                            contentDescription = "Collapse",
                                            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                        Text(
                                            "Collapse",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }

                        when (selectedSortMode) {
                            SortModeShow.SEASON -> {
                                ChildrenList(
                                    childrenGroups = childrenGroups,
                                    onChildClick = onChildClick,
                                    expandedParents = expandedParents,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    sortedChildren = sortedChildrenTop,
                                    sortedTopLimit = sortedTopLimit,
                                    subtitleBuilder = { subtitleBuilder(it, DisplayMode.LIST) },
                                    spoilers = spoilers,
                                    spoilName = spoilName,
                                    showChildRatedCompletion = showChildRatedCompletion,
                                    nullIsLoading = isLoadingRatings,
                                )
                            }
                            else -> {
                                RateItemList(
                                    items = if (selectedOrder == SortOrder.ASCENDING) sortedChildren else sortedChildren.asReversed(),
                                    onChildClick = onChildClick,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    sortedChildren = sortedChildren,
                                    sortedTopLimit = if (selectedSortMode == SortModeShow.RATING) sortedTopLimit else 0,
                                    //subtitleBuilder = { subtitleBuilder(it, DisplayMode.LIST) },
                                    reverseSorting = selectedSortMode == SortModeShow.RATING || selectedSortMode == SortModeShow.RUNTIME,
                                    showRanking = true,
                                    spoilers = spoilers,
                                    spoilName = spoilName,
                                    nullIsLoading = isLoadingRatings,
                                )
                            }
                        }
                    }
                    DisplayMode.GRID -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            OutlinedToggleButton(
                                checked = invertedGrid,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    invertedGrid = it
                                },
                                shapes = ToggleButtonDefaults.shapes(),
                            ) {
                                Text(
                                    "Inverted",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (showGridLegend) {
                            BucketLegendChips(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                usedBuckets = usedBuckets,
                                selected = selectedBucket,
                                onSelect = { selectedBucket = it }
                            )
                        }
                        if (childrenGroups.values.isNotEmpty()) {
                            if (childrenGroups.keys.size > 1 || invertedGrid) {
                                ChildrenGridExpressive(
                                    contentPadding = PaddingValues(start = 4.dp, end = 12.dp),
                                    childrenGroups = childrenGroups,
                                    rowText = rowText,
                                    columnText = columnText,
                                    onChildClick = onChildClick,
                                    highlightedBucket = selectedBucket,
                                    nullIsLoading = isLoadingRatings,
                                    inverted = invertedGrid,
                                )
                            }
                            else {
                                ChildrenGrid(
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    childrenGroups = childrenGroups,
                                    rowText = rowText,
                                    columnText = columnText,
                                    onChildClick = onChildClick,
                                    highlightedBucket = selectedBucket,
                                    nullIsLoading = isLoadingRatings,
                                    inverted = invertedGrid,
                                )
                            }
                        }
                    }
                    DisplayMode.WRAPPED -> {
                        SettingListItem(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            title = "Columns",
                            description = null,
                            position = ListItemPosition.SINGLE,
                            supportingContent = {
                                Slider(
                                    columnsWrapped,
                                    onValueChange = { value ->
                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                        columnsWrapped = value
                                    },
                                    steps = 13,
                                    valueRange = 0f..14f
                                )
                            },
                            trailingContent = {
                                SettingsValueText(
                                    (columnsWrapped.toInt() + 1).toString(),
                                    modifier = Modifier.padding(horizontal = 5.dp)
                                )
                            }
                        )
                        if (showGridLegend) {
                            BucketLegendChips(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                usedBuckets = usedBuckets,
                                selected = selectedBucket,
                                onSelect = { selectedBucket = it }
                            )
                        }
                        if (childrenGroups.values.isNotEmpty()) {
                            ChildrenWrappedExpressive(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                childrenGroups = childrenGroups,
                                columns = columnsWrapped.toInt() + 1,
                                onChildClick = onChildClick,
                                highlightedBucket = selectedBucket,
                                nullIsLoading = isLoadingRatings
                            )
                        }
                    }
                    DisplayMode.TIMELINE -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            if (timelineCategories.size > 1) {
                                OutlinedConnectedButtonsExpressive(
                                    selectedIndex = selectedCategoryIndex,
                                    onSelectionChanged = {
                                        selectedCategoryIndex = it
                                    },
                                    options = timelineCategories.map { it.displayName },
                                )
                            }
                            //else Spacer(modifier = Modifier.width(4.dp))

                            OutlinedToggleButton(
                                modifier = Modifier.padding(end = 3.dp),
                                checked = trendline,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    trendline = it
                                },
                                shapes = ToggleButtonDefaults.shapes(),
                            ) {
                                Text(
                                    "Trendline",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (showGridLegend) {
                            BucketLegendChips(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                usedBuckets = usedBuckets,
                            )
                        }
                        ChildrenTimeline(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            childrenGroups = if (selectedCategoryIndex == 0) childrenGroups else parentGroups,
                            onChildClick = if (selectedCategoryIndex == 0) onChildClick else { _ -> },
                            placeholderRatio = if (selectedCategoryIndex == 0) 16f / 9f else 2f / 3f,
                            sortedChildren = sortedChildrenTop,
                            spoilers = spoilers,
                            spoilName = spoilName,
                            trendline = trendline,
                        )
                    }
                    else -> {
                        Text("Not implemented", modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(modifier = Modifier.padding(vertical = 400.dp))
                    }
                }

            }
        }
    }
}


fun expandGroupWhenFirstNull(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    expandedParents: MutableSet<String?>,
) {
    childrenGroups.forEach { (parent, children) ->
        if (children.find { it.rating == null } != null) {
            expandedParents.add(parent?.title)
            return
        }
    }
}


fun getTopRatedLimit(childrenCount: Int): Int {
    return when {
        childrenCount >= 10 -> round(childrenCount * 0.1f).toInt().coerceIn(3, 10)
        childrenCount in 5..<10 -> 1
        else -> 0
    }
}

fun getTopRatedChildren(childrenUnsorted: List<RateItem>): List<RateItem> {
    val sortedChildrenBestRated = childrenUnsorted
        .sortedByDescending { episode -> episode.rating ?: -1f }
        .filter { it.rating != null }
    val sortedTopLimit = getTopRatedLimit(sortedChildrenBestRated.size)
    return if (sortedTopLimit > 0) sortedChildrenBestRated.take(sortedTopLimit) else emptyList()
}