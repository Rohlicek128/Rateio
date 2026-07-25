package com.rohlicek.rateio.presentation.browse

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.components.HeroCarousel
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.utils.formatCompact


@Composable
fun TmdbTrendingCarousel(
    modifier: Modifier = Modifier,
    category: CategoryType,
    padding: PaddingValues = PaddingValues(16.dp),
    onItemClick: (externalId: String, type: CategoryType) -> Unit
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }
    val viewModel: TmdbTrendingCarouselViewModel = viewModel(
        key = category.name,
        factory = TmdbTrendingCarouselViewModel.factory(category, imdbRepository),
    )
    val state by viewModel.state.collectAsState()

    HeroCarousel(
        modifier = modifier,
        padding = padding,
        items = state.results,
        subtitleBuilder = { if (it.ratingWeight > 0f) "${formatCompact(it.ratingWeight.toLong())} Votes" else null },
        isLoading = state.isLoading,
        autoScroll = true,
        dotIndicator = true,
        colorBucketsOverride = when (category) {
            CategoryType.TMDB_SHOWS -> RatingColorBucketConstants.RC_IMDB_SHOWS
            CategoryType.TMDB_MOVIES -> RatingColorBucketConstants.RC_IMDB_MOVIES
            else -> getCurrentRatingColorBuckets()
        },
        onItemClick = {
            if (it.externalId != null) {
                onItemClick(it.externalId, category)
            }
        },
    )
}