package com.example.rateio.presentation.rating

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.remote.TmdbEpisodeMetadata
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.navigation.Route
import com.example.rateio.presentation.rating.steam.SteamGameDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbEpisodeDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbMovieDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbShowDetailScreen
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass


@Composable
fun SavedRateItemScreen(
    itemId: Long,
    onChildClick: (childId: Long, parentId: Long) -> Unit,
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
    val viewModel: SavedRateItemViewModel = viewModel(
        factory = SavedRateItemViewModel.factory(itemId, itemRepository, categoryRepository)
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
        state.item != null -> {
            val item = state.item!!
            val category = state.category

            val type = if (!item.externalId.isNullOrBlank() && item.externalSource != null)
                item.externalSource else CategoryType.CUSTOM

            when (type) {
                CategoryType.TMDB_SHOWS -> {
                    TmdbShowDetailScreen(
                        showId = item.externalId!!.toInt(),
                        isSaved = true,
                        customRating = item.rating,
                        onRatingSaved = viewModel::saveRating,
                        onStatusSaved = viewModel::updateStatus,
                        onMetadataSaved = viewModel::updateMetadata,
                        onBackClick = onBackClick,
                        onEpisodeClick = {showId, seasonNumber, episodeNumber ->
                            viewModel.findOrCreateEpisodeAndNavigate(
                                showId = showId,
                                seasonNumber = seasonNumber,
                                episodeNumber = episodeNumber,
                                onNavigate = onChildClick,
                            )
                        }
                    )
                }
                CategoryType.TMDB_EPISODES -> {
                    val metadata = item.metadataJSON?.let {
                            runCatching {
                                Json.decodeFromString<TmdbEpisodeMetadata>(it)
                            }.getOrNull()
                        }

                    if (metadata != null) {
                        TmdbEpisodeDetailScreen(
                            showId = metadata.showId,
                            season = metadata.seasonNumber,
                            episode = metadata.episodeNumber,
                            //debug = "  |  ${item.id}, ${item.parentId}",
                            isSaved = true,
                            customRating = item.rating,
                            onRatingSaved = viewModel::saveRatingAndComplete,
                            onBackClick = onBackClick,
                            onNextClick = { nextSeason, nextEpisode ->
                                viewModel.findOrCreateEpisodeAndNavigate(
                                    showId = metadata.showId,
                                    seasonNumber = nextSeason,
                                    episodeNumber = nextEpisode,
                                    onNavigate = onChildClick,
                                )
                            },
                            onPreviousClick = { prevSeason, prevEpisode ->
                                viewModel.findOrCreateEpisodeAndNavigate(
                                    showId = metadata.showId,
                                    seasonNumber = prevSeason,
                                    episodeNumber = prevEpisode,
                                    onNavigate = onChildClick,
                                )
                            },
                        )
                    }
                }
                CategoryType.TMDB_MOVIES -> {
                    TmdbMovieDetailScreen(
                        movieId = item.externalId!!.toInt(),
                        isSaved = true,
                        customRating = item.rating,
                        onRatingSaved = viewModel::saveRatingAndComplete,
                        onStatusSaved = viewModel::updateStatus,
                        onBackClick = onBackClick,
                    )
                }
                CategoryType.STEAM_GAMES -> {
                    SteamGameDetailScreen(
                        appId = item.externalId!!,
                        isSaved = true,
                        customRating = item.rating,
                        onRatingSaved = viewModel::saveRating,
                        onBackClick = onBackClick,
                    )
                }
                else -> {
                    RateItemDetailScreen(
                        title = item.title,
                        subtitle = item.subtitle,
                        categoryName = category?.name,
                        description = "${item.externalId}, ${item.externalSource}, ${item.updatedAt}, ${item.createdAt}",
                        coverImageUrl = item.coverImageUrl,
                        backdropImageUrl = null,
                        placeholderRatio = 2f / 3f,
                        rating = item.rating,
                        onBackClick = onBackClick,
                        canAddToLibrary = true,
                        extraContent = { },
                        onRatingSaved = viewModel::saveRating,
                        onOpenSettings = { }
                    )
                }
            }

        }
    }

}