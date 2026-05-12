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
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.remote.toCarouselImage
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.DateProgressBar
import com.example.rateio.presentation.components.DisplaySelector
import com.example.rateio.presentation.components.EpisodeGrid
import com.example.rateio.presentation.components.EpisodeWrapped
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.components.rating.EpisodeRatingGraph
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.formatTime


@Composable
fun TmdbShowDetailScreen(
    showId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onBackClick: () -> Unit,
    onEpisodeClick: (showId: Int, seasonNumber: Int, episodeNumber: Int) -> Unit,
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

    val viewModel: TmdbShowDetailViewModel = viewModel(
        factory = TmdbShowDetailViewModel.factory(showId, categoryRepository, itemRepository)
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
            val episodesViewModel: TmdbEpisodesViewModel = viewModel(
                factory = TmdbEpisodesViewModel.factory(
                    showId = showId,
                    imdbId = show.externalIds?.imdbId,
                    seasonNumbers = seasons.map { it.seasonNumber },
                )
            )
            val episodesState by episodesViewModel.state.collectAsState()

            var selectedRatings by remember { mutableIntStateOf(if (!isSaved) 0 else 2) }
            var invertedGrid by remember { mutableStateOf(false) }

            RateItemDetailScreen(
                title = show.name,
                subtitle = buildString {
                    show.firstAirDate?.take(4)?.let { append(it) }
                    show.lastAirDate?.take(4)?.let { append(" - $it") }
                    if (show.status != null) append("  |  ${show.status}")
                }.ifBlank { null },
                categoryName = CategoryRegistry.forType(CategoryType.TMDB_SHOWS)?.name,
                description = show.overview,
                coverImageUrl = show.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = show.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = if (!isSaved) state.imdbRating?.normalizedRating else customRating,
                ratingVotes = if (!isSaved) state.imdbRating?.voteCount else null,
                ratingLabel = state.imdbRating?.normalizedRating?.let { "%.1f/10 on IMDb".format(it * 10f) },
                onRatingSaved = onRatingSaved,
                onBackClick = onBackClick,
                canAddToLibrary = false,
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LibraryToggle(
                                checked = state.savedItemId != null,
                                onCheckedChange = {
                                    viewModel.onToggleSaved(state.show!!)
                                },
                                itemName = show.name,
                            )
                        }
                    }


                    // Genres
                    if (show.genres.isNotEmpty()) {
                        item {
                            GenreChips(
                                genres = show.genres.map { it.name },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // Next Episode
                    if (show.nextEpisodeToAir != null) {
                        item { SectionHeader("Next Episode") }
                        val episode = show.nextEpisodeToAir
                        item {
                            DateProgressBar(
                                startDateString = show.lastEpisodeToAir?.airDate,
                                endDateString = episode.airDate,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                            )
                        }
                        item {
                            RateItemCard(
                                title = episode.name,
                                subtitle = "S${episode.seasonNumber}E${episode.episodeNumber}  |  ${formatDate(episode.airDate)}",
                                coverImagePath = if (!episode.stillPath.isNullOrBlank())
                                    "https://image.tmdb.org/t/p/w300${episode.stillPath}" else null,
                                rating = episodesState.imdbRatings[episode.seasonNumber]?.get(episode.episodeNumber),
                                isLoading = false,
                                placeholderRatio = 16f / 9f,
                                padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                onClick = { onEpisodeClick(
                                    show.id,
                                    episode.seasonNumber,
                                    episode.episodeNumber
                                ) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Creators
                    show.createdBy.takeIf { it.isNotEmpty() }?.let { creator ->
                        item { SectionHeader("Creators") }
                        item {
                            LazyRow(
                                modifier = Modifier.height(120.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(creator.take(10), key = { it.creditId }) { member ->
                                    PersonCard(
                                        name = member.name,
                                        position = null,
                                        profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                        width = 80.dp,
                                        height = 80.dp,
                                    )
                                }
                            }
                        }
                    }

                    // Cast
                    if (show.credits != null && show.credits.cast.isNotEmpty()) {
                        item { SectionHeader("Cast") }
                        item {
                            LazyRow(
                                modifier = Modifier.height(150.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                show.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
                                    items(cast.take(10), key = { it.creditId }) { member ->
                                        PersonCard(
                                            name = member.name,
                                            position = member.character,
                                            profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                        )
                                    }
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
                                images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
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
                                images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                itemWidth = 240.dp,
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Episodes
                    item { SectionHeader("Episodes") }
                    item {
                        DisplaySelector(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            selectedIndex = selectedRatings,
                            onSelectionChanged = { selectedRatings = it },
                            options = listOf("IMDb", "TMDb", "Yours"),
                        )
                    }
                    item {
                        DisplaySelector(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            selectedIndex = state.selectedEpisodeMode,
                            onSelectionChanged = viewModel::onModeSelect,
                            options = listOf("List", "Grid", "Wrapped", "Timeline"),
                            unCheckedIcons = listOf(Icons.AutoMirrored.Outlined.List, Icons.Outlined.GridOn, Icons.AutoMirrored.Outlined.WrapText, Icons.Outlined.Timeline),
                            checkedIcons = listOf(Icons.AutoMirrored.Filled.List, Icons.Filled.GridOn, Icons.AutoMirrored.Filled.WrapText, Icons.Filled.Timeline),
                        )
                    }

                    when {
                        episodesState.isLoadingEpisodes -> item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                CircularWavyProgressIndicator()
                            }
                        }
                        episodesState.seasonEpisodes.isNotEmpty() -> {
                            val ratings: Map<Int, Map<Int, Float?>> = when (selectedRatings) {
                                0 -> episodesState.imdbRatings
                                1 -> episodesState.seasonEpisodes.mapValues { (_, episodes) ->
                                    episodes.associate { it -> it.episodeNumber to it.voteAverage?.div(10f).takeIf { it != 0f } }
                                }
                                2 -> episodesState.seasonEpisodes.mapValues { (_, episodes) ->
                                    episodes.associate { it.episodeNumber to null }
                                }
                                else -> episodesState.imdbRatings
                            }

                            when (state.selectedEpisodeMode) {
                                0 -> {
                                    items(seasons, key = { it.id }) { season ->
                                        val flatRatings = ratings[season.seasonNumber]?.values?.filterNotNull()
                                        RateItemCard(
                                            title = "Season ${season.seasonNumber}",
                                            subtitle = "${(season.airDate ?: "N/A").take(4)}  |  ${season.episodeCount} episodes",
                                            coverImagePath = "https://image.tmdb.org/t/p/w185${season.posterPath}",
                                            rating = if (!episodesState.isLoadingRatings && !flatRatings.isNullOrEmpty())
                                                flatRatings.average().toFloat() else null,
                                            onClick = { }
                                        )
                                    }

                                    episodesState.seasonEpisodes
                                        .entries
                                        .sortedBy { it.key }
                                        .forEach { (seasonNumber, episodes) ->
                                            item { SectionHeader("Seasons $seasonNumber") }

                                            items(
                                                episodes.sortedBy { it.episodeNumber },
                                                key = { it.id },
                                            ) { episode ->
                                                val rating = ratings[seasonNumber]?.get(episode.episodeNumber)
                                                RateItemCard(
                                                    title = episode.name,
                                                    subtitle = "Episode ${episode.episodeNumber}",
                                                    coverImagePath = "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                                                    rating = if (!episodesState.isLoadingRatings && rating == null && ratings[seasonNumber]?.isEmpty() == true)
                                                        episode.voteAverage?.div(10f) else rating,
                                                    isLoading = episodesState.isLoadingRatings && rating == null,
                                                    placeholderRatio = 16f / 9f,
                                                    padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                                    bubbleText = if (episode.runtime > 0) formatTime(episode.runtime) else null,
                                                    onClick = { onEpisodeClick(
                                                        show.id,
                                                        episode.seasonNumber,
                                                        episode.episodeNumber
                                                    ) },
                                                )
                                            }
                                        }

                                    /*items(
                                        episodesState.seasonEpisodes
                                            .values.flatten()
                                            .sortedBy { -(episodesState.imdbRatings[it.seasonNumber]?.get(it.episodeNumber) ?: -1f) },
                                        key = { it.id },
                                    ) { episode ->
                                        val rating = episodesState.imdbRatings[episode.seasonNumber]?.get(episode.episodeNumber)
                                        RateItemCard(
                                            title = episode.name,
                                            subtitle = "S${episode.seasonNumber}E${episode.episodeNumber}  |  ${if (episode.runtime > 0) "${episode.runtime}m" else "N/A"}",
                                            coverImagePath = "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                                            rating = if (!episodesState.isLoadingRatings && rating == null && episodesState.imdbRatings[episode.seasonNumber]?.isEmpty() == true)
                                                episode.voteAverage?.div(10f) else rating,
                                            isLoading = episodesState.isLoadingRatings && rating == null,
                                            placeholderRatio = 16f / 9f,
                                            padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                            onClick = { onEpisodeClick(
                                                show.id,
                                                episode.seasonNumber,
                                                episode.episodeNumber
                                            ) },
                                        )
                                    }*/

                                }
                                1 -> {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            OutlinedToggleButton(
                                                checked = invertedGrid,
                                                onCheckedChange = { invertedGrid = it },
                                                shapes = ToggleButtonDefaults.shapes(),
                                            ) {
                                                Text(
                                                    "Inverted",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                    }
                                    item {
                                        EpisodeGrid(
                                            seasonEpisodes = episodesState.seasonEpisodes,
                                            imdbRatings = ratings,
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
                                2 -> {
                                    item {
                                        EpisodeWrapped(
                                            seasonEpisodes = episodesState.seasonEpisodes,
                                            imdbRatings = ratings,
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
                                3 -> {
                                    item {
                                        EpisodeRatingGraph(
                                            episodes = episodesState.seasonEpisodes,
                                            imdbRatings = ratings,
                                            onEpisodeClick = { season, episode ->
                                                onEpisodeClick(showId, season, episode)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                        )
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
                    }

                }
            )

        }
    }
}