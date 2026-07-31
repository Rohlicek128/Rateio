package com.rohlicek.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.remote.openlibrary.OpenLibraryClient
import com.rohlicek.rateio.data.remote.openlibrary.toRateItem
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.steam.SteamClient
import com.rohlicek.rateio.data.remote.steam.toRateItem
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


data class BrowseState(
    val availableCategories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val query: String = "",
    val results: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class BrowseViewModel : ViewModel() {

    private val _state = MutableStateFlow(BrowseState())
    val state: StateFlow<BrowseState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Only show API-backed categories in browse
            val apiCategories = CategoryRegistry.all.filter {
                it.type != CategoryType.CUSTOM
            }
            _state.update { it.copy(
                availableCategories = apiCategories,
                selectedCategory = apiCategories.firstOrNull(),
            )}
        }
    }

    fun onCategorySelected(category: Category) {
        _state.update { it.copy(selectedCategory = category, results = emptyList(), query = "") }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        ratingsJob?.cancel()
        if (query.isBlank()) { _state.update { it.copy(results = emptyList()) }; return }

        searchJob = viewModelScope.launch {
            delay(300.milliseconds)
            _state.update { it.copy(isLoading = true) }
            try {
                val results = when (_state.value.selectedCategory?.type) {
                    CategoryType.TMDB_SHOWS  -> RateioApplication.instance.tmdbClient.tmdb.searchShows(query)
                        .results.map { it.toRateItem() }
                    CategoryType.TMDB_MOVIES -> RateioApplication.instance.tmdbClient.tmdb.searchMovies(query)
                        .results.map { it.toRateItem() }
                    CategoryType.STEAM_GAMES -> SteamClient.steamCommunity.searchGames(query)
                        .map { it.toRateItem() }
                    CategoryType.OPEN_LIBRARY_BOOKS -> OpenLibraryClient.service.searchWorks(query)
                        .docs.map { it.toRateItem() }
                    else -> emptyList()
                }
                _state.update { it.copy(
                    results = results,
                    isLoading = false,
                )}


                if (_state.value.selectedCategory?.type == CategoryType.STEAM_GAMES) {
                    results.mapIndexed { index, item ->
                        launch {
                            val rating = runCatching {
                                SteamClient.steamStore.getGameReviews(item.externalId ?: "")
                                    .querySummary?.normalizedRating
                            }.getOrNull()

                            _state.update { current ->
                                val updated = current.results.toMutableList()
                                if (index < updated.size) updated[index] = updated[index].copy(rating = rating)
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

    private var searchJob: Job? = null
    private var ratingsJob: Job? = null
}