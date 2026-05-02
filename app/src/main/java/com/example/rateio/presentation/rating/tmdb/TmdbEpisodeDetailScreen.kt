package com.example.rateio.presentation.rating.tmdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.rateio.data.remote.TmdbCastMember
import com.example.rateio.presentation.components.AdaptiveAsyncImage
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CastCard
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen


@Composable
fun TmdbEpisodeDetailScreen(
    showId: Int,
    season: Int,
    episode: Int,
    onBackClick: () -> Unit,
) {
    val viewModel: TmdbEpisodeDetailViewModel = viewModel(
        factory = TmdbEpisodeDetailViewModel.factory(showId, season, episode)
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
        state.episode != null -> {
            val episode = state.episode!!

            RateItemDetailScreen(
                title = episode.name,
                subtitle = "Season ${episode.seasonNumber}, Episode ${episode.episodeNumber}  |  ${episode.airDate ?: "N/A"}  |  ${if (episode.runtime > 0) "${episode.runtime}m" else "N/A"}",
                description = episode.overview,
                coverImageUrl = episode.stillPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                placeholderRatio = 16f / 9f,
                backdropImageUrl = episode.stillPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                rating = state.imdbRating,
                ratingLabel = episode.voteAverage?.let { "%.1f/10 on TMDb".format(it) },
                onBackClick = onBackClick,
                extraContent = {

                    // Cast
                    episode.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
                        item { SectionHeader("Cast") }
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
                    state.images?.stills?.takeIf { it.isNotEmpty() }?.let { images ->
                        item { SectionHeader("Images") }
                        item {
                            AdaptiveImageCarousel(
                                baseUrl = "https://image.tmdb.org/t/p/w300",
                                images.sortedBy { -it.voteCount },
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