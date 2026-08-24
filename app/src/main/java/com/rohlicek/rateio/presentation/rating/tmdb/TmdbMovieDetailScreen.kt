package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.toCarouselImage
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.presentation.category.ItemListRow
import com.rohlicek.rateio.presentation.components.AdaptiveImageCarousel
import com.rohlicek.rateio.presentation.components.CollapsibleHeader
import com.rohlicek.rateio.presentation.components.CrewPersonRow
import com.rohlicek.rateio.presentation.components.GenreChips
import com.rohlicek.rateio.presentation.components.ImageSize
import com.rohlicek.rateio.presentation.components.statistics.ExternalRatingStatCard
import com.rohlicek.rateio.presentation.components.statistics.ItemStatCard
import com.rohlicek.rateio.presentation.components.ModalEnumSelector
import com.rohlicek.rateio.presentation.components.PersonCard
import com.rohlicek.rateio.presentation.components.ReviewCard
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.rating.RateItemDetailScreen
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.RatingTransformationsConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.ModalSettings
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsTextField
import com.rohlicek.rateio.utils.formatCompact
import com.rohlicek.rateio.utils.formatDate
import com.rohlicek.rateio.utils.formatItemRankLabel
import com.rohlicek.rateio.utils.formatTime
import com.rohlicek.rateio.utils.openExternalLink
import com.rohlicek.rateio.utils.parseDate
import java.util.Locale


@Composable
fun TmdbMovieDetailScreen(
    movieId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    savedRank: Int? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onStatusSaved: ((ItemStatus) -> Unit)? = null,
    onCoverOverrideSaved: ((String?) -> Unit)? = null,
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
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: TmdbMovieDetailViewModel = viewModel(
        factory = TmdbMovieDetailViewModel.factory(movieId, categoryRepository, itemRepository, imdbRepository)
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

            var showStatusSelector by remember { mutableStateOf(false) }

            // Settings
            var coverOverride by remember(state.savedItem) { mutableStateOf(state.savedItem?.coverImageOverride) }

            val crewByDepartment = remember(movie) {
                movie.credits?.crew?.takeIf { it.isNotEmpty() }?.sortedByDescending { it.popularity }?.groupBy { it.department }
            }

            var showSettings by remember { mutableStateOf(false) }
            if (showSettings) {
                ModalSettings(
                    title = "${movie.title}'s Settings",
                    onDismiss = { showSettings = false }
                ) {
                    item {
                        SettingListItem(
                            title = "Cover Image Override",
                            description = "Override the cover image url that will be displayed (ideally 2:3 ratio)",
                            position = ListItemPosition.SINGLE,
                            supportingContent = {
                                SettingsTextField(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    value = coverOverride ?: "",
                                    onValueChange = { value ->
                                        coverOverride = value
                                        onCoverOverrideSaved?.invoke(value)
                                    },
                                    singleLine = false,
                                    placeholder = { Text("eg. https://example.org/image.jpg") },
                                    onReset = {
                                        coverOverride = null
                                        onCoverOverrideSaved?.invoke(null)
                                    }
                                )
                            }
                        )
                    }
                }
            }

            RateItemDetailScreen(
                title = movie.title,
                subtitle = buildString {
                    append(formatDate(movie.releaseDate))
                    if (movie.status != null && movie.status != "Released") append("  |  ${movie.status}")
                }.ifBlank { null },
                categoryName = CategoryRegistry.forType(CategoryType.TMDB_MOVIES)?.name,
                description = movie.overview,
                coverImageUrl = (if (!isSaved) null else coverOverride) ?: movie.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = movie.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = if (!isSaved) state.imdbRating?.averageRating else customRating,
                ratingVotes = if (!isSaved) state.imdbRating?.numVotes else null,
                ratingLabel = savedRank?.let { formatItemRankLabel(it, CategoryType.TMDB_MOVIES) },
                ratingColorBucketsOverride = if (!isSaved) RatingColorBucketConstants.RC_IMDB_MOVIES else getCurrentRatingColorBuckets(),
                onRatingSaved = onRatingSaved,
                onRatingClick = {
                    openExternalLink(
                        context,
                        url = "https://www.imdb.com/title/${movie.imdbId}"
                    )
                }.takeIf { !isSaved && movie.imdbId != null },
                //review = if (state.savedItem != null) "" else null,
                onBackClick = onBackClick,
                onOpenSettings = if (isSaved) {
                    { showSettings = true }
                } else null,
                savedInLibrary = state.savedItem != null,
                onChangeLibrary = { viewModel.onToggleSaved(state.movie!!) },
                //debug = "${state.savedItem?.id}, ${state.savedItem?.parentId}, ${state.savedItem?.categoryId}, ${state.savedItem?.externalId}",
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

                    // Ratings
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            if (isSaved) {
                                ExternalRatingStatCard(
                                    rating = state.imdbRating?.averageRating,
                                    votes = state.imdbRating?.numVotes,
                                    source = "IMDb",
                                    transformationOverride = RatingTransformationsConstants.TF_IMDB,
                                    colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_MOVIES,
                                    onClickUrl = movie.imdbId?.let { "https://www.imdb.com/title/$it" },
                                )
                            }
                            ExternalRatingStatCard(
                                rating = movie.voteAverage?.div(10f),
                                votes = movie.voteCount,
                                source = "TMDB",
                                transformationOverride = RatingTransformationsConstants.TF_PERCENTAGE,
                                colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_MOVIES,
                                onClickUrl = "https://www.themoviedb.org/movie/${movie.id}",
                            )
                            if (!isSaved) {
                                ExternalRatingStatCard(
                                    rating = state.savedItem?.rating,
                                    votes = null,
                                    source = "Yours",
                                    showNullVotes = false,
                                )
                            }
                        }
                    }

                    // Genres
                    if (movie.genres.isNotEmpty()) {
                        item {
                            GenreChips(
                                genres = movie.genres.map { it.name },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
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
                                FilledTonalButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                        showStatusSelector = true
                                    },
                                    shapes = ButtonDefaults.shapes(),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                                ) {
                                    Text(
                                        status.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            if (showStatusSelector) {
                                ModalEnumSelector(
                                    title = "Status",
                                    selectedOption = status,
                                    onOptionSelected = {
                                        status = it
                                        onStatusSaved?.invoke(it)
                                    },
                                    separatedOptions = listOf(ItemStatus.NONE),
                                    onDismiss = { showStatusSelector = false },
                                )
                            }
                        }
                    }

                    // Crew
                    crewByDepartment?.takeIf { it.isNotEmpty() }?.let { crew ->
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
                                Column(
                                    modifier = Modifier.padding(start = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CrewPersonRow(
                                        departmentFilter = "Directors",
                                        people = crew["Directing"],
                                        onPersonClick = onPersonClick,
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(end = 16.dp))
                                    CrewPersonRow(
                                        departmentFilter = "Writers",
                                        people = crew["Writing"],
                                        onPersonClick = onPersonClick,
                                    )
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
                                        items(cast.take(25), key = { it.creditId }) { member ->
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
                            val sortedImages = images.sortedBy { -it.voteCount }
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
                                    urlBuilder = { size, path ->
                                        "https://image.tmdb.org/t/p/${when(size) {
                                            ImageSize.MEDIUM -> "w500"
                                            ImageSize.LARGE -> "original"
                                        }}${path}"
                                    },
                                    sortedImages.map { it.toCarouselImage() },
                                    itemWidth = 110.dp,
                                    itemHeight = 180.dp,
                                    shape = MaterialTheme.shapes.large,
                                    maximizable = true,
                                    supportingContent = { url, onDismiss ->
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                                coverOverride = url
                                                onCoverOverrideSaved?.invoke(url)
                                                onDismiss()
                                            },
                                            shapes = ButtonDefaults.shapes(),
                                        ) {
                                            Icon(
                                                Icons.Default.ReplayCircleFilled,
                                                contentDescription = "Override",
                                                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                            )
                                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                            Text(
                                                "Override",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    state.images?.backdrops?.takeIf { it.isNotEmpty() }?.let { images ->
                        item {
                            val sortedImages = images.sortedBy { -it.voteCount }
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
                                    urlBuilder = { size, path ->
                                        "https://image.tmdb.org/t/p/${when(size) {
                                            ImageSize.MEDIUM -> "w780"
                                            ImageSize.LARGE -> "original"
                                        }}${path}"
                                    },
                                    sortedImages.map { it.toCarouselImage() },
                                    itemWidth = 240.dp,
                                    shape = MaterialTheme.shapes.large,
                                    maximizable = true
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
                                    items(reviews.sortedByDescending { parseDate(it.updatedAt) }, key = { it.id }) { review ->
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