package com.example.rateio.presentation.rating.tmdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.rateio.data.remote.tmdb.toCarouselImage
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.ItemStatus
import com.example.rateio.presentation.category.ItemListRow
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CollapsibleHeader
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.ItemStatCard
import com.example.rateio.presentation.components.ItemStatusSelector
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.ReviewCard
import com.example.rateio.presentation.components.ScreenError
import com.example.rateio.presentation.components.ScreenLoading
import com.example.rateio.presentation.components.label
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.presentation.rating.display.RatingColorBucketConstants
import com.example.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.example.rateio.utils.formatCompact
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.formatTime
import java.util.Locale


@Composable
fun TmdbMovieDetailScreen(
    movieId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onStatusSaved: ((ItemStatus) -> Unit)? = null,
    onPersonClick: ((Int) -> Unit)? = null,
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

    val viewModel: TmdbMovieDetailViewModel = viewModel(
        factory = TmdbMovieDetailViewModel.factory(movieId, categoryRepository, itemRepository)
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
        state.movie != null -> {
            val movie = state.movie!!

            var status by remember(state.savedItem?.status) { mutableStateOf(
                if (state.savedItem == null) ItemStatus.WATCHLIST
                else state.savedItem!!.status
            ) }

            RateItemDetailScreen(
                title = movie.title,
                subtitle = buildString {
                    append(formatDate(movie.releaseDate))
                    if (movie.status != null && movie.status != "Released") append("  |  ${movie.status}")
                }.ifBlank { null },
                categoryName = CategoryRegistry.forType(CategoryType.TMDB_MOVIES)?.name,
                description = movie.overview,
                coverImageUrl = movie.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = movie.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = if (!isSaved) state.imdbRating?.normalizedRating else customRating,
                ratingVotes = if (!isSaved) state.imdbRating?.voteCount else null,
                ratingLabel = state.imdbRating?.normalizedRating?.let { "%.1f on IMDb".format(Locale.US, it * 10f) },
                ratingColorBucketsOverride = if (!isSaved) RatingColorBucketConstants.RC_IMDB_MOVIES else getCurrentRatingColorBuckets(),
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
                                header = "Runtime",
                                statistic = formatTime(movie.runtime),
                            )
                            if (movie.revenue > 0) {
                                ItemStatCard(
                                    header = "Revenue",
                                    statistic = formatCompact(
                                        movie.revenue,
                                        decimalsMillion = 0,
                                    ),
                                )
                            }
                            ItemStatCard(
                                header = "Popularity",
                                statistic = "%.1f".format(Locale.US, movie.popularity),
                            )
                        }
                    }
                },
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LibraryToggle(
                                checked = state.savedItem != null,
                                onCheckedChange = {
                                    viewModel.onToggleSaved(state.movie!!)
                                },
                                itemName = movie.title,
                            )
                        }
                    }


                    // Genres
                    if (movie.genres.isNotEmpty()) {
                        item {
                            GenreChips(
                                genres = movie.genres.map { it.name },
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

                    // Crew
                    movie.credits?.crew?.takeIf { it.isNotEmpty() }?.let { crew ->
                        item {
                            val headerName = "Crew"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                LazyRow(
                                    modifier = Modifier.height(150.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(crew.sortedByDescending { it.popularity }.take(10), key = { it.creditId }) { member ->
                                        PersonCard(
                                            name = member.name,
                                            position = member.job,
                                            profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                            width = 80.dp,
                                            height = 100.dp,
                                            onClick = {
                                                onPersonClick?.invoke(member.id)
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cast
                    if (movie.credits != null && movie.credits.cast.isNotEmpty()) {
                        item {
                            val headerName = "Cast"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                LazyRow(
                                    modifier = Modifier.height(200.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    movie.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
                                        items(cast.take(15), key = { it.creditId }) { member ->
                                            PersonCard(
                                                name = member.name,
                                                position = member.character,
                                                profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                                onClick = {
                                                    onPersonClick?.invoke(member.id)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //Images
                    state.images?.posters?.takeIf { it.isNotEmpty() }?.let { images ->
                        item {
                            val headerName = "Posters"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                AdaptiveImageCarousel(
                                    baseUrl = "https://image.tmdb.org/t/p/w500",
                                    images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                    itemWidth = 110.dp,
                                    itemHeight = 180.dp,
                                    shape = MaterialTheme.shapes.large,
                                )
                            }
                        }
                    }
                    state.images?.backdrops?.takeIf { it.isNotEmpty() }?.let { images ->
                        item {
                            val headerName = "Backdrops"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                AdaptiveImageCarousel(
                                    baseUrl = "https://image.tmdb.org/t/p/w780",
                                    images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                    itemWidth = 240.dp,
                                    shape = MaterialTheme.shapes.large,
                                )
                            }
                        }
                    }

                    // Reviews
                    state.reviews?.results?.takeIf { it.isNotEmpty() }?.let { reviews ->
                        item {
                            val headerName = "Reviews"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(reviews, key = { it.id }) { review ->
                                        ReviewCard(
                                            modifier = Modifier.size(width = 320.dp, height = 190.dp),
                                            name = review.author,
                                            supportingText = formatDate(review.updatedAt),
                                            avatarPath = "https://image.tmdb.org/t/p/w92${review.authorDetails?.avatarPath}",
                                            rating = review.authorDetails?.rating?.div(10f),
                                            content = review.content,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Recommendations
                    state.recommendations.takeIf { it.isNotEmpty() }?.let { recommendations ->
                        item {
                            val headerName = "Recommendations"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                ItemListRow(
                                    title = "",
                                    items = recommendations,
                                    showRanking = false,
                                    showNullRatings = true,
                                    isLoading = false,
                                    onItemClick = { item ->
                                        item.externalId?.let { id ->
                                            item.externalSource?.let { type ->

                                            }
                                        }
                                    },
                                    colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_MOVIES,
                                )
                            }
                        }
                    }

                }
            )
        }
    }
}