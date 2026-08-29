package com.rohlicek.rateio.presentation.rating

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.rohlicek.rateio.data.remote.tmdb.TmdbRepository
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.rating.openlibrary.OLWorkDetailScreen
import com.rohlicek.rateio.presentation.rating.steam.SteamGameDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbEpisodeDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbMovieDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbShowDetailScreen
import com.rohlicek.rateio.utils.formatDate
import com.rohlicek.rateio.utils.parseDate
import kotlinx.serialization.json.Json


@Composable
fun SavedRateItemScreen(
    itemId: Long,
    tmdbRepository: TmdbRepository,
    onChildClick: (childId: Long, parentId: Long) -> Unit,
    onPersonClick: (Int) -> Unit,
    onListClick: (Int) -> Unit,
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
        factory = SavedRateItemViewModel.factory(itemId, itemRepository, categoryRepository, tmdbRepository)
    )
    val state by viewModel.state.collectAsState()


    when {
        state.isLoading -> {
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
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
                        savedRank = state.itemRank,
                        onRatingSaved = viewModel::saveRating,
                        onWeightSaved = viewModel::updateWeight,
                        onStatusSaved = viewModel::updateStatus,
                        onCoverOverrideSaved = viewModel::updateCoverOverride,
                        onMetadataSaved = viewModel::updateMetadata,
                        onPersonClick = onPersonClick,
                        onBackClick = onBackClick,
                        onEpisodeClick = { seasonItem, episodeItem, saveRatings ->
                            viewModel.findOrCreateChildAndNavigate(
                                parentItem = seasonItem,
                                childItem = episodeItem,
                                saveRatings = saveRatings,
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
                            savedRank = state.itemRank,
                            onRatingSaved = viewModel::saveRatingAndComplete,
                            seasonEpisodeCount = metadata.seasonEpisodeCount,
                            onPersonClick = onPersonClick,
                            onBackClick = onBackClick,
                            onNextClick = { nextSeason, nextEpisode, nextEpisodeCount ->
                                viewModel.findOrCreateEpisodeAndNavigate(
                                    showId = metadata.showId,
                                    seasonNumber = nextSeason,
                                    episodeNumber = nextEpisode,
                                    seasonEpisodeCount = nextEpisodeCount,
                                    onNavigate = onChildClick,
                                )
                            },
                            onPreviousClick = { prevSeason, prevEpisode, prevEpisodeCount ->
                                viewModel.findOrCreateEpisodeAndNavigate(
                                    showId = metadata.showId,
                                    seasonNumber = prevSeason,
                                    episodeNumber = prevEpisode,
                                    seasonEpisodeCount = prevEpisodeCount,
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
                        savedRank = state.itemRank,
                        onRatingSaved = viewModel::saveRatingAndComplete,
                        onStatusSaved = viewModel::updateStatus,
                        onCoverOverrideSaved = viewModel::updateCoverOverride,
                        onPersonClick = onPersonClick,
                        onListClick = onListClick,
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

                CategoryType.OPEN_LIBRARY_BOOKS -> {
                    OLWorkDetailScreen (
                        workId = item.externalId!!,
                        isSaved = true,
                        customRating = item.rating,
                        onRatingSaved = viewModel::saveRatingAndComplete,
                        onStatusSaved = viewModel::updateStatus,
                        onCoverOverrideSaved = viewModel::updateCoverOverride,
                        onMetadataSaved = viewModel::updateMetadata,
                        onBackClick = onBackClick,
                        onChapterClick = { partItem, chapterItem ->
                            viewModel.findOrCreateChildAndNavigate(
                                parentItem = partItem,
                                childItem = chapterItem,
                                onNavigate = onChildClick,
                            )
                        }
                    )
                }
                CategoryType.OPEN_LIBRARY_CHAPTER -> {
                    EditableRateItemDetailScreen(
                        item = item,
                        categoryName = "Chapters",
                        onItemUpdate = viewModel::updateItem,
                        onRatingSaved = viewModel::saveRatingAndComplete,
                        onBackClick = onBackClick,
                    )
                }
                else -> {
                    RateItemDetailScreen(
                        title = item.title,
                        subtitle = item.subtitle,
                        categoryName = category?.name,
                        description = null,
                        coverImageUrl = item.coverImageUrl,
                        backdropImageUrl = null,
                        placeholderRatio = 2f / 3f,
                        rating = item.rating,
                        onBackClick = onBackClick,
                        extraContent = { },
                        onRatingSaved = viewModel::saveRating,
                        onOpenSettings = { },
                        debug = "${item.id}, ${item.parentId}, ${item.externalId}, ${item.externalSource}," +
                                " ${formatDate(parseDate(item.updatedAt))}," +
                                " ${formatDate(parseDate(item.createdAt))}, ${item.metadataJSON}",
                    )
                }
            }

        }
    }

}