package com.example.rateio.presentation.components.rating

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.DisplaySelector
import com.example.rateio.presentation.components.SortBySelectionButton
import com.example.rateio.presentation.rating.tmdb.SortMode
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import kotlinx.serialization.json.Json


enum class DisplayMode {
    LIST,
    GRID,
    WRAPPED,
    TIMELINE
}

@Composable
fun ChildrenDisplay(
    childrenGroups: Map<RateItem?, List<RateItem>>,
    onChildClick: (RateItem) -> Unit,
    columnText: (Int) -> String,
    rowText: (Int) -> String,
    selectedDisplayMode: DisplayMode,
    onDisplayModeSelect: (DisplayMode) -> Unit,
    selectedSortMode: SortMode,
    onSortModeSelect: (SortMode) -> Unit,
    expandedParents: MutableSet<String?>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    expandIfSingleGroup: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current

    val sortedBestEpisodes = remember(childrenGroups) {
        childrenGroups
            .flatMap { (_, episodes) -> episodes }
            .sortedByDescending { episode ->
                episode.rating ?: -1f
            }
    }
    val sortedEpisodes = remember(childrenGroups, selectedSortMode) {
        childrenGroups
            .flatMap { (_, episodes) -> episodes }
            .let { episodes ->
                when (selectedSortMode) {
                    SortMode.BY_RATING_WORST -> episodes.sortedBy { episode ->
                        episode.rating ?: 2f
                    }
                    SortMode.BY_RUNTIME -> episodes.sortedByDescending { episode ->
                        val metadata = episode.metadataJSON?.let {
                            runCatching {
                                Json.decodeFromString<TmdbEpisodeMetadata>(it)
                            }.getOrNull()
                        }
                        metadata?.runtime
                    }
                    SortMode.BY_NAME -> episodes.sortedBy { episode ->
                        episode.title
                    }
                    else -> sortedBestEpisodes
                }
            }
    }

    if (expandIfSingleGroup && childrenGroups.keys.size == 1)
        expandedParents.add(childrenGroups.keys.first()?.title)

    var invertedGrid by remember { mutableStateOf(false) }
    var columnsWrapped by remember { mutableFloatStateOf(4f) }

    Column (
        modifier = modifier,
    ) {
        DisplaySelector(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            selectedIndex = DisplayMode.entries.indexOf(selectedDisplayMode),
            onSelectionChanged = { onDisplayModeSelect(DisplayMode.entries[it]) },
            options = listOf("List", "Grid", "Wrapped", "Timeline"),
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
                            SortBySelectionButton(
                                selected = selectedSortMode,
                                onSelect = onSortModeSelect,
                            )

                            AnimatedVisibility(
                                visible = selectedSortMode == SortMode.BY_SEASON,
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
                            SortMode.BY_SEASON -> {
                                ChildrenList(
                                    childrenGroups = childrenGroups,
                                    onChildClick = onChildClick,
                                    expandedParents = expandedParents,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    displayNotNullCounter = true,
                                )
                            }
                            else -> {
                                RateItemList(
                                    items = sortedEpisodes,
                                    onChildClick = onChildClick,
                                    modifier = Modifier.padding(horizontal = 16.dp),
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
                        ChildrenGrid(
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            childrenGroups = childrenGroups,
                            rowText = rowText,
                            columnText = columnText,
                            onChildClick = onChildClick,
                        )
                    }
                    DisplayMode.WRAPPED -> {
                        SettingListItem(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            title = "Columns",
                            description = "Value: ${columnsWrapped.toInt() + 1}",
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
                            }
                        )
                        ChildrenWrapped(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            childrenGroups = childrenGroups,
                            columns = columnsWrapped.toInt() + 1,
                            onChildClick = onChildClick,
                        )
                    }
                    DisplayMode.TIMELINE -> {
                        ChildrenTimeline(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            childrenGroups = childrenGroups,
                            onChildClick = onChildClick,
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