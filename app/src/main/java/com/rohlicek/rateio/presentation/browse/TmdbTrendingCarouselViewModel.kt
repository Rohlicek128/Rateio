package com.rohlicek.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.TmdbTimeWindow
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TrendingCarouselState(
    val results: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbTrendingCarouselViewModel(
    category: CategoryType,
    private val imdbRepository: ImdbRatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TrendingCarouselState())
    val state: StateFlow<TrendingCarouselState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                if (category != CategoryType.TMDB_SHOWS && category != CategoryType.TMDB_MOVIES) return@launch

                val results = when (category) {
                    CategoryType.TMDB_SHOWS -> TmdbClient.tmdb.trendingShows(
                        TmdbTimeWindow.WEEK.displayName.lowercase()
                    ).results.map { show ->
                        RateItem(
                            id = 0,
                            categoryId = 0,
                            title = show.name,
                            subtitle = show.firstAirDate?.take(4),
                            coverImageUrl = show.backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
                            coverImageLowUrl = show.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" },
                            rating = show.voteAverage?.div(10f),
                            externalId = show.id.toString(),
                            externalSource = CategoryType.TMDB_SHOWS,
                        )
                    }

                    CategoryType.TMDB_MOVIES -> TmdbClient.tmdb.trendingMovies(
                        TmdbTimeWindow.WEEK.displayName.lowercase()
                    ).results.map { movie ->
                        RateItem(
                            id = 0,
                            categoryId = 0,
                            title = movie.title,
                            subtitle = movie.releaseDate?.take(4),
                            coverImageUrl = movie.backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
                            coverImageLowUrl = movie.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" },
                            rating = movie.voteAverage?.div(10f),
                            externalId = movie.id.toString(),
                            externalSource = CategoryType.TMDB_MOVIES,
                        )
                    }
                    else -> emptyList()
                }
                _state.update { it.copy(results = results, isLoading = false) }


                results.mapIndexed { index, item ->
                    if (item.externalId != null) {
                        launch {
                            val cachedRating = imdbRepository.getRatingByTmdbId(item.externalId.toInt())
                            val rating = if (cachedRating != null)
                                cachedRating
                            else {
                                val imdbId = if (category == CategoryType.TMDB_MOVIES)
                                    TmdbClient.tmdb.getMovieExternalIds(item.externalId.toInt()).imdbId
                                else TmdbClient.tmdb.getShowExternalIds(item.externalId.toInt()).imdbId
                                imdbRepository.linkImdbToTmdb(imdbId, item.externalId.toInt())
                                imdbRepository.getRatingByImdbId(imdbId)
                            }

                            _state.update { current ->
                                val updated = current.results.toMutableList()
                                if (index < updated.size) updated[index] = updated[index].copy(
                                    rating = rating?.averageRating,
                                    ratingWeight = rating?.numVotes?.toFloat() ?: 0f
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
        fun factory(category: CategoryType, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbTrendingCarouselViewModel(category, imdbRepository) }
        }
    }
}