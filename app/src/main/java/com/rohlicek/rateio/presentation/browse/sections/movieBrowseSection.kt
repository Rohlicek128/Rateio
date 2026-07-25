package com.rohlicek.rateio.presentation.browse.sections

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.browse.DiscoverScreen
import com.rohlicek.rateio.presentation.browse.TmdbTrendingCarousel
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem

fun LazyListScope.movieBrowseSection(
    onTopRatedClick: (type: CategoryType) -> Unit,
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
) {
    item {
        TmdbTrendingCarousel(
            category = CategoryType.TMDB_MOVIES,
            onItemClick = onItemClick,
        )
    }

    item {
        DiscoverScreen(
            title = "Popular",
            sortBy = "popularity.desc",
            category = CategoryType.TMDB_MOVIES,
            onItemClick = onItemClick,
            resultsSortBy = { null },
        )
    }

    item {
        DiscoverScreen(
            title = "Recent Best",
            sortBy = "vote_count.desc",
            category = CategoryType.TMDB_MOVIES,
            onItemClick = onItemClick,
            resultsSortBy = { it.rating },
            resultsSortOrder = SortOrder.DESCENDING,
        )
    }

    item {
        SettingListItem(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            title = "Top Rated",
            description = "Leaderboard of ${CategoryType.TMDB_MOVIES.displayName}",
            icon = Icons.Default.Leaderboard,
            position = ListItemPosition.SINGLE,
            onClick = { onTopRatedClick(CategoryType.TMDB_MOVIES) },
        )
    }
}