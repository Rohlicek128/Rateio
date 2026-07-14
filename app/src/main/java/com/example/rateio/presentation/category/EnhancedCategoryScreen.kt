package com.example.rateio.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.ItemStatus
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.ScreenScaffold
import com.example.rateio.presentation.components.ExpressiveScrollBar
import com.example.rateio.presentation.components.ModalSortableEnumSelector
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.RateItemGridCard
import com.example.rateio.presentation.components.SortByButton
import com.example.rateio.presentation.components.SortOrder
import com.example.rateio.presentation.rating.tmdb.SortModeShow
import com.example.rateio.ui.theme.GoogleSans
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.parseDate


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

    var selectedStatus by remember { mutableStateOf<ItemStatus?>(null) } // null means "All"
    var showSortBySheet by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(SortModeLibrary.RATING) }
    var currentOrder by remember { mutableStateOf(SortOrder.DESCENDING) }

    // Group items
    val inProgressItems = remember(state.items) {
        state.items
            .filter { it.status == ItemStatus.IN_PROGRESS || it.status == ItemStatus.WATCHLIST }
            .sortedByDescending { it.updatedAt }
            .sortedByDescending { it.status }
    }

    // Filter and sort the main list based on user selection
    val filteredAndSortedItems = remember(state.items, selectedStatus, currentSort, currentOrder) {
        val filtered = if (selectedStatus == null) {
            state.items
        } else {
            state.items.filter { it.status == selectedStatus }
        }

        val sorted = when (currentSort) {
            SortModeLibrary.NAME -> filtered.sortedBy { it.title }
            SortModeLibrary.RATING -> filtered.sortedBy { it.rating ?: (if (currentOrder == SortOrder.ASCENDING) 2f else -1f) }
            SortModeLibrary.UPDATED -> filtered.sortedBy { it.updatedAt }
            SortModeLibrary.CREATED -> filtered.sortedBy { it.createdAt }
        }

        if (currentOrder == SortOrder.ASCENDING) sorted else sorted.asReversed()
    }

    val listState = rememberLazyListState()

    ScreenScaffold(
        title = state.category?.name ?: "",
        onBackClick = onBackClick,
        actions = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = null,
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 16.dp)
                    .widthIn(min = 58.dp)
            ) {
                Text(
                    text = state.items.size.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans,
                    maxLines = 1,
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = 0.dp, end = 24.dp)
                    .clip(MaterialTheme.shapes.largeIncreased),
                contentPadding = PaddingValues(bottom = 80.dp),
                state = listState,
            ) {
                //if (state.category?.type == CategoryType.TMDB_MOVIES) {
                //    viewModel.editAll()
                //}
                // --- SECTION 1: IN PROGRESS CAROUSEL ---
                if (inProgressItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Currently Watching/Playing",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        LazyRow (
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(inProgressItems) { item ->
                                RateItemGridCard(
                                    title = item.title,
                                    subtitle = item.subtitle,
                                    coverImagePath = item.coverImageOverride ?: item.coverImageLowUrl,
                                    rating = item.rating,
                                    placeholderRatio = 2f / 3f,
                                    padding = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
                                    showNullRatings = item.status != ItemStatus.WATCHLIST,
                                    onClick = { onItemClick(item) },
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(16.dp))
                    }
                }

                // --- SECTION 2: FILTERS AND SORTING ---
                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Library",
                            style = MaterialTheme.typography.titleLarge
                        )

                        SortByButton(onClick = { showSortBySheet = true })
                        if (showSortBySheet) {
                            ModalSortableEnumSelector(
                                selectedOption = currentSort,
                                onOptionSelected = { currentSort = it },
                                selectedOrder = currentOrder,
                                onOrderChange = { currentOrder = it },
                                onDismiss = { showSortBySheet = false },
                            )
                        }
                    }

                    // Filter Chips for Statuses
                    ScrollableTabRow(
                        selectedTabIndex = if (selectedStatus == null) 0 else ItemStatus.entries.indexOf(selectedStatus) + 1,
                        edgePadding = 16.dp,
                        divider = {},
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        // "All" Tab
                        Tab(
                            selected = selectedStatus == null,
                            onClick = { selectedStatus = null },
                            text = { Text("All") }
                        )
                        ItemStatus.entries.forEach { status ->
                            Tab(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                text = { Text(status.displayName) }
                            )
                        }
                    }
                }

                // --- SECTION 3: THE MAIN LIST ---
                itemsIndexed(
                    items = filteredAndSortedItems,
                    key = { _, item -> item.id }
                ) { index, item ->
                    RateItemCard(
                        title = item.title,
                        subtitle = when (currentSort) {
                            SortModeLibrary.UPDATED -> formatDate(parseDate(item.updatedAt))
                            SortModeLibrary.CREATED -> formatDate(parseDate(item.createdAt))
                            else -> item.subtitle
                        },
                        coverImagePath = item.coverImageOverride ?: item.coverImageLowUrl,
                        rating = item.rating,
                        placeholderRatio = 2f / 3f,
                        padding = PaddingValues(start = 18.dp, top = 6.dp, bottom = 6.dp),
                        rank = if (currentSort == SortModeLibrary.RATING) index + 1 else null,
                        onClick = { onItemClick(item) },
                    )
                }

                // --- SECTION 4: EMPTY STATE ---
                if (filteredAndSortedItems.isEmpty()) {
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