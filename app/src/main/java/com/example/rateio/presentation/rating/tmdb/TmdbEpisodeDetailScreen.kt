package com.example.rateio.presentation.rating.tmdb

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
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.remote.tmdb.toCarouselImage
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CollapsibleHeader
import com.example.rateio.presentation.components.ImageSize
import com.example.rateio.presentation.components.ItemStatCard
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.ScreenError
import com.example.rateio.presentation.components.ScreenLoading
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.utils.formatCompact
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.formatItemRankLabel
import com.example.rateio.utils.formatTime
import java.util.Locale


@Composable
fun TmdbEpisodeDetailScreen(
    showId: Int,
    season: Int,
    episode: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    savedRank: Int? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onNextClick: (season: Int, episode: Int) -> Unit,
    onPreviousClick: (season: Int, episode: Int) -> Unit,
    onBackClick: () -> Unit,
    debug: String? = null,
    viewModel: TmdbEpisodeDetailViewModel = viewModel(
        factory = TmdbEpisodeDetailViewModel.factory(showId, season, episode)
    ),
) {
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

            RateItemDetailScreen(
                title = episode.name,
                subtitle = formatDate(episode.airDate),
                categoryName = "Episodes",
                description = episode.overview,
                coverImageUrl = episode.stillPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                placeholderRatio = 16f / 9f,
                backdropImageUrl = episode.stillPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                rating = if (!isSaved) state.imdbRating?.normalizedRating else customRating,
                ratingVotes = if (!isSaved) state.imdbRating?.voteCount else null,
                ratingLabel = savedRank?.let { formatItemRankLabel(it, CategoryType.TMDB_EPISODES) },
                onRatingSaved = onRatingSaved,
                onBackClick = onBackClick,
                debug = debug,
                headerExtraContent = {
                    //Stats
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            ItemStatCard(
                                header = "Episode",
                                statistic = "S${episode.seasonNumber}E${episode.episodeNumber}",
                            )
                            ItemStatCard(
                                header = "Runtime",
                                statistic = formatTime(episode.runtime),
                            )
                            if (!episode.productionCode.isNullOrBlank()) {
                                ItemStatCard(
                                    header = "Code",
                                    statistic = episode.productionCode,
                                )
                            }
                        }
                    }
                },
                extraContent = {

                    // Move buttons
                    item {
                        MoveButtons(
                            state.previousEpisode,
                            state.nextEpisode,
                            onPreviousClick = onPreviousClick,
                            onNextClick = onNextClick,
                        )
                    }

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
        }
    }
}

@Composable
private fun MoveButtons(
    previousEpisode: Pair<Int, Int>?,
    nextEpisode: Pair<Int, Int>?,
    onNextClick: (season: Int, episode: Int) -> Unit,
    onPreviousClick: (season: Int, episode: Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            enabled = previousEpisode != null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                val (prevSeason, prevEpisode) = previousEpisode!!
                onPreviousClick(prevSeason, prevEpisode)
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
                val (nextSeason, nextEpisode) = nextEpisode!!
                onNextClick(nextSeason, nextEpisode)
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