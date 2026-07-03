package com.example.rateio.presentation.rating.openlibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.remote.openlibrary.OpenLibraryClient
import com.example.rateio.data.remote.tmdb.toCarouselImage
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.ItemStatus
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CarouselImage
import com.example.rateio.presentation.components.CollapsibleHeader
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.ItemStatCard
import com.example.rateio.presentation.components.ItemStatusSelector
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.label
import com.example.rateio.presentation.rating.RateItemDetailScreen


@Composable
fun OLWorkDetailScreen(
    workId: String,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onStatusSaved: ((ItemStatus) -> Unit)? = null,
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        state.work != null -> {
            val work = state.work!!
            val author = state.author
            val editionWithContents = if (state.editionsWithContents.isNotEmpty())
                state.editionsWithContents.first()
            else null

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
                                statistic = if (state.numberOfPages != null)
                                    "~${state.numberOfPages.toString()} "
                                else "N/A",
                            )
                            ItemStatCard(
                                header = "Chapters",
                                statistic = editionWithContents?.tableOfContents?.size?.toString() ?: "N/A",
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

                    // Status
                    if (isSaved) {
                        item {
                            val headerName = "Status"
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
                                    FilledTonalButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            openSheet()
                                        },
                                        shapes = ButtonDefaults.shapes(),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            status.label(),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
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
                            if (editionWithContents != null && editionWithContents.tableOfContents != null) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(state.editionsWithContents.size.toString())
                                    state.editionsWithContents.forEach { edition ->
                                        Text("${edition.languages?.first()?.key} - ${edition.tableOfContents?.size}")
                                    }


                                    editionWithContents.tableOfContents.forEachIndexed { index, chapter ->
                                        RateItemCard(
                                            title = when {
                                                !chapter.label.isNullOrBlank() -> chapter.label
                                                //!chapter.title.isNullOrBlank() -> chapter.title
                                                else -> "Chapter ${index + 1}"
                                            },
                                            //titleStyle = MaterialTheme.typography.titleLarge,
                                            subtitle = when {
                                                !chapter.title.isNullOrBlank() -> chapter.title
                                                //!chapter.pageNum.isNullOrBlank() -> chapter.pageNum
                                                else -> null
                                            },
                                            //overlineText = if (topIndex != -1) "RATED #${topIndex + 1}" else null,
                                            coverImagePath = null,
                                            rating = null,
                                            //isLoading = selectedRatings == 0 && episodesState.isLoadingRatings && rating == null,
                                            placeholderRatio = 16f / 9f,
                                            padding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            onClick = { },
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                            else {
                                Text("Nothing")
                            }
                        }
                    }

                }
            )
        }
    }
}