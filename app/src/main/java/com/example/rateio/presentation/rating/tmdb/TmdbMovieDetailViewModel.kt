package com.example.rateio.presentation.rating.tmdb

import com.example.rateio.data.remote.TmdbMovieDetail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbImageResponse
import com.example.rateio.data.remote.TmdbShowDetail
import com.example.rateio.data.remote.imdb.ImdbRating
import com.example.rateio.data.remote.imdb.ImdbRatingFetcher
import com.example.rateio.data.remote.toRateItem
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
    val imdbRating: ImdbRating? = null,
    val images: TmdbImageResponse? = null,
    val savedItemId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbMovieDetailViewModel(
    id: Int,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
) : ViewModel() {
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

                launch {
                    val moviesCategory = categoryRepository.getCategoryByType(CategoryType.TMDB_MOVIES)
                    moviesCategory?.let { cat ->
                        val existing = itemRepository.getByExternalId(
                            externalId = movie.id.toString(),
                            categoryId = cat.id,
                        )
                        if (existing != null) _state.update { it.copy(savedItemId = existing.id) }
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
            if (state.savedItemId != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItemId,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.TMDB_MOVIES)?.id ?: 0,
                    title = movie.title,
                ))
                _state.update { it.copy(savedItemId = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.TMDB_MOVIES)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.TMDB_MOVIES)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.TMDB_MOVIES)!!.copy(id = id) }

                val id = itemRepository.save(movie.toRateItem(cat.id))
                _state.update { it.copy(savedItemId = id) }
            }
        }
    }

    companion object {
        fun factory(id: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { TmdbMovieDetailViewModel(id, categoryRepository, itemRepository) }
        }
    }
}