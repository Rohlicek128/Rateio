package com.example.rateio.presentation.rating.tmdb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rateio.data.remote.TmdbCastMember
import com.example.rateio.data.remote.TmdbSeason
import com.example.rateio.presentation.components.AdaptiveAsyncImage
import com.example.rateio.presentation.rating.RateItemDetailScreen


@Composable
fun TmdbShowDetailScreen(
    showId: Int,
    onBackClick: () -> Unit,
    onSeasonClick: (seasonId: Int, showId: Int) -> Unit = { _, _ -> },
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
                rating = show.voteAverage?.div(10f),
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
                                items(cast.take(10), key = { it.name }) { member ->
                                    CastCard(member)
                                }
                            }
                        }
                    }

                    // Seasons
                    val seasons = show.seasons.filter { it.seasonNumber > 0 }
                    if (seasons.isNotEmpty()) {
                        item { SectionHeader("Seasons") }
                        items(seasons, key = { it.id }) { season ->
                            SeasonRow(
                                season = season,
                                onClick = { onSeasonClick(season.id, showId) },
                            )
                        }
                    }
                }
            )
        }
    }
}

// -- Supporting composables --

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
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
private fun CastCard(member: TmdbCastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp),
    ) {
        AsyncImage(
            model = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = member.character,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SeasonRow(season: TmdbSeason, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Season ${season.seasonNumber}") },
        supportingContent = { Text("${season.episodeCount} episodes") },
        leadingContent = {
            Card(
                shape = MaterialTheme.shapes.small,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = season.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                    contentDescription = "Poster",
                    modifier = Modifier.height(80.dp)
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
    HorizontalDivider()
}