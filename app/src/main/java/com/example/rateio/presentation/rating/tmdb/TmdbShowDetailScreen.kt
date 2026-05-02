package com.example.rateio.presentation.rating.tmdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.WrapText
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CastCard
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen


@Composable
fun TmdbShowDetailScreen(
    showId: Int,
    onBackClick: () -> Unit,
    onEpisodeClick: (showId: Int, seasonNumber: Int, episodeNumber: Int) -> Unit,
) {
    val viewModel: TmdbShowDetailViewModel = viewModel(
        factory = TmdbShowDetailViewModel.factory(showId)
    )
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
        state.show != null -> {
            val show = state.show!!
            val seasons = show.seasons.filter { it.seasonNumber > 0 }
            val gridViewModel: TmdbEpisodesViewModel = viewModel(
                factory = TmdbEpisodesViewModel.factory(
                    showId = showId,
                    seasonNumbers = seasons.map { it.seasonNumber },
                )
            )
            val gridState by gridViewModel.state.collectAsState()


            var selectedMode by remember { mutableIntStateOf(0) }

            RateItemDetailScreen(
                title = show.name,
                subtitle = buildString {
                    show.firstAirDate?.take(4)?.let { append(it) }
                    if (show.status != null) append(" · ${show.status}")
                }.ifBlank { null },
                description = show.overview,
                coverImageUrl = show.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = show.backdropPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                rating = state.imdbRating,
                ratingLabel = show.voteAverage?.let { "%.1f/10 on TMDb".format(it) },
                onBackClick = onBackClick,
                extraContent = {
                    // Genres
                    if (show.genres.isNotEmpty()) {
                        item {
                            GenreChips(
                                genres = show.genres.map { it.name },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // Cast
                    show.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
                        item {
                            SectionHeader("Cast")
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(cast.take(10), key = { it.creditId }) { member ->
                                    CastCard(member)
                                }
                            }
                        }
                    }

                    //Images
                    state.images?.posters?.takeIf { it.isNotEmpty() }?.let { images ->
                        item { SectionHeader("Posters") }
                        item {
                            AdaptiveImageCarousel(
                                baseUrl = "https://image.tmdb.org/t/p/w500",
                                images.sortedBy { -it.voteCount },
                                itemWidth = 110.dp,
                                itemHeight = 180.dp,
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }
                    state.images?.backdrops?.takeIf { it.isNotEmpty() }?.let { images ->
                        item { SectionHeader("Backdrops") }
                        item {
                            AdaptiveImageCarousel(
                                baseUrl = "https://image.tmdb.org/t/p/w780",
                                images.sortedBy { -it.voteCount },
                                itemWidth = 240.dp,
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Seasons
                    item { SectionHeader("Seasons / Episodes") }
                    item { DisplaySelector(selectedMode, onSelectionChanged = { selectedMode = it }) }

                    when (selectedMode) {
                        0 -> {
                            /*items(seasons, key = { it.id }) { season ->
                                var ratingTest: Float? = null
                                if (season.voteAverage != null) {
                                    if (season.voteAverage > 0f) ratingTest = season.voteAverage.div(10f).plus(0.1f)
                                }
                                RateItemCard(
                                    title = "Season ${season.seasonNumber}",
                                    subtitle = "${(season.airDate ?: "N/A").take(4)}  |  ${season.episodeCount} episodes",
                                    coverImagePath = "https://image.tmdb.org/t/p/w185${season.posterPath}",
                                    rating = ratingTest,
                                    onClick = { onSeasonClick(season.id, showId) }
                                )
                            }*/

                            when {
                                gridState.isLoading -> item {
                                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                        CircularWavyProgressIndicator()
                                    }
                                }
                                gridState.seasonEpisodes.isNotEmpty() -> {
                                    gridState.seasonEpisodes
                                        .entries
                                        .sortedBy { it.key }
                                        .forEach { (seasonNumber, episodes) ->
                                            item { SectionHeader("Seasons $seasonNumber") }

                                            items(episodes.sortedBy { it.episodeNumber }) { episode ->
                                                var rating: Float? = null
                                                if (episode.voteAverage != null) {
                                                    if (episode.voteAverage > 0f) rating = episode.voteAverage.div(10f)
                                                }
                                                RateItemCard(
                                                    title = episode.name,
                                                    subtitle = "Episode ${episode.episodeNumber}  |  ${if (episode.runtime > 0) "${episode.runtime}m" else "N/A"}",
                                                    coverImagePath = "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                                                    rating = rating,
                                                    placeholderRatio = 16f / 9f,
                                                    padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                                    onClick = { onEpisodeClick(
                                                            show.id,
                                                            episode.seasonNumber,
                                                            episode.episodeNumber
                                                        ) },
                                                )
                                            }
                                        }
                                }
                            }
                        }
                        2 -> {
                            when {
                                gridState.isLoading -> item {
                                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                        CircularWavyProgressIndicator()
                                    }
                                }
                                gridState.seasonEpisodes.isNotEmpty() -> item {
                                    EpisodeGrid(
                                        seasonEpisodes = gridState.seasonEpisodes,
                                        onEpisodeClick = { season, episode ->
                                            run {
                                                onEpisodeClick(
                                                    show.id,
                                                    season,
                                                    episode
                                                )
                                            }
                                        },
                                        modifier = Modifier.padding(bottom = 16.dp),
                                    )
                                }
                            }
                        }
                        else -> {
                            item {
                                Text("Not implemented", modifier = Modifier.padding(horizontal = 16.dp))
                                Spacer(modifier = Modifier.padding(vertical = 400.dp))
                            }
                        }
                    }

                }
            )
        }
    }
}


@Composable
private fun GenreChips(genres: List<String>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        genres.forEach { genre ->
            SuggestionChip(
                onClick = {},
                label = { Text(genre) },
            )
        }
    }
}


@Composable
private fun DisplaySelector(selectedIndex: Int, onSelectionChanged: (Int) -> Unit,) {
    val options = listOf("List", "Grid", "Wrapped", "Timeline")
    val unCheckedIcons = listOf(Icons.AutoMirrored.Outlined.List, Icons.Outlined.GridOn, Icons.AutoMirrored.Outlined.WrapText, Icons.Outlined.Timeline)
    val checkedIcons = listOf(Icons.AutoMirrored.Filled.List, Icons.Filled.GridOn, Icons.AutoMirrored.Filled.WrapText, Icons.Filled.Timeline)

    Row (
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, label ->
            OutlinedToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onSelectionChanged(index) },
                modifier = Modifier.semantics { role = Role.RadioButton },
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Icon(
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index],
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}