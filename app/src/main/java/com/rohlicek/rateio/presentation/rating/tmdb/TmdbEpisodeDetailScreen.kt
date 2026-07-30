package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.toCarouselImage
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.components.AdaptiveImageCarousel
import com.rohlicek.rateio.presentation.components.CollapsibleHeader
import com.rohlicek.rateio.presentation.components.ImageSize
import com.rohlicek.rateio.presentation.components.statistics.ItemRatingStatCard
import com.rohlicek.rateio.presentation.components.statistics.ItemStatCard
import com.rohlicek.rateio.presentation.components.PersonCard
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.rating.RateItemDetailScreen
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.RatingTransformationsConstants
import com.rohlicek.rateio.utils.formatDateCompact
import com.rohlicek.rateio.utils.formatItemRankLabel
import com.rohlicek.rateio.utils.formatTime


@Composable
fun TmdbEpisodeDetailScreen(
    showId: Int,
    season: Int,
    episode: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    savedRank: Int? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    seasonEpisodeCount: Int? = null,
    onNextClick: (season: Int, episode: Int, episodeCount: Int?) -> Unit,
    onPreviousClick: (season: Int, episode: Int, episodeCount: Int?) -> Unit,
    onBackClick: () -> Unit,
    debug: String? = null,
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: TmdbEpisodeDetailViewModel = viewModel(
        factory = TmdbEpisodeDetailViewModel.factory(
            showId,
            season,
            episode,
            seasonEpisodeCount,
            imdbRepository
        )
    )
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
        }
        state.episode != null -> {
            val episode = state.episode!!

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                RateItemDetailScreen(
                    title = episode.name,
                    subtitle = "Season ${episode.seasonNumber}, Episode ${episode.episodeNumber}",
                    categoryName = "Episodes",
                    description = episode.overview,
                    coverImageUrl = episode.stillPath?.let {
                        "https://image.tmdb.org/t/p/original$it"
                    } ?: "",
                    placeholderRatio = 16f / 9f,
                    backdropImageUrl = episode.stillPath?.let {
                        "https://image.tmdb.org/t/p/original$it"
                    },
                    rating = if (!isSaved) state.imdbRating?.averageRating else customRating,
                    ratingVotes = if (!isSaved) state.imdbRating?.numVotes else null,
                    ratingLabel = savedRank?.let { formatItemRankLabel(it, CategoryType.TMDB_EPISODES) },
                    onRatingSaved = onRatingSaved,
                    onBackClick = onBackClick,
                    debug = (if (!episode.productionCode.isNullOrBlank()) {
                        episode.productionCode + "  "
                    } else "") + (debug ?: ""),
                    headerExtraContent = {
                        //Stats
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                ItemStatCard(
                                    header = "Release Date",
                                    statistic = formatDateCompact(episode.airDate),
                                )
                                ItemStatCard(
                                    header = "Runtime",
                                    statistic = formatTime(episode.runtime),
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
                                    ItemRatingStatCard(
                                        rating = state.imdbRating?.averageRating,
                                        votes = state.imdbRating?.numVotes,
                                        source = "IMDb",
                                        transformationOverride = RatingTransformationsConstants.TF_IMDB,
                                        colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_EPISODES,
                                        onClickUrl = state.episode?.externalIds?.imdbId?.let { "https://www.imdb.com/title/$it" },
                                    )
                                }
                                ItemRatingStatCard(
                                    rating = episode.voteAverage?.div(10f),
                                    votes = episode.voteCount,
                                    source = "TMDB",
                                    transformationOverride = RatingTransformationsConstants.TF_PERCENTAGE,
                                    colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_EPISODES,
                                    onClickUrl = "https://www.themoviedb.org/tv/${showId}/season/${season}/episode/${episode.episodeNumber}",
                                )
                                if (!isSaved) {
                                    ItemRatingStatCard(
                                        rating = state.savedItem?.rating,
                                        votes = null,
                                        source = "Yours",
                                        showNullVotes = false,
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(10.dp)) }

                        // Crew
                        episode.credits?.crew?.takeIf { it.isNotEmpty() }?.let { crew ->
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
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Cast
                        if (episode.credits != null && (episode.credits.cast.isNotEmpty() || episode.credits.guest.isNotEmpty())) {
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
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        episode.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
                                            items(cast.take(10), key = { it.creditId }) { member ->
                                                PersonCard(
                                                    name = member.name,
                                                    position = member.character,
                                                    profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                                )
                                            }
                                        }
                                        episode.credits?.guest?.takeIf { it.isNotEmpty() }?.let { guest ->
                                            items(guest.sortedByDescending { it.popularity }.take(20), key = { it.creditId }) { member ->
                                                PersonCard(
                                                    name = member.name,
                                                    position = member.character,
                                                    profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                                    width = 80.dp,
                                                    height = 100.dp,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //Images
                        state.images?.stills?.takeIf { it.isNotEmpty() }?.let { images ->
                            item {
                                val headerName = "Images"
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
                                                ImageSize.MEDIUM -> "w300"
                                                ImageSize.LARGE -> "original"
                                            }}${path}"
                                        },
                                        images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                        itemWidth = 250.dp,
                                        shape = MaterialTheme.shapes.large,
                                        maximizable = true,
                                    )
                                }
                            }
                        }

                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(135.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        )
                )

                FloatingMoveButtonsExpressive(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 28.dp, end = 28.dp, bottom = 56.dp),
                    previousEpisode = state.previousEpisode,
                    nextEpisode = state.nextEpisode,
                    onPreviousClick = onPreviousClick,
                    onNextClick = onNextClick,
                )
            }
        }
    }
}


@Composable
private fun FloatingMoveButtonsExpressive(
    previousEpisode: EpisodeMoveData?,
    nextEpisode: EpisodeMoveData?,
    onNextClick: (season: Int, episode: Int, episodeCount: Int?) -> Unit,
    onPreviousClick: (season: Int, episode: Int, episodeCount: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    val interactionSources = remember(2) {
        List(2) { MutableInteractionSource() }
    }

    val buttonWidth = 165.dp
    val iconSize = 24.dp
    val colors = ButtonDefaults.filledTonalButtonColors().copy(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    ButtonGroup(
        modifier = modifier,
        expandedRatio = 0.2f,
        overflowIndicator = {},
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        customItem(
            buttonGroupContent = {
                ElevatedButton(
                    modifier = Modifier.width(buttonWidth).animateWidth(interactionSource = interactionSources[0]),
                    enabled = previousEpisode != null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onPreviousClick(
                            previousEpisode!!.seasonNumber,
                            previousEpisode.episodeNumber,
                            previousEpisode.episodeCount
                        )
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = colors,
                    interactionSource = interactionSources[0],
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous episode",
                        modifier = Modifier.size(iconSize),
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        "Previous",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                    )
                }
            },
            menuContent = {}
        )

        customItem(
            buttonGroupContent = {
                ElevatedButton(
                    modifier = Modifier.width(buttonWidth).animateWidth(interactionSource = interactionSources[1]),
                    enabled = nextEpisode != null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onNextClick(
                            nextEpisode!!.seasonNumber,
                            nextEpisode.episodeNumber,
                            nextEpisode.episodeCount
                        )
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = colors,
                    interactionSource = interactionSources[1],
                ) {
                    Text(
                        "Next",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next episode",
                        modifier = Modifier.size(iconSize),
                    )
                }
            },
            menuContent = {}
        )
    }
}

@Composable
private fun MoveButtons(
    previousEpisode: EpisodeMoveData?,
    nextEpisode: EpisodeMoveData?,
    onNextClick: (season: Int, episode: Int, episodeCount: Int?) -> Unit,
    onPreviousClick: (season: Int, episode: Int, episodeCount: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            enabled = previousEpisode != null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onPreviousClick(
                    previousEpisode!!.seasonNumber,
                    previousEpisode.episodeNumber,
                    previousEpisode.episodeCount
                )
            },
            shapes = ButtonDefaults.shapes(),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous episode",
                modifier = Modifier.size(ToggleButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
            Text(
                "Previous",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(4.dp))

        OutlinedButton(
            enabled = nextEpisode != null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onNextClick(
                    nextEpisode!!.seasonNumber,
                    nextEpisode.episodeNumber,
                    nextEpisode.episodeCount
                )
            },
            shapes = ButtonDefaults.shapes(),
        ) {
            Text(
                "Next",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next episode",
                modifier = Modifier.size(ToggleButtonDefaults.IconSize),
            )
        }

    }
}