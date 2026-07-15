package com.example.rateio.presentation.rating.steam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.remote.steam.toCarouselImage
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.ImageSize
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.ScreenError
import com.example.rateio.presentation.components.ScreenLoading
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen

@Composable
fun SteamGameDetailScreen(
    appId: String,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }
    val categoryRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        CategoryRepository(db.categoryDao())
    }

    val viewModel: SteamGameDetailViewModel = viewModel(
        factory = SteamGameDetailViewModel.factory(appId, categoryRepository, itemRepository)
    )
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
        }
        state.game != null -> {
            val game = state.game!!

            RateItemDetailScreen(
                title = game.name,
                subtitle = buildString {
                    game.releaseDate?.date.let { append(it) }
                    if (game.priceOverview != null && game.priceOverview.finalFormatted != null) {
                        append("  |  ${game.priceOverview.finalFormatted}")
                        if (game.priceOverview.discountPercent > 0) {
                            append(" (-${game.priceOverview.discountPercent}%)")
                        }
                    }
                }.ifBlank { null },
                categoryName = CategoryRegistry.forType(CategoryType.STEAM_GAMES)?.name,
                description = game.shortDescription,
                coverImageUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/${game.steamAppId}/library_600x900_2x.jpg",
                backdropImageUrl = game.headerImage,
                rating = if (!isSaved) {
                    if (state.reviews != null && state.reviews?.totalReviews != null
                        && state.reviews?.totalPositive != null && state.reviews?.totalReviews!! >= 200)
                        state.reviews?.totalPositive?.div(state.reviews?.totalReviews?.toFloat() ?: 1f) else null
                } else customRating,
                ratingVotes = if (!isSaved) state.reviews?.totalReviews else null,
                //ratingLabel = state.reviews?.reviewScoreDesc,
                onRatingSaved = onRatingSaved,
                onBackClick = onBackClick,
                canAddToLibrary = false,
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LibraryToggle(
                                checked = state.savedItemId != null,
                                onCheckedChange = {
                                    viewModel.onToggleSaved(state.game!!)
                                },
                                itemName = game.name,
                            )
                        }
                    }


                    // Genres
                    if (game.genres?.isNotEmpty() ?: false) {
                        item {
                            GenreChips(
                                genres = game.genres.map { it.description },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }


                    // Screenshots
                    game.screenshots?.takeIf { it.isNotEmpty() }?.let { screenshots ->
                        item { SectionHeader("Screenshots") }
                        item {
                            AdaptiveImageCarousel(
                                urlBuilder = { _, path ->
                                    path
                                },
                                screenshots.sortedBy { it.id }.map { it.toCarouselImage() },
                                itemWidth = 320.dp,
                                itemHeight = 215.dp,
                                shape = MaterialTheme.shapes.large,
                                maximizable = true,
                            )
                        }
                    }
                }
            )
        }
    }
}