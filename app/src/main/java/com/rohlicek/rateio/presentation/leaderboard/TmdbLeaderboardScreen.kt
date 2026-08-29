package com.rohlicek.rateio.presentation.leaderboard

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeWeightedRating
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.category.GroupByLibrary
import com.rohlicek.rateio.presentation.category.SortModeLibrary
import com.rohlicek.rateio.presentation.components.ConnectedItemSelector
import com.rohlicek.rateio.presentation.components.ExpressiveScrollBar
import com.rohlicek.rateio.presentation.components.GroupByButton
import com.rohlicek.rateio.presentation.components.ModalSortableEnumSelector
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.components.SortByButton
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.components.rating.ParentCompletionText
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating
import com.rohlicek.rateio.presentation.rating.tmdb.RatingsSource
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsListHeader
import com.rohlicek.rateio.presentation.settings.SettingsSwitch
import com.rohlicek.rateio.utils.formatCompact
import com.rohlicek.rateio.utils.formatDate
import com.rohlicek.rateio.utils.parseDate


@Composable
fun TmdbLeaderboardScreen(
    category: CategoryType,
    onItemClick: (tmdbId: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }
    val categoryRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        CategoryRepository(db.categoryDao())
    }
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: TmdbLeaderboardViewModel = viewModel(
        key = category.name,
        factory = TmdbLeaderboardViewModel.factory(category, categoryRepository, itemRepository, imdbRepository),
    )
    val state by viewModel.state.collectAsState()
    val queryParamsState by viewModel.queryParams.collectAsState()

    //val haptic = LocalHapticFeedback.current

    val ratings: Map<Int, RatingData> = remember(state.imdbRatings, state.tmdbRatings, state.selectedRatingsSource) {
        when (state.selectedRatingsSource) {
            RatingsSource.TMDB -> state.tmdbRatings
            RatingsSource.IMDB -> state.imdbRatings
            else -> emptyMap()
        }
    }

    val sortedItems = remember(state.results, ratings, queryParamsState.sortBy, queryParamsState.sortOrder) {
        val sorted = when (queryParamsState.sortBy) {
            DiscoverSortBy.VOTE_AVERAGE -> state.results.sortedWith(
                compareByDescending<RateItem, Float?>(nullsFirst()) {
                    it.externalId?.toInt()?.let { id -> ratings[id]?.rating }
                }.thenByDescending(nullsFirst()) { it.externalId?.toInt()?.let { id -> ratings[id]?.votes } }
            )
            DiscoverSortBy.VOTE_COUNT -> state.results.sortedByDescending {
                it.externalId?.toInt()?.let { id -> ratings[id]?.votes }
            }
            else -> state.results
        }

        if (queryParamsState.sortOrder == SortOrder.DESCENDING) sorted else sorted.asReversed()
    }

    val groupedItems: Map<String?, List<RateItem>> = remember(sortedItems, state.groupByMode, state.groupByOrder, ratings) {
        when (state.groupByMode) {
            GroupByLibrary.YEAR -> {
                val yearComparator = Comparator<String?> { s1, s2 ->
                    val year1 = s1?.toIntOrNull()
                    val year2 = s2?.toIntOrNull()
                    when {
                        year1 == null && year2 == null -> 0
                        year1 == null -> 1
                        year2 == null -> -1
                        else -> year1.compareTo(year2)
                    }
                }
                val finalComparator = if (state.groupByOrder == SortOrder.DESCENDING) {
                    yearComparator.reversed()
                } else yearComparator

                sortedItems
                    .groupBy { it.subtitle }
                    .toSortedMap(finalComparator)
            }
            else -> mapOf(null to sortedItems)
        }
    }

    var showSortBySheet by remember { mutableStateOf(false) }
    var showGroupBySheet by remember { mutableStateOf(false) }
    val orderInteractionSources = remember(2) {
        List(2) { MutableInteractionSource() }
    }

    ScreenScaffold(
        title = "Leaderboard of ${category.displayName}",
        onBackClick = onBackClick,
    ) { padding, listState ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(start = 18.dp, end = 28.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = padding,
                state = listState,
            ) {
                item {
                    SettingListItem(
                        title = "Ratings Source",
                        description = "Select from which source will the ratings be from.",
                        icon = Icons.Default.CloudDownload,
                        position = ListItemPosition.SINGLE,
                        supportingContent = {
                            ConnectedItemSelector(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                selectedIndex = RatingsSource.entries.indexOf(state.selectedRatingsSource),
                                onSelectionChanged = {
                                    viewModel.onSelectRatings(RatingsSource.entries[it])
                                },
                                options = RatingsSource.entries.take(2).map { it.displayName },
                            )
                        }
                    )
                }

                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ButtonGroup(
                            modifier = Modifier.fillMaxWidth(),
                            expandedRatio = 0.2f,
                            overflowIndicator = {},
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            customItem(
                                buttonGroupContent = {
                                    GroupByButton(
                                        modifier = Modifier.weight(1f).animateWidth(interactionSource = orderInteractionSources[0]),
                                        interactionSource = orderInteractionSources[0],
                                        onClick = { showGroupBySheet = true }
                                    )
                                },
                                menuContent = {}
                            )
                            customItem(
                                buttonGroupContent = {
                                    SortByButton(
                                        modifier = Modifier.weight(1f).animateWidth(interactionSource = orderInteractionSources[1]),
                                        interactionSource = orderInteractionSources[1],
                                        onClick = { showSortBySheet = true }
                                    )
                                },
                                menuContent = {}
                            )
                        }
                        if (showGroupBySheet) {
                            ModalSortableEnumSelector(
                                title = "Group By",
                                selectedOption = state.groupByMode,
                                onOptionSelected = {
                                    if (it == state.groupByMode) viewModel.onSelectGroupBy(GroupByLibrary.NONE)
                                    else viewModel.onSelectGroupBy(it)
                                },
                                selectedOrder = state.groupByOrder,
                                onOrderChange = viewModel::onSelectGroupOrder,
                                onDismiss = { showGroupBySheet = false },
                                separatedOptions = listOf(GroupByLibrary.NONE),
                                footerContent = {
                                    item { SettingsListHeader("Other") }
                                    item {
                                        SettingListItem(
                                            title = "Global Ranking",
                                            position = ListItemPosition.SINGLE,
                                            trailingContent = {
                                                SettingsSwitch(
                                                    checked = state.globalRank,
                                                    onCheckedChange = viewModel::onGlobalRankChange,
                                                )
                                            }
                                        )
                                    }
                                }
                            )
                        }
                        if (showSortBySheet) {
                            ModalSortableEnumSelector(
                                selectedOption = queryParamsState.sortBy,
                                onOptionSelected = viewModel::updateSortBy,
                                selectedOrder = queryParamsState.sortOrder,
                                onOrderChange = viewModel::updateSortOrder,
                                onDismiss = { showSortBySheet = false },
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                when {
                    state.isLoading -> {
                        item { ScreenLoading() }
                    }
                    state.error != null -> {
                        item { ScreenError(state.error) }
                    }
                    else -> {
                        groupedItems.takeIf { it.isNotEmpty() }?.let {
                            groupedItems.forEach { (group, items) ->
                                if (state.groupByMode != GroupByLibrary.NONE) {
                                    item {
                                        Row(
                                            modifier = Modifier.padding(
                                                start = 42.dp,
                                                end = 10.dp,
                                                top = 24.dp
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Text(
                                                text = group ?: "Unknown",
                                                style = MaterialTheme.typography.displayLarge,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                                fontWeight = FontWeight.Black,
                                            )
                                            if (items.isNotEmpty()) {
                                                val rated = items.filter { it.rating != null }
                                                if (rated.isNotEmpty()) {
                                                    val avg = rated.sumOf {
                                                        it.rating?.toDouble() ?: 0.0
                                                    } / rated.size
                                                    Text(
                                                        modifier = Modifier.padding(bottom = 12.dp),
                                                        text = "(avg. ${getTransformedRating(avg.toFloat())})",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.75f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                itemsIndexed(
                                    items = items,
                                    key = { _, item -> item.externalId!!.toInt() }
                                ) { index, item ->
                                    val rank = if (state.globalRank) sortedItems.indexOf(item) + 1
                                        else index + 1
                                    val rating = item.externalId?.toInt()?.let { ratings[it] }
                                    RateItemCard(
                                        title = item.title,
                                        subtitle = item.subtitle,
                                        overlineText = if (rating?.votes != null) {
                                            "${formatCompact(rating.votes.toLong())} VOTES"
                                        } else null,
                                        coverImagePath = item.coverImageLowUrl,
                                        rating = rating?.rating,
                                        placeholderRatio = 2f / 3f,
                                        padding = PaddingValues(vertical = 4.dp),
                                        rank = rank,
                                        rankHighlighted = item.externalId?.let { state.completedItems.contains(it) } ?: false,
                                        colorBucketsOverride = when (category) {
                                            CategoryType.TMDB_MOVIES -> RatingColorBucketConstants.RC_IMDB_MOVIES
                                            CategoryType.TMDB_SHOWS -> RatingColorBucketConstants.RC_IMDB_SHOWS
                                            else -> getCurrentRatingColorBuckets()
                                        },
                                        onClick = { onItemClick(item.externalId?.toInt() ?: 0) },
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            ExpressiveScrollBar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 42.dp),
                listState = listState,
            )
        }
    }
}