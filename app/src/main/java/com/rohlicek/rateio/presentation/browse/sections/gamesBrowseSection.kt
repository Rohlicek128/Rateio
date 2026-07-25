package com.rohlicek.rateio.presentation.browse.sections

import androidx.compose.foundation.lazy.LazyListScope
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.browse.DiscoverScreen


fun LazyListScope.gamesBrowseSection(
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
) {
    item {
        DiscoverScreen(
            title = "Popular",
            sortBy = "popularity.desc",
            category = CategoryType.STEAM_GAMES,
            onItemClick = onItemClick,
            resultsSortBy = { null },
        )
    }
}