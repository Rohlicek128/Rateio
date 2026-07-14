package com.example.rateio.presentation.rating.tmdb

import androidx.compose.runtime.mutableStateSetOf
import com.example.rateio.data.remote.tmdb.TmdbMovieDetail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.ImdbRatingEntity
import com.example.rateio.data.remote.imdb.ImdbRatingRepository
import com.example.rateio.data.remote.tmdb.TmdbClient
import com.example.rateio.data.remote.tmdb.TmdbImageResponse
import com.example.rateio.data.remote.tmdb.TmdbReviews
import com.example.rateio.data.remote.tmdb.toRateItem
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbMovieDetailState(
    val movie: TmdbMovieDetail? = null,
    val imdbRating: ImdbRatingEntity? = null,
    val images: TmdbImageResponse? = null,
    val reviews: TmdbReviews? = null,
    val recommendations: List<RateItem> = emptyList(),
    val savedItem: RateItem? = null,

    val collapsedHeaders: MutableSet<String> = mutableStateSetOf(),

    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbMovieDetailViewModel(
    id: Int,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
    private val imdbRepository: ImdbRatingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbMovieDetailState())
    val state: StateFlow<TmdbMovieDetailState> = _state.asStateFlow()


    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val movie = TmdbClient.tmdb.getMovie(id)
                _state.update { it.copy(movie = movie, isLoading = false) }

                launch {
                    _state.update { it.copy(imdbRating = imdbRepository.getRating(movie.imdbId)) }
                }

                launch {
                    val images = TmdbClient.tmdb.getMovieImages(id)
                    _state.update { it.copy(images = images) }
                }

                launch {
                    val reviews = TmdbClient.tmdb.getMovieReviews(id)
                    _state.update { it.copy(reviews = reviews) }
                }

                launch {
                    val recommendations = TmdbClient.tmdb.getMovieRecommendations(id).results
                        .map { it.toRateItem() }
                    _state.update { it.copy(recommendations = recommendations) }
                }

                launch {
                    val moviesCategory = categoryRepository.getCategoryByType(CategoryType.TMDB_MOVIES)
                    moviesCategory?.let { cat ->
                        val existing = itemRepository.getByExternalId(
                            externalId = movie.id.toString(),
                            categoryId = cat.id,
                        )
                        if (existing != null) _state.update { it.copy(savedItem = existing) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onToggleSaved(movie: TmdbMovieDetail) {
        viewModelScope.launch {
            val state = _state.value
            if (state.savedItem != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItem.id,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.TMDB_MOVIES)?.id ?: 0,
                    title = movie.title,
                ))
                _state.update { it.copy(savedItem = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.TMDB_MOVIES)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.TMDB_MOVIES)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.TMDB_MOVIES)!!.copy(id = id) }

                val id = itemRepository.save(movie.toRateItem(cat.id))
                val item = itemRepository.getById(id)
                _state.update { it.copy(savedItem = item) }
            }
        }
    }

    companion object {
        fun factory(id: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbMovieDetailViewModel(id, categoryRepository, itemRepository, imdbRepository) }
        }
    }
}