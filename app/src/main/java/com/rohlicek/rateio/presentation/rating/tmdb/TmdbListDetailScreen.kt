package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.components.ConnectedButtonsExpressive
import com.rohlicek.rateio.presentation.components.ModalEpisodeGroupsSelector
import com.rohlicek.rateio.presentation.components.OrderButton
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.components.statistics.ExternalRatingStatCard
import com.rohlicek.rateio.presentation.components.statistics.ItemStatCard
import com.rohlicek.rateio.presentation.leaderboard.RatingData
import com.rohlicek.rateio.presentation.rating.RateItemDetailScreen
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.RatingTransformationsConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.utils.formatCompact


@Composable
fun TmdbListDetailScreen(
    listId: Int,
    onItemClick: (tmdbId: Int) -> Unit,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: TmdbListDetailViewModel = viewModel(
        factory = TmdbListDetailViewModel.factory(listId, imdbRepository)
    )
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
        }
        state.list != null -> {
            val list = state.list!!

            val ratings: Map<Int, RatingData> = when (state.selectedRatingsSource) {
                RatingsSource.TMDB -> state.tmdbRatings
                RatingsSource.IMDB -> state.imdbRatings
                else -> emptyMap()
            }

            RateItemDetailScreen(
                title = list.name ?: "N/A",
                subtitle = "by ${list.createdBy}",
                categoryName = "Lists",
                description = list.description,
                coverImageUrl = list.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                } ?: "",
                placeholderRatio = 2f / 3f,
                backdropImageUrl = list.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                showNullRating = false,
                rating = null,
                onBackClick = onBackClick,
                headerExtraContent = {
                    //Stats
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            if (list.favoriteCount != null && list.itemCount != null) {
                                ItemStatCard(
                                    header = "Favorite",
                                    statistic = list.favoriteCount.toString(),
                                )

                                ItemStatCard(
                                    header = "Runtime",
                                    statistic = list.itemCount.toString(),
                                )
                            }
                        }
                    }
                },
                extraContent = {

                    // Ratings
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            ExternalRatingStatCard(
                                rating = null,
                                votes = 0,
                                source = "TMDB",
                                transformationOverride = RatingTransformationsConstants.TF_PERCENTAGE,
                                colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_EPISODES,
                            )
                            ExternalRatingStatCard(
                                rating = null,
                                votes = null,
                                source = "Yours",
                                showNullVotes = false,
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(10.dp)) }

                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ConnectedButtonsExpressive(
                                itemSpacing = 3.dp,
                                selectedIndex = RatingsSource.entries.indexOf(state.selectedRatingsSource),
                                onSelectionChanged = {
                                    viewModel.onSelectRatings(RatingsSource.entries[it])
                                },
                                options = RatingsSource.entries.map { it.displayName },
                            )
                        }
                    }

                    itemsIndexed(
                        items = list.items,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        val rating = ratings[item.id]
                        RateItemCard(
                            title = item.title ?: "N/A",
                            subtitle = item.releaseDate?.take(4),
                            overlineText = if (rating?.votes != null) {
                                "${formatCompact(rating.votes.toLong())} VOTES"
                            } else null,
                            coverImagePath = item.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
                            rating = rating?.rating,
                            placeholderRatio = 2f / 3f,
                            padding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            //rank = index + 1,
                            //rankHighlighted = item.externalId?.let { state.completedItems.contains(it) } ?: false,
                            colorBucketsOverride = when (CategoryType.TMDB_MOVIES) {
                                CategoryType.TMDB_MOVIES -> RatingColorBucketConstants.RC_IMDB_MOVIES
                                CategoryType.TMDB_SHOWS -> RatingColorBucketConstants.RC_IMDB_SHOWS
                                else -> getCurrentRatingColorBuckets()
                            },
                            onClick = { onItemClick(item.id) },
                        )
                    }
                }
            )
        }
    }
}