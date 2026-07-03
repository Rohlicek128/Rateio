package com.example.rateio.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.tmdb.TmdbEpisodeSummary
import com.example.rateio.presentation.rating.display.getTransformedRating


@Composable
fun EpisodeWrapped(
    seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>>,
    imdbRatings: Map<Int, Map<Int, Float?>>,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    //Spacer(modifier = Modifier.height(8.dp))

    var columnCount by remember { mutableFloatStateOf(5f) }
    val padding = 28.dp

    Row(
        modifier
            .fillMaxWidth()
            .padding(top = padding, start = padding, end = padding),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Columns",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = columnCount.toInt().toString(),
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleLarge,
        )
    }
    Slider(
        columnCount,
        onValueChange = { columnCount = it },
        modifier = modifier
            .padding(horizontal = 24.dp)
            .offset(y = (-18).dp),
        steps = 8,
        valueRange = 1f..10f
    )

    //Spacer(modifier = Modifier.height(4.dp))

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.horizontalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        seasonEpisodes
            .entries
            .sortedBy { it.key }
            .forEach { (seasonNumber, episodes) ->
                SeasonSection(
                    seasonNumber = seasonNumber,
                    episodes = episodes,
                    ratings = imdbRatings[seasonNumber] ?: emptyMap(),
                    columns = columnCount.toInt(),
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
    columns: Int,
    onEpisodeClick: (season: Int, episode: Int) -> Unit,
) {
    val gridGap = 6.dp
    Column(verticalArrangement = Arrangement.spacedBy(gridGap)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Season $seasonNumber",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            val flatRatings = ratings.values.filterNotNull()
            if (flatRatings.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))

                val display = getTransformedRating(flatRatings.average().toFloat())
                Text(
                    text = "(avg. ${display})",
                    style = MaterialTheme.typography.titleMedium,
                    //fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val rows = (episodes.size + columns - 1) / columns

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(gridGap),
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < episodes.size) {
                        RateBox(
                            rating = ratings[episodes[index].episodeNumber],
                            roundedCorners = 8.dp,
                            minWidth = 40.dp,
                            maxWidth = 40.dp,
                            height = 5.dp,
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