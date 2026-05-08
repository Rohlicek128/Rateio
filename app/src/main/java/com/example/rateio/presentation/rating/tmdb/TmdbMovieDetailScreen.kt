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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.utils.formatDate


@Composable
fun TmdbMovieDetailScreen(
    movieId: Int,
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
        state.movie != null -> {
            val movie = state.movie!!

            RateItemDetailScreen(
                title = movie.title,
                subtitle = buildString {
                    append(formatDate(movie.releaseDate))
                    if (movie.status != null) append("  |  ${movie.status}  |  ${if (movie.runtime > 0) movie.runtime.toString() + "m" else "N/A"}")
                }.ifBlank { null },
                categoryName = "Movie",
                description = movie.overview,
                coverImageUrl = movie.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = movie.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = state.imdbRating?.normalizedRating,
                ratingVotes = state.imdbRating?.voteCount,
                ratingLabel = movie.voteAverage?.let { "%.1f/10 on TMDb".format(it) },
                onBackClick = onBackClick,
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            val isSaved = state.savedItemId != null
                            OutlinedToggleButton(
                                checked = isSaved,
                                onCheckedChange = { viewModel.onToggleSaved(state.movie!!) },
                                shapes = ToggleButtonDefaults.shapes(),
                            ) {
                                if (isSaved) {
                                    Icon(
                                        Icons.Filled.Bookmark,
                                        contentDescription = "Remove from library",
                                        modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                    )
                                } else {
                                    Icon(
                                        Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Add to library",
                                        modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                    )
                                }

                                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))

                                Text(
                                    if (isSaved) "Remove from library" else "Add to library",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
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

                    // Crew
                    movie.credits?.crew?.takeIf { it.isNotEmpty() }?.let { crew ->
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
                    if (movie.credits != null && movie.credits.cast.isNotEmpty()) {
                        item { SectionHeader("Cast") }
                        item {
                            LazyRow(
                                modifier = Modifier.height(150.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                movie.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
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

                }
            )
        }
    }
}