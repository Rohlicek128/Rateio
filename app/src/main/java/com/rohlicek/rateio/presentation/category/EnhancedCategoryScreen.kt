package com.rohlicek.rateio.presentation.category

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeWeightedRating
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.ConnectedButtonsExpressive
import com.rohlicek.rateio.presentation.components.ExpressiveScrollBar
import com.rohlicek.rateio.presentation.components.GroupByButton
import com.rohlicek.rateio.presentation.components.HeroCarousel
import com.rohlicek.rateio.presentation.components.ModalSortableEnumSelector
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.presentation.components.RateItemGridCard
import com.rohlicek.rateio.presentation.components.RowButtonEnumSelector
import com.rohlicek.rateio.presentation.components.SortByButton
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.components.rating.ParentCompletionText
import com.rohlicek.rateio.presentation.components.statistics.RatingsBarChart
import com.rohlicek.rateio.presentation.components.statistics.StatCard
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsListHeader
import com.rohlicek.rateio.presentation.settings.SettingsSwitch
import com.rohlicek.rateio.utils.bottomShadow
import com.rohlicek.rateio.utils.formatDate
import com.rohlicek.rateio.utils.parseDate
import java.util.Locale


@Composable
fun EnhancedCategoryScreen(
    categoryId: Long,
    onItemClick: (RateItem) -> Unit,
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

    val viewModel: LibraryCategoryViewModel = viewModel(
        factory = LibraryCategoryViewModel.factory(categoryId, categoryRepository, itemRepository)
    )
    val state by viewModel.state.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    //val selectedStatuses = remember { ItemStatus.entries.toMutableStateList() }
    var showSortBySheet by remember { mutableStateOf(false) }
    var showGroupBySheet by remember { mutableStateOf(false) }
    val orderInteractionSources = remember(2) {
        List(2) { MutableInteractionSource() }
    }

    val watchlistItems = remember(state.items) {
        state.items.filter { it.status == ItemStatus.WATCHLIST }
    }

    // Group items
    val inProgressItems = remember(state.items) {
        state.items
            .filter { it.status == ItemStatus.IN_PROGRESS || it.status == ItemStatus.WATCHLIST }
            .sortedByDescending { it.updatedAt }
            .sortedByDescending { it.status == ItemStatus.IN_PROGRESS }
    }

    val isAggregate = state.category?.type != null && state.category?.type == CategoryType.TMDB_SHOWS

    // Filter and sort the main list based on user selection
    val filteredAndSortedItems = remember(state.items, settingsState.statusFilter, settingsState.sortMode, settingsState.sortOrder) {
        /*val filtered = if (selectedStatuses.isEmpty()) {
            state.items.filter { it.status == ItemStatus.NONE }
        } else {
            state.items.filter { it.status in selectedStatuses }
        }*/
        val filtered = if (settingsState.statusFilter == null) {
            state.items
        } else {
            state.items.filter { it.status == settingsState.statusFilter }
        }

        val sorted = when (settingsState.sortMode) {
            SortModeLibrary.NAME -> filtered.sortedBy { it.title }
            SortModeLibrary.RATING -> filtered.sortedBy {
                (if (isAggregate) {
                    computeWeightedRating(it.rating, it.ratingWeight.toInt())
                } else it.rating) ?: (if (settingsState.sortOrder == SortOrder.ASCENDING) 2f else -1f)
            }
            SortModeLibrary.UPDATED -> filtered.sortedBy { it.updatedAt }
            SortModeLibrary.CREATED -> filtered.sortedBy { it.createdAt }
        }

        if (settingsState.sortOrder == SortOrder.ASCENDING) sorted else sorted.asReversed()
    }

    val groupedItems: Map<String?, List<RateItem>> = remember(filteredAndSortedItems, settingsState.statusFilter, settingsState.groupByMode, settingsState.groupByOrder) {
        when (settingsState.groupByMode) {
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
                val finalComparator = if (settingsState.groupByOrder == SortOrder.DESCENDING) {
                    yearComparator.reversed()
                } else yearComparator

                filteredAndSortedItems
                    .groupBy { it.subtitle }
                    .toSortedMap(finalComparator)
            }
            else -> mapOf(null to filteredAndSortedItems)
        }
    }

    /*LaunchedEffect(filteredAndSortedItems) {
        filteredAndSortedItems.filter { it.rating != null }.forEachIndexed { index, show ->
            println("${index + 1}. ${show.title} (${show.subtitle ?: "????"}) - ${ "%.3f".format(Locale.US, show.rating?.times(10f))
                //computeWeightedRating(show.rating, show.ratingWeight.toInt())?.let { 
                //    "%.3f".format(Locale.US, it * 10f)
                //}
            }") //(${show.ratingWeight.toInt()}/${show.length?.toInt()} rated)")
        }
    }*/

    ScreenScaffold(
        title = state.category?.name ?: "",
        onBackClick = onBackClick,
    ) { padding, listState ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = 0.dp, end = 24.dp),
                    //.clip(MaterialTheme.shapes.largeIncreased),
                contentPadding = PaddingValues(bottom = 80.dp),
                state = listState,
            ) {
                //if (state.category?.type == CategoryType.TMDB_MOVIES) {
                //    viewModel.editAll()
                //}
                // --- SECTION 1: IN PROGRESS CAROUSEL ---
                if (inProgressItems.isNotEmpty()) {
                    item {
                        /*Text(
                            text = "Currently Watching/Playing",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )*/

                        if (state.category?.type != null &&
                            state.category?.type == CategoryType.TMDB_SHOWS || state.category?.type == CategoryType.TMDB_MOVIES) {
                            HeroCarousel(
                                padding = PaddingValues(top = 12.dp, bottom = 8.dp, start = 16.dp),
                                //preferredItemWidth = 266.dp,
                                //itemHeight = 400.dp,
                                items = inProgressItems,
                                subtitleBuilder = {
                                    if (state.category?.type == CategoryType.TMDB_SHOWS && it.length != null && it.ratingWeight > 0f) {
                                        "${it.subtitle}  |  ${it.ratingWeight.toInt()}/${it.length.toInt()} Rated"
                                    }
                                    else it.subtitle
                                },
                                customRatingTransform = {
                                    if (isAggregate) {
                                        computeWeightedRating(it.rating, it.ratingWeight.toInt())
                                    } else it.rating
                                },
                                isLoading = state.isLoading,
                                //autoScroll = true,
                                dotIndicator = true,
                                showNullRating = false,
                                colorBucketsOverride = if (isAggregate) RatingColorBucketConstants.RC_IMDB_SHOWS else getCurrentRatingColorBuckets(),
                                onItemClick = { onItemClick(it) },
                            )
                        }
                        else {
                            LazyRow (
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(inProgressItems) { item ->
                                    RateItemGridCard(
                                        title = item.title,
                                        subtitle = item.subtitle,
                                        coverImagePath = item.coverImageOverride ?: item.coverImageLowUrl,
                                        rating = if (isAggregate) {
                                            computeWeightedRating(item.rating, item.ratingWeight.toInt())
                                        } else item.rating,
                                        placeholderRatio = 2f / 3f,
                                        padding = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
                                        showNullRatings = item.status != ItemStatus.WATCHLIST,
                                        colorBucketsOverride = if (isAggregate) RatingColorBucketConstants.RC_IMDB_SHOWS else getCurrentRatingColorBuckets(),
                                        onClick = { onItemClick(item) },
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(start = 16.dp, end = 10.dp, top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Added",
                            value = if (state.items.isNotEmpty()) state.items.size.toString() else "--",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )

                        StatCard(
                            title = settingsState.statusFilter?.displayName ?: "Watchlist",
                            value = if (settingsState.statusFilter != null && filteredAndSortedItems.isNotEmpty())
                                filteredAndSortedItems.size.toString()
                            else if (watchlistItems.isNotEmpty())
                                watchlistItems.size.toString()
                            else "--",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                item {
                    RatingsBarChart(
                        modifier = Modifier.padding(16.dp),
                        chartHeight = 180.dp,
                        entries = filteredAndSortedItems,
                        title = state.category?.type?.displayName ?: "Library",
                        type = settingsState.chartType,
                        onTypeSelect = viewModel::onChartTypeSelect,
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(MaterialTheme.shapes.large),
                        thickness = 2.dp,
                    )
                }

                // --- SECTION 2: FILTERS AND SORTING ---
                /*item {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, end = 10.dp, top = 6.dp),
                        text = state.category?.type?.displayName ?: "Library",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                    )
                }*/
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
                                selectedOption = settingsState.groupByMode,
                                onOptionSelected = {
                                    if (it == settingsState.groupByMode) viewModel.onGroupByModeSelect(GroupByLibrary.NONE)
                                    else viewModel.onGroupByModeSelect(it)
                                },
                                selectedOrder = settingsState.groupByOrder,
                                onOrderChange = viewModel::onGroupByOrderChange,
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
                                                    checked = settingsState.globalRank,
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
                                selectedOption = settingsState.sortMode,
                                onOptionSelected = viewModel::onSortModeSelect,
                                selectedOrder = settingsState.sortOrder,
                                onOrderChange = viewModel::onSortOrderChange,
                                onDismiss = { showSortBySheet = false },
                            )
                        }
                    }
                }
                item {
                    RowButtonEnumSelector(
                        contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        selectedOption = settingsState.statusFilter,
                        onOptionSelected = viewModel::onStatusFilterSelect,
                        excludedOptions = listOf(ItemStatus.NONE),
                        nullIsAll = true,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .bottomShadow(16.dp)
                    )
                }

                // --- SECTION 3: THE MAIN LIST ---
                groupedItems.takeIf { it.isNotEmpty() }?.let {
                    groupedItems.forEach { (group, items) ->
                        if (settingsState.groupByMode != GroupByLibrary.NONE) {
                            item {
                                Row(
                                    modifier = Modifier.padding(start = 42.dp, end = 10.dp, top = 24.dp),
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
                                            val avg = rated.sumOf { it.rating?.toDouble() ?: 0.0 } / rated.size
                                            Text(
                                                modifier = Modifier.padding(bottom = 12.dp),
                                                text = "(avg. ${getTransformedRating(avg.toFloat())})",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        itemsIndexed(
                            items = items,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            val rank = if (settingsState.sortMode == SortModeLibrary.RATING && item.rating != null) {
                                if (settingsState.globalRank) filteredAndSortedItems.indexOf(item) + 1
                                else index + 1
                            }
                            else null
                            RateItemCard(
                                title = item.title,
                                subtitle = when (settingsState.sortMode) {
                                    SortModeLibrary.UPDATED -> formatDate(parseDate(item.updatedAt))
                                    SortModeLibrary.CREATED -> formatDate(parseDate(item.createdAt))
                                    else -> item.subtitle
                                    //else -> when (settingsState.groupByMode) {
                                    //    GroupByLibrary.YEAR -> formatDate(parseDate(item.createdAt))
                                    //    else -> item.subtitle
                                    //}
                                },
                                coverImagePath = item.coverImageOverride ?: item.coverImageLowUrl,
                                rating = if (isAggregate) {
                                    computeWeightedRating(item.rating, item.ratingWeight.toInt())
                                } else item.rating,
                                placeholderRatio = 2f / 3f,
                                padding = PaddingValues(start = 18.dp, top = 6.dp, bottom = 6.dp),
                                rank = rank,
                                onClick = { onItemClick(item) },
                                //bubbleText = if (item.externalSource == CategoryType.TMDB_MOVIES && item.length != null)
                                //    formatTime(item.length.toInt())
                                //else null,
                                colorBucketsOverride = if (isAggregate) RatingColorBucketConstants.RC_IMDB_SHOWS else getCurrentRatingColorBuckets(),
                                leadingRateBoxContent = if (isAggregate) {
                                    {
                                        ParentCompletionText(
                                            numberOfCompleted = item.ratingWeight.toInt(),
                                            numberOfAll = item.length?.toInt() ?: 0,
                                        )
                                    }
                                } else null,
                            )
                        }
                    }
                }

                // --- SECTION 4: EMPTY STATE ---
                if (groupedItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items found in this category.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            ExpressiveScrollBar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 32.dp),
                listState = listState,
            )
        }

    }
}