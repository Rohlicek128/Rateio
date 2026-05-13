package com.example.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbEpisodeSummary
import com.example.rateio.data.remote.TmdbRepository
import com.example.rateio.data.remote.imdb.ImdbRatingFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbEpisodesState(
    val seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>> = emptyMap(),
    val imdbRatings: Map<Int, Map<Int, Float?>> = emptyMap(),
    val isLoadingEpisodes: Boolean = false,
    val isLoadingRatings: Boolean = true,
    val error: String? = null,
)

class TmdbEpisodesViewModel(
    private val showId: Int,
    private val seasonNumbers: List<Int>,
    imdbId: String?,
    fetchRatings: Boolean,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbEpisodesState())
    val state: StateFlow<TmdbEpisodesState> = _state.asStateFlow()

    private val repository = TmdbRepository()
    private val imdbFetcher = ImdbRatingFetcher()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEpisodes = true) }
            try {
                val episodes = repository.getAllEpisodes(showId, seasonNumbers)
                _state.update { it.copy(seasonEpisodes = episodes, isLoadingEpisodes = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoadingEpisodes = false) }
            }
        }

        if (fetchRatings) {
            fetchImdbRatings(imdbId)
        } else {
            _state.update { it.copy(isLoadingRatings = false) }
        }
    }

    fun fetchImdbRatings(imdbId: String?) {
        if (imdbId != null) {
            viewModelScope.launch {
                imdbFetcher
                    .ratingsForShow(imdbId, seasonNumbers)
                    .collect { ratings ->
                        _state.update { current ->
                            current.copy(
                                imdbRatings = ratings,
                                isLoadingRatings = ratings.size < seasonNumbers.size,
                            )
                        }
                    }
            }
        } else {
            _state.update { it.copy(isLoadingRatings = false) }
        }
    }

    companion object {
        fun factory(showId: Int, seasonNumbers: List<Int>, imdbId: String?, fetchRatings: Boolean) = viewModelFactory {
            initializer { TmdbEpisodesViewModel(showId, seasonNumbers, imdbId, fetchRatings) }
        }
    }
}