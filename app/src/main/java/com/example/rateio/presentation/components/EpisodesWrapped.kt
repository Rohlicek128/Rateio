package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.TmdbEpisodeSummary


@Composable
fun EpisodeWrapped(
    seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>>,
    imdbRatings: Map<Int, Map<Int, Float?>>,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        seasonEpisodes
            .entries
            .sortedBy { it.key }
            .forEach { (seasonNumber, episodes) ->
                SeasonSection(
                    seasonNumber = seasonNumber,
                    episodes = episodes,
                    ratings = imdbRatings[seasonNumber] ?: emptyMap(),
                    onEpisodeClick = onEpisodeClick,
                )
            }
    }
}

@Composable
private fun SeasonSection(
    seasonNumber: Int,
    episodes: List<TmdbEpisodeSummary>,
    ratings: Map<Int, Float?>,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = "Season $seasonNumber",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        val columns = 5
        val rows = (episodes.size + columns - 1) / columns

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < episodes.size) {
                        RateBox(
                            rating = ratings[episodes[index].episodeNumber],
                            roundedCorners = 8.dp,
                            minWidth = 42.dp,
                            maxWidth = 42.dp,
                            height = 4.dp,
                            textStyle = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            onClick = {
                                onEpisodeClick(seasonNumber, episodes[index].episodeNumber)
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

}