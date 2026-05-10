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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.remote.steam.toCarouselImage
import com.example.rateio.data.remote.toCarouselImage
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.utils.formatDate


@Composable
fun TmdbEpisodeDetailScreen(
    showId: Int,
    season: Int,
    episode: Int,
    onNextClick: (season: Int, episode: Int) -> Unit,
    onPreviousClick: (season: Int, episode: Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: TmdbEpisodeDetailViewModel = viewModel(
        factory = TmdbEpisodeDetailViewModel.factory(showId, season, episode)
    ),
) {
    val state by viewModel.state.collectAsState()

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
        state.episode != null -> {
            val episode = state.episode!!

            RateItemDetailScreen(
                title = episode.name,
                subtitle = "Season ${episode.seasonNumber}, Episode ${episode.episodeNumber}  |  ${formatDate(episode.airDate)}  |  ${if (episode.runtime > 0) "${episode.runtime}m" else "N/A"}",
                categoryName = "Episodes",
                description = episode.overview,
                coverImageUrl = episode.stillPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                placeholderRatio = 16f / 9f,
                backdropImageUrl = episode.stillPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                rating = state.imdbRating?.normalizedRating,
                ratingVotes = state.imdbRating?.voteCount,
                ratingLabel = episode.voteAverage?.let { "%.1f/10 on TMDb".format(it) },
                onBackClick = onBackClick,
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
                        item { SectionHeader("Crew") }
                        item {
                            LazyRow(
                                modifier = Modifier.height(130.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(crew.take(10), key = { it.creditId }) { member ->
                                    PersonCard(
                                        name = member.name,
                                        position = member.job,
                                        profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                        width = 80.dp,
                                        height = 80.dp,
                                    )
                                }
                            }
                        }
                    }

                    // Cast
                    if (episode.credits != null && (episode.credits.cast.isNotEmpty() || episode.credits.guest.isNotEmpty())) {
                        item { SectionHeader("Cast") }
                        item {
                            LazyRow(
                                modifier = Modifier.height(150.dp),
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
                                    items(guest.take(10), key = { it.creditId }) { member ->
                                        PersonCard(
                                            name = member.name,
                                            position = member.character,
                                            profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                            width = 80.dp,
                                            height = 80.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    //Images
                    state.images?.stills?.takeIf { it.isNotEmpty() }?.let { images ->
                        item { SectionHeader("Images") }
                        item {
                            AdaptiveImageCarousel(
                                baseUrl = "https://image.tmdb.org/t/p/w300",
                                images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                itemWidth = 250.dp,
                                shape = MaterialTheme.shapes.large,
                            )
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