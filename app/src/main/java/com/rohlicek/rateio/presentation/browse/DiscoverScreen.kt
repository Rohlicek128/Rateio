package com.rohlicek.rateio.presentation.browse

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.category.ItemListRow
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets


@Composable
fun <R : Comparable<R>> DiscoverScreen(
    category: CategoryType,
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    resultsSortBy: (RateItem) -> R?,
    sortBy: String = "popularity.desc",
    showNullRatings: Boolean = false,
    resultsSortOrder: SortOrder = SortOrder.ASCENDING,
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: DiscoverViewModel = viewModel(
        key = category.name + sortBy,
        factory = DiscoverViewModel.factory(category, sortBy, imdbRepository),
    )
    val state by viewModel.state.collectAsState()

    val sortedResults = remember(state.results, resultsSortBy, resultsSortOrder) {
        if (resultsSortOrder == SortOrder.DESCENDING) {
            state.results.sortedByDescending(resultsSortBy)
        } else {
            state.results.sortedBy(resultsSortBy)
        }
    }

    ItemListRow(
        modifier = modifier,
        title = title,
        items = sortedResults,
        isLoading = state.isLoading,
        showRanking = true,
        showNullRatings = showNullRatings,
        colorBucketsOverride = when (category) {
            CategoryType.TMDB_SHOWS -> RatingColorBucketConstants.RC_IMDB_SHOWS
            CategoryType.TMDB_MOVIES -> RatingColorBucketConstants.RC_IMDB_MOVIES
            else -> getCurrentRatingColorBuckets()
        },
        onItemClick = { item ->
            item.externalId?.let { id ->
                item.externalSource?.let { type ->
                    onItemClick(id, type)
                }
            }
        },
        emptyContent = {
            Text(
                "Nothing to discover :(",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}