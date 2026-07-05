package com.example.rateio.presentation.rating.openlibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.remote.openlibrary.OLTableContent
import com.example.rateio.data.remote.openlibrary.OLWorkMetadata
import com.example.rateio.data.remote.openlibrary.OpenLibraryClient
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.ItemStatus
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CarouselImage
import com.example.rateio.presentation.components.CollapsibleHeader
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.ItemStatCard
import com.example.rateio.presentation.components.ItemStatusSelector
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.ScreenError
import com.example.rateio.presentation.components.ScreenLoading
import com.example.rateio.presentation.components.rating.ChildrenDisplay
import com.example.rateio.presentation.components.rating.ItemProgressBar
import com.example.rateio.presentation.rating.RateItemDetailScreen
import kotlinx.serialization.json.Json


@Composable
fun OLWorkDetailScreen(
    workId: String,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onStatusSaved: ((ItemStatus) -> Unit)? = null,
    onMetadataSaved: ((String?) -> Unit)? = null,
    onBackClick: () -> Unit,
    onChapterClick: (partItem: RateItem, chapterItem: RateItem) -> Unit,
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

    val viewModel: OLWorkDetailViewModel = viewModel(
        factory = OLWorkDetailViewModel.factory(
            workId.removePrefix("/works/"),
            categoryRepository,
            itemRepository
        )
    )
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current

    when {
        state.isLoading -> {
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
        }
        state.work != null -> {
            val work = state.work!!
            val author = state.author
            val editionWithContents = if (state.editionsWithContents.isNotEmpty())
                state.editionsWithContents.first()
            else null
            val metadata = state.savedItem?.metadataJSON?.let {
                runCatching {
                    Json.decodeFromString<OLWorkMetadata>(it)
                }.getOrNull()
            }

            val tableOfContents = when {
                metadata != null && metadata.numberOfChaptersByParts != null ->
                    generateTableOfContent(metadata.numberOfChaptersByParts)
                editionWithContents?.tableOfContents != null ->
                    editionWithContents.tableOfContents
                metadata != null && metadata.numberOfChaptersByPartsAPI != null ->
                    generateTableOfContent(metadata.numberOfChaptersByPartsAPI)
                else ->
                    emptyList()
            }
            val numberOfPages = metadata?.numberOfPages ?: state.numberOfPages

            val userRatings = viewModel.userRatingsState.collectAsStateWithLifecycle()

            var keyIndex = 0
            var groupIndex = 0
            val chaptersGroups = groupChaptersByLowestLevel(tableOfContents)
                .mapValues {
                    keyIndex++
                    it.value.mapIndexed { index, chapter ->
                        val chapterItem = userRatings.value["${workId}-P${keyIndex}"]?.get("${workId}-P${keyIndex}-C${index}")
                        RateItem(
                            id = chapterItem?.id ?: 0,
                            parentId = keyIndex.toLong(),
                            categoryId = state.savedItem?.categoryId ?: 0,
                            title = when {
                                chapterItem != null -> chapterItem.title
                                !chapter.label.isNullOrBlank() -> chapter.label
                                //!chapter.title.isNullOrBlank() -> chapter.title
                                else -> "Chapter ${index + 1}"
                            },
                            subtitle = when {
                                chapterItem != null -> chapterItem.subtitle
                                !chapter.title.isNullOrBlank() -> chapter.title
                                //!chapter.pageNum.isNullOrBlank() -> chapter.pageNum
                                else -> null
                            },
                            coverImageUrl = chapterItem?.coverImageUrl,
                            coverImageLowUrl = chapterItem?.coverImageLowUrl,
                            //rating = Random.nextFloat() * 0.25f + 0.75f,
                            rating = chapterItem?.rating,
                            externalId = "${workId}-P${keyIndex}-C${index}",
                            externalSource = CategoryType.OPEN_LIBRARY_CHAPTER,
                        )
                    }
                }
                .mapKeys { group ->
                    groupIndex++
                    if (group.key != null) {
                        val groupKey = group.key!!
                        RateItem(
                            id = 0,
                            parentId = state.savedItem?.id,
                            categoryId = state.savedItem?.categoryId ?: 0,
                            title = when {
                                !groupKey.title.isNullOrBlank() -> groupKey.title
                                !groupKey.label.isNullOrBlank() -> groupKey.label
                                else -> "Part $groupIndex"
                            },
                            subtitle = "${group.value.size} chapters",
                            coverImageUrl = null,
                            rating = null,
                            externalId = "${workId}-P${groupIndex}",
                            externalSource = CategoryType.OPEN_LIBRARY_PART,
                        )
                    }
                    else {
                        val groupItem: RateItem? = RateItem(
                            id = 0,
                            parentId = state.savedItem?.id,
                            categoryId = state.savedItem?.categoryId ?: 0,
                            title = work.title ?: "Unknown",
                            subtitle = "${group.value.size} chapters",
                            coverImageUrl = work.covers?.takeIf { it.isNotEmpty() }?.let {
                                OpenLibraryClient.COVERS_BASE_URL + "/ID/${it.first()}-M.jpg"
                            },
                            rating = null,
                            externalId = "${workId}-P${groupIndex}",
                            externalSource = CategoryType.OPEN_LIBRARY_PART,
                        )
                        groupItem
                    }
                }

            val numOfChapters = if (editionWithContents?.tableOfContents != null &&
                (metadata == null || metadata.numberOfChaptersByPartsAPI == null)) {
                chaptersGroups.values.map { it.size }
            } else null
            val numOfPages = if (metadata != null && metadata.numberOfPages == null) {
                numberOfPages
            } else null
            if (numOfChapters != null || numberOfPages != null) {
                onMetadataSaved?.invoke(Json.encodeToString(
                    (metadata ?: OLWorkMetadata()).copy(
                        numberOfChaptersByPartsAPI = numOfChapters,
                        numberOfPages = numOfPages,
                    )
                ))
            }


            val onChildClick = { child: RateItem ->
                if (child.externalId != null) {
                    val ids = child.externalId.split("-")
                    val parentId = "${ids[0]}-${ids[1]}"
                    val parent = chaptersGroups.keys.find { it?.externalId == parentId }
                    if (parent != null) {
                        onChapterClick(parent, child)
                    }
                }
            }

            var status by remember(state.savedItem?.status) { mutableStateOf(
                if (state.savedItem == null) ItemStatus.WATCHLIST
                else state.savedItem!!.status
            ) }

            RateItemDetailScreen(
                title = work.title ?: "N/A",
                subtitle = if (author?.name != null) "by ${author.name}" else null,
                categoryName = CategoryRegistry.forType(CategoryType.OPEN_LIBRARY_BOOKS)?.name,
                description = work.description?.value,
                coverImageUrl = work.covers?.takeIf { it.isNotEmpty() }?.let {
                    OpenLibraryClient.COVERS_BASE_URL + "/ID/${it.first()}-L.jpg"
                },
                backdropImageUrl = work.covers?.takeIf { it.isNotEmpty() }?.let {
                    OpenLibraryClient.COVERS_BASE_URL + "/ID/${it.first()}-L.jpg"
                },
                showNullRating = isSaved,
                rating = if (isSaved) customRating else null,
                ratingVotes = null,
                onRatingSaved = onRatingSaved,
                onBackClick = onBackClick,
                canAddToLibrary = false,
                headerExtraContent = {
                    //Stats
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            ItemStatCard(
                                header = "pages",
                                statistic = if (numberOfPages != null) "~${numberOfPages} " else "N/A",
                            )
                            ItemStatCard(
                                header = "Chapters",
                                statistic = if (tableOfContents.isNotEmpty()) tableOfContents.size.toString() else "N/A",
                            )
                        }
                    }
                },
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LibraryToggle(
                                checked = state.savedItem != null,
                                onCheckedChange = {
                                    viewModel.onToggleSaved(work, author)
                                },
                                itemName = work.title ?: "Unknown Name",
                            )
                        }
                    }


                    // Genres
                    if (!work.subjects.isNullOrEmpty()) {
                        item {
                            GenreChips(
                                genres = work.subjects.take(6),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // Progress Bar
                    if ((isSaved || userRatings.value.isNotEmpty()) && numberOfPages != null) {
                        item {
                            val headerName = "Progress"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                ItemStatusSelector(
                                    selected = status,
                                    onStatusSelected = {
                                        status = it
                                        onStatusSaved?.invoke(it)
                                    }
                                ) { openSheet ->
                                    val completed = metadata?.completedPages ?: 0
                                    val remaining = numberOfPages - completed
                                    ItemProgressBar(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 10.dp
                                        ),
                                        endString = "${completed}/${numberOfPages} pages",
                                        endValue = numberOfPages.toFloat(),
                                        currentString = "Remaining $remaining page${if (remaining == 1) "" else "s"}",
                                        currentValue = completed.toFloat(),
                                        status = status,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            openSheet()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    //Images
                    work.covers?.takeIf { it.isNotEmpty() }?.let { images ->
                        item {
                            val headerName = "Covers"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                AdaptiveImageCarousel(
                                    baseUrl = OpenLibraryClient.COVERS_BASE_URL,
                                    images.map { CarouselImage(
                                        filePath = "/ID/${it}-M.jpg",
                                        aspectRatio = 2f / 3f,
                                    ) },
                                    itemWidth = 110.dp,
                                    itemHeight = 180.dp,
                                    shape = MaterialTheme.shapes.large,
                                )
                            }
                        }
                    }

                    item {
                        val headerName = "Chapters"
                        CollapsibleHeader(
                            headerName,
                            isOpened = headerName !in state.collapsedHeaders,
                            onClick = {
                                if (it) state.collapsedHeaders.remove(headerName)
                                else state.collapsedHeaders.add(headerName)
                            }
                        ) {
                            ChildrenDisplay(
                                childrenGroups = chaptersGroups,
                                onChildClick = onChildClick,
                                rowText = { "C$it" },
                                columnText = { "P$it" },
                                selectedDisplayMode = state.selectedDisplayMode,
                                onDisplayModeSelect = viewModel::onDisplayModeSelect,
                                selectedSortMode = state.selectedSortMode,
                                onSortModeSelect = viewModel::onSortModeSelect,
                                expandedParents = state.expandedChapters,
                            )
                        }
                    }

                }
            )
        }
    }
}

private fun generateTableOfContent(structure: List<Int>): List<OLTableContent> {
    val result = mutableListOf<OLTableContent>()
    structure.forEachIndexed { index, chapters ->
        if (structure.size > 1) {
            result.add(OLTableContent(
                0, "Part ${index + 1}", null, null)
            )
        }
        for (i in 1..chapters) {
            result.add(OLTableContent(
                1, "Chapter $i", null, null)
            )
        }
    }
    return result
}

private fun groupChaptersByLowestLevel(items: List<OLTableContent>): Map<OLTableContent?, List<OLTableContent>> {
    if (items.isEmpty()) return emptyMap()

    val minLevel = items.minOf { it.level ?: 0 }

    // Edge Case 1: If all items have the same level, do not group.
    if (items.all { it.level == minLevel }) {
        return mapOf(null to items)
    }

    // Use LinkedHashMap to preserve the "unraveled" chronological order
    val resultMap = LinkedHashMap<OLTableContent?, List<OLTableContent>>()

    var currentParent: OLTableContent? = null
    var currentChildren = mutableListOf<OLTableContent>()

    for (item in items) {
        if (item.level == minLevel) {
            if (currentParent != null) {
                resultMap[currentParent] = currentChildren
            }

            currentParent = item
            currentChildren = mutableListOf()
        } else {
            currentChildren.add(item)
        }
    }

    resultMap[currentParent] = currentChildren

    return resultMap
}