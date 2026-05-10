package com.example.rateio.presentation.rating.steam

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.steam.toCarouselImage
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.rating.RateItemDetailScreen

@Composable
fun SteamGameDetailScreen(
    appId: String,
    onBackClick: () -> Unit,
) {
    val viewModel: SteamGameDetailViewModel = viewModel(
        factory = SteamGameDetailViewModel.factory(appId)
    )
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
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
                coverImageUrl = "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/${game.steamAppid}/library_600x900_2x.jpg",
                backdropImageUrl = game.headerImage,
                rating = if (state.reviews != null && state.reviews?.totalReviews != null
                    && state.reviews?.totalPositive != null && state.reviews?.totalReviews!! >= 200)
                    state.reviews?.totalPositive?.div(state.reviews?.totalReviews?.toFloat() ?: 1f) else null,
                ratingVotes = state.reviews?.totalReviews,
                ratingLabel = state.reviews?.reviewScoreDesc,
                onBackClick = onBackClick,
                extraContent = {
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
                                baseUrl = "",
                                screenshots.sortedBy { it.id }.map { it.toCarouselImage() },
                                itemWidth = 320.dp,
                                itemHeight = 215.dp,
                                shape = MaterialTheme.shapes.large,
                            )
                        }
                    }
                }
            )
        }
    }
}