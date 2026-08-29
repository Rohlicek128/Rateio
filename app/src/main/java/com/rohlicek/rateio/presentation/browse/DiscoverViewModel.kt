package com.rohlicek.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.steam.SteamClient
import com.rohlicek.rateio.data.remote.steam.toRateItem
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate


data class DiscoverState(
    val results: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class DiscoverViewModel(
    category: CategoryType,
    sortBy: String,
    private val imdbRepository: ImdbRatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val today = LocalDate.now()

                val results = when (category) {
                    CategoryType.TMDB_SHOWS -> RateioApplication.instance.tmdbClient.tmdb.discoverShows(
                        sortBy = sortBy,
                        minVoteAverage = 6.0,
                        airDateGte = today.minusMonths(6).toString(),
                        airDateLte = today.toString(),
                    ).results.map { it.toRateItem() }
                    CategoryType.TMDB_MOVIES -> RateioApplication.instance.tmdbClient.tmdb.discoverMovies(
                        sortBy = sortBy,
                        minVoteAverage = 5.5,
                        releaseDateGte = today.minusMonths(6).toString(),
                        releaseDateLte = today.toString(),
                    ).results.map { it.toRateItem() }
                    CategoryType.STEAM_GAMES -> SteamClient.steamApi.getMostPlayedGames()
                        .response.ranks.sortedByDescending { it.peakInGame }.map { it.toRateItem() }
                    else -> emptyList()
                }
                _state.update { it.copy(results = results, isLoading = false) }

                when (category) {
                    CategoryType.TMDB_MOVIES, CategoryType.TMDB_SHOWS -> {
                        results.mapIndexed { index, item ->
                            if (item.externalId != null) {
                                launch {
                                    val cachedRating = imdbRepository.getRatingByTmdbId(item.externalId.toInt())?.averageRating
                                    val rating = if (cachedRating != null) cachedRating
                                    else {
                                        val imdbId = if (category == CategoryType.TMDB_MOVIES)
                                            RateioApplication.instance.tmdbClient.tmdb.getMovieExternalIds(item.externalId.toInt()).imdbId
                                        else RateioApplication.instance.tmdbClient.tmdb.getShowExternalIds(item.externalId.toInt()).imdbId
                                        imdbRepository.linkImdbToTmdb(imdbId, item.externalId.toInt())
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
                    CategoryType.STEAM_GAMES -> {
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
                    else -> {}
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(category: CategoryType, sortBy: String, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { DiscoverViewModel(category, sortBy, imdbRepository) }
        }
    }
}