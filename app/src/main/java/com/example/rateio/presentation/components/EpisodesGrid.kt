package com.example.rateio.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.tmdb.TmdbEpisodeSummary


@Composable
fun EpisodeGrid(
    seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>>,
    imdbRatings: Map<Int, Map<Int, Float?>>,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(12.dp)
            .horizontalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(48.dp),
            ) {
                Text(
                    text = "   ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
                val longestSeason = seasonEpisodes.values.maxBy { it.size }
                longestSeason
                    .sortedBy { it.episodeNumber }
                    .forEach { episode ->
                        Text(
                            text = "E${episode.episodeNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }
            }

            seasonEpisodes
                .entries
                .sortedBy { it.key }
                .forEach { (seasonNumber, episodes) ->
                    SeasonColumn(
                        seasonNumber = seasonNumber,
                        episodes = episodes,
                        ratings = imdbRatings[seasonNumber] ?: emptyMap(),
                        onEpisodeClick = onEpisodeClick,
                    )
                }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(48.dp),
            ) {
                Text(
                    text = "Avg",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }

            seasonEpisodes
                .entries
                .sortedBy { it.key }
                .forEach { (seasonNumber) ->
                    SeasonAverage(
                        ratings = imdbRatings[seasonNumber] ?: emptyMap(),
                    )
                }
        }
    }
}

@Composable
private fun SeasonColumn(
    seasonNumber: Int,
    episodes: List<TmdbEpisodeSummary>?,
    ratings: Map<Int, Float?>,
    onEpisodeClick: (Int, Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "S$seasonNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val width = 39.dp
        val height = 4.dp
        episodes
            ?.sortedBy { it.episodeNumber }
            ?.forEach { episode ->
                RateBox(
                    rating = ratings[episode.episodeNumber],
                    roundedCorners = 7.dp,
                    minWidth = width,
                    maxWidth = width,
                    height = height,
                    textStyle = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    onClick = {
                        onEpisodeClick(seasonNumber, episode.episodeNumber)
                    }
                )
            }

        episodes?.size?.let {
            if (it <= 0) {
                RateBox(
                    rating = null,
                    roundedCorners = 7.dp,
                    minWidth = width,
                    maxWidth = width,
                    height = height,
                    textStyle = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SeasonAverage(
    ratings: Map<Int, Float?>,
) {
    val flatRatings = ratings.values.filterNotNull()

    val width = 39.dp
    val height = 4.dp
    RateBox(
        rating = if (flatRatings.isNotEmpty()) flatRatings.average().toFloat() else null,
        roundedCorners = 7.dp,
        minWidth = width,
        maxWidth = width,
        height = height,
        textStyle = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}