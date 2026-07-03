package com.example.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.tmdb.TmdbClient
import com.example.rateio.data.remote.steam.SteamClient
import com.example.rateio.data.remote.steam.toRateItem
import com.example.rateio.data.remote.tmdb.toRateItem
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
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


                if (category.type == CategoryType.STEAM_GAMES) {
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
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(category: Category, sortBy: String) = viewModelFactory {
            initializer { DiscoverViewModel(category, sortBy) }
        }
    }
}