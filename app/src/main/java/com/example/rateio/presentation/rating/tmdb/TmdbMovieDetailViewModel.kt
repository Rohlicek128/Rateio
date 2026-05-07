package com.example.rateio.presentation.rating.tmdb

import com.example.rateio.data.remote.TmdbMovieDetail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbImageResponse
import com.example.rateio.data.remote.imdb.ImdbRating
import com.example.rateio.data.remote.imdb.ImdbRatingFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbMovieDetailState(
    val movie: TmdbMovieDetail? = null,
    val imdbRating: ImdbRating? = null,
    val images: TmdbImageResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbMovieDetailViewModel(id: Int) : ViewModel() {
    private val _state = MutableStateFlow(TmdbMovieDetailState())
    val state: StateFlow<TmdbMovieDetailState> = _state.asStateFlow()

    private val imdbFetcher = ImdbRatingFetcher()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val movie = TmdbClient.tmdb.getMovie(id)
                _state.update { it.copy(movie = movie, isLoading = false) }

                launch {
                    val rating = imdbFetcher.fetch(movie.imdbId)
                    _state.update { it.copy(imdbRating = rating) }
                }

                launch {
                    val images = TmdbClient.tmdb.getMovieImages(id)
                    _state.update { it.copy(images = images) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(id: Int) = viewModelFactory {
            initializer { TmdbMovieDetailViewModel(id) }
        }
    }
}