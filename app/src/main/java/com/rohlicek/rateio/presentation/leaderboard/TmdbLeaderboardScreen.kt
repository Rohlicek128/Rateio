package com.rohlicek.rateio.presentation.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.ConnectedItemSelector
import com.rohlicek.rateio.presentation.components.ExpressiveScrollBar
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.tmdb.RatingsSource
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.utils.formatCompact


@Composable
fun TmdbLeaderboardScreen(
    category: CategoryType,
    onItemClick: (tmdbId: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: TmdbLeaderboardViewModel = viewModel(
        key = category.name,
        factory = TmdbLeaderboardViewModel.factory(category, imdbRepository),
    )
    val state by viewModel.state.collectAsState()

    //val haptic = LocalHapticFeedback.current

    val ratings: Map<Int, RatingData> = when (state.selectedRatingsSource) {
        RatingsSource.TMDB -> state.tmdbRatings
        RatingsSource.IMDB -> state.imdbRatings
        else -> emptyMap()
    }

    val listState = rememberLazyListState()

    ScreenScaffold(
        title = "Top Rated ${category.displayName}",
        onBackClick = onBackClick,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(start = 18.dp, end = 28.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = padding,
                state = listState,
            ) {
                item {
                    SettingListItem(
                        title = "Ratings Source",
                        description = "Select from which source will the ratings be from.",
                        icon = Icons.Default.CloudDownload,
                        position = ListItemPosition.SINGLE,
                        supportingContent = {
                            ConnectedItemSelector(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                selectedIndex = RatingsSource.entries.indexOf(state.selectedRatingsSource),
                                onSelectionChanged = {
                                    viewModel.onSelectRatings(RatingsSource.entries[it])
                                },
                                options = RatingsSource.entries.take(2).map { it.displayName },
                            )
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                when {
                    state.isLoading -> {
                        item { ScreenLoading() }
                    }
                    state.error != null -> {
                        item { ScreenError(state.error) }
                    }
                    else -> {
                        itemsIndexed(
                            items = state.results.sortedWith(
                                compareByDescending<RateItem, Float?>(nullsFirst()) {
                                    it.externalId?.toInt()?.let { id -> ratings[id]?.rating }
                                }.thenByDescending(nullsFirst()) { it.externalId?.toInt()?.let { id -> ratings[id]?.votes } }
                            ),
                            key = { _, item -> item.externalId!!.toInt() }
                        ) { index, item ->
                            val rating = item.externalId?.toInt()?.let { ratings[it] }
                            RateItemCard(
                                title = item.title,
                                subtitle = item.subtitle,
                                overlineText = if (rating?.votes != null) {
                                    "${formatCompact(rating.votes.toLong())} VOTES"
                                } else null,
                                coverImagePath = item.coverImageLowUrl,
                                rating = rating?.rating,
                                placeholderRatio = 2f / 3f,
                                padding = PaddingValues(vertical = 4.dp),
                                rank = index + 1,
                                colorBucketsOverride = when (category) {
                                    CategoryType.TMDB_MOVIES -> RatingColorBucketConstants.RC_IMDB_MOVIES
                                    CategoryType.TMDB_SHOWS -> RatingColorBucketConstants.RC_IMDB_SHOWS
                                    else -> getCurrentRatingColorBuckets()
                                },
                                onClick = { onItemClick(item.externalId?.toInt() ?: 0) },
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            ExpressiveScrollBar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = padding.calculateTopPadding() + 16.dp, bottom = 42.dp),
                listState = listState,
            )
        }
    }
}