package com.example.rateio.presentation.rating.tmdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    viewModel: TmdbMovieDetailViewModel = viewModel(
        factory = TmdbMovieDetailViewModel.factory(movieId)
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