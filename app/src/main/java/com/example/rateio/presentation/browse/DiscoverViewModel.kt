package com.example.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbService
import com.example.rateio.data.remote.steam.SteamClient
import com.example.rateio.data.remote.steam.toRateItem
import com.example.rateio.data.remote.toRateItem
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
    //val resultsRatings: List<Float?> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class DiscoverViewModel(
    category: Category,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverState())
    val state: StateFlow<DiscoverState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val results = when (category.type) {
                    CategoryType.TMDB_SHOWS -> TmdbClient.tmdb.discoverShows()
                        .results.map { it.toRateItem() }
                    CategoryType.TMDB_MOVIES -> TmdbClient.tmdb.discoverMovies()
                        .results.map { it.toRateItem() }
                    CategoryType.STEAM_GAMES -> SteamClient.steamApi.getMostPlayedGames()
                        .response.ranks.sortedByDescending { it.peakInGame }.map { it.toRateItem() }
                    else -> emptyList()
                }
                _state.update { it.copy(results = results, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(category: Category) = viewModelFactory {
            initializer { DiscoverViewModel(category) }
        }
    }
}