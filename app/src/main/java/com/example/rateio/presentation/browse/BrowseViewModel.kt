package com.example.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.steam.SteamClient
import com.example.rateio.data.remote.steam.toRateItem
import com.example.rateio.data.remote.toRateItem
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


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
            delay(300)
            _state.update { it.copy(isLoading = true) }
            try {
                val results = when (_state.value.selectedCategory?.type) {
                    CategoryType.TMDB_SHOWS  -> TmdbClient.tmdb.searchShows(query)
                        .results.map { it.toRateItem() }
                    CategoryType.TMDB_MOVIES -> TmdbClient.tmdb.searchMovies(query)
                        .results.map { it.toRateItem() }
                    CategoryType.STEAM_GAMES -> SteamClient.steamCommunity.searchGames(query)
                        .map { it.toRateItem() }
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

                if (_state.value.selectedCategory?.type == CategoryType.TMDB_SHOWS ||
                    _state.value.selectedCategory?.type == CategoryType.TMDB_MOVIES
                ) {
                    val isTv = _state.value.selectedCategory?.type == CategoryType.TMDB_SHOWS

                    ratingsJob = viewModelScope.launch {
                        results.take(8).chunked(5).forEach { batch ->
                            // Step 1 — resolve IMDb ids for this batch from TMDb
                            val imdbIds = batch.mapNotNull { item ->
                                val tmdbId = item.externalId?.toIntOrNull() ?: return@mapNotNull null
                                runCatching {
                                    if (isTv) TmdbClient.tmdb.getTvExternalIds(tmdbId).imdbId
                                    else TmdbClient.tmdb.getMovieExternalIds(tmdbId).imdbId
                                }.getOrNull()?.let { item.externalId to it }
                            }.toMap()

                            if (imdbIds.isEmpty()) return@launch

                            // Step 2 — batch fetch IMDb ratings
                            val queryString = imdbIds.values.joinToString("&") { "titleIds=$it" }
                            val ratings = retryWithBackoff {
                                TmdbClient.imdb.batchGetTitles("https://api.imdbapi.dev/titles:batchGet?$queryString")
                                    .titles
                                    .associate { it.id to it.rating.normalizedRating }
                            } ?: return@launch

                            // Step 3 — update items
                            _state.update { current ->
                                val updated = current.results.toMutableList()
                                updated.forEachIndexed { index, item ->
                                    val imdbId = imdbIds[item.externalId] ?: return@forEachIndexed
                                    val rating = ratings[imdbId] ?: return@forEachIndexed
                                    updated[index] = updated[index].copy(rating = rating)
                                }
                                current.copy(results = updated)
                            }

                            delay(300L)
                        }
                    }

                }

            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    suspend fun <T> retryWithBackoff(
        times: Int = 2,
        initialDelay: Long = 750L,
        block: suspend () -> T,
    ): T? {
        repeat(times) { attempt ->
            runCatching { return block() }
            delay(initialDelay * (attempt + 1))
        }
        return null
    }

    private var searchJob: Job? = null
    private var ratingsJob: Job? = null
}