package com.example.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbShowDetail
import com.example.rateio.data.remote.TmdbShowImageResponse
import com.example.rateio.data.remote.imdb.ImdbRating
import com.example.rateio.data.remote.imdb.ImdbRatingFetcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbShowDetailState(
    val show: TmdbShowDetail? = null,
    val imdbRating: ImdbRating? = null,
    val images: TmdbShowImageResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbShowDetailViewModel(showId: Int) : ViewModel() {
    private val _state = MutableStateFlow(TmdbShowDetailState())
    val state: StateFlow<TmdbShowDetailState> = _state.asStateFlow()

    private val imdbFetcher = ImdbRatingFetcher()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val show = TmdbClient.tmdb.getShow(showId)
                _state.update { it.copy(show = show, isLoading = false) }

                launch {
                    val rating = imdbFetcher.fetch(show.externalIds?.imdbId)
                    _state.update { it.copy(imdbRating = rating) }
                }

                launch {
                    val images = TmdbClient.tmdb.getShowImages(showId)
                    _state.update { it.copy(images = images) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(showId: Int) = viewModelFactory {
            initializer { TmdbShowDetailViewModel(showId) }
        }
    }
}