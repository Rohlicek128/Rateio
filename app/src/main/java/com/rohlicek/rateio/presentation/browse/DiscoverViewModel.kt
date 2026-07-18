package com.rohlicek.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.steam.SteamClient
import com.rohlicek.rateio.data.remote.steam.toRateItem
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class DiscoverState(
    val results: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class DiscoverViewModel(
    category: Category,
    sortBy: String,
    private val imdbRepository: ImdbRatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val results = when (category.type) {
                    CategoryType.TMDB_SHOWS -> TmdbClient.tmdb.discoverShows(sortBy = sortBy)
                        .results.map { it.toRateItem() }
                    CategoryType.TMDB_MOVIES -> TmdbClient.tmdb.discoverMovies(sortBy = sortBy)
                        .results.map { it.toRateItem() }
                    CategoryType.STEAM_GAMES -> SteamClient.steamApi.getMostPlayedGames()
                        .response.ranks.sortedByDescending { it.peakInGame }.map { it.toRateItem() }
                    else -> emptyList()
                }
                _state.update { it.copy(results = results, isLoading = false) }

                when {
                    category.type == CategoryType.TMDB_MOVIES || category.type == CategoryType.TMDB_SHOWS -> {
                        results.mapIndexed { index, item ->
                            if (item.externalId != null) {
                                launch {
                                    val cachedRating = imdbRepository.getRatingByTmdbId(item.externalId.toInt())?.averageRating
                                    val rating = if (cachedRating != null) cachedRating
                                    else {
                                        val imdbId = if (category.type == CategoryType.TMDB_MOVIES)
                                            TmdbClient.tmdb.getMovieExternalIds(item.externalId.toInt()).imdbId
                                        else TmdbClient.tmdb.getShowExternalIds(item.externalId.toInt()).imdbId
                                        imdbRepository.getRatingByImdbId(imdbId)?.averageRating
                                    }

                                    _state.update { current ->
                                        val updated = current.results.toMutableList()
                                        if (index < updated.size) updated[index] = updated[index].copy(
                                            rating = rating
                                        )
                                        current.copy(results = updated)
                                    }
                                }
                            }
                        }
                    }
                    category.type == CategoryType.STEAM_GAMES -> {
                        results.mapIndexed { index, item ->
                            launch {
                                val rating = runCatching {
                                    SteamClient.steamStore.getGameReviews(item.externalId ?: "")
                                        .querySummary?.normalizedRating
                                }.getOrNull()
                                _state.update { current ->
                                    val updated = current.results.toMutableList()
                                    if (index < updated.size) updated[index] = updated[index].copy(
                                        rating = rating
                                    )
                                    current.copy(results = updated)
                                }
                            }
                            launch {
                                val details = runCatching {
                                    SteamClient.steamStore.getGames(item.externalId ?: "", filters = "basic")[item.externalId]
                                }.getOrNull()?.data

                                _state.update { current ->
                                    val updated = current.results.toMutableList()
                                    if (index < updated.size) updated[index] = updated[index].copy(
                                        title = details?.name ?: updated[index].title
                                    )
                                    current.copy(results = updated)
                                }
                            }
                        }
                    }
                    else -> {

                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(category: Category, sortBy: String, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { DiscoverViewModel(category, sortBy, imdbRepository) }
        }
    }
}