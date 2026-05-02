package com.example.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbEpisodeDetail
import com.example.rateio.data.remote.TmdbEpisodeImageResponse
import com.example.rateio.data.remote.imdb.ImdbRatingFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbEpisodeDetailState(
    val episode: TmdbEpisodeDetail? = null,
    val imdbRating: Float? = null,
    val images: TmdbEpisodeImageResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbEpisodeDetailViewModel(showId: Int, seasonNumber: Int, episodeNumber: Int) : ViewModel() {
    private val _state = MutableStateFlow(TmdbEpisodeDetailState())
    val state: StateFlow<TmdbEpisodeDetailState> = _state.asStateFlow()

    private val imdbFetcher = ImdbRatingFetcher()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val episode = TmdbClient.tmdb.getEpisode(showId, seasonNumber, episodeNumber)
                _state.update { it.copy(episode = episode, isLoading = false) }

                launch {
                    val rating = imdbFetcher.fetch(episode.externalIds?.imdbId)
                    _state.update { it.copy(imdbRating = rating) }
                }

                launch {
                    val images = TmdbClient.tmdb.getEpisodeImages(showId, seasonNumber, episodeNumber)
                    _state.update { it.copy(images = images) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(showId: Int, seasonNumber: Int, episodeNumber: Int) = viewModelFactory {
            initializer { TmdbEpisodeDetailViewModel(showId, seasonNumber, episodeNumber) }
        }
    }
}