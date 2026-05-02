package com.example.rateio.presentation.rating.tmdb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.TmdbEpisodeSummary
import com.example.rateio.presentation.components.RateBox


@Composable
fun EpisodeGrid(
    seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>>,
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
                    onEpisodeClick = onEpisodeClick,
                )
            }
    }
}

@Composable
private fun SeasonSection(
    seasonNumber: Int,
    episodes: List<TmdbEpisodeSummary>,
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
                            rating = episodes[index].voteAverage?.let {
                                if (it > 0f) episodes[index].voteAverage?.div(10f) else null
                            },
                            roundedCorners = 8.dp,
                            minWidth = 44.dp,
                            maxWidth = 44.dp,
                            height = 6.dp,
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