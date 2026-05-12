package com.example.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbShowDetail
import com.example.rateio.data.remote.TmdbImageResponse
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


data class TmdbShowDetailState(
    val show: TmdbShowDetail? = null,
    val imdbRating: ImdbRating? = null,
    val images: TmdbImageResponse? = null,
    val savedItemId: Long? = null,

    val selectedEpisodeMode: Int = 0,

    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbShowDetailViewModel(
    showId: Int,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
) : ViewModel() {
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

                launch {
                    val showsCategory = categoryRepository.getCategoryByType(CategoryType.TMDB_SHOWS)
                    showsCategory?.let { cat ->
                        val existing = itemRepository.getByExternalId(
                            externalId = show.id.toString(),
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

    fun onToggleSaved(show: TmdbShowDetail) {
        viewModelScope.launch {
            val state = _state.value
            if (state.savedItemId != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItemId,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.TMDB_SHOWS)?.id ?: 0,
                    title = show.name,
                ))
                _state.update { it.copy(savedItemId = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.TMDB_SHOWS)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.TMDB_SHOWS)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.TMDB_SHOWS)!!.copy(id = id) }

                val id = itemRepository.save(show.toRateItem(cat.id))
                _state.update { it.copy(savedItemId = id) }
            }
        }
    }

    fun onModeSelect(selectedMode: Int) {
        _state.update { it.copy(selectedEpisodeMode = selectedMode) }
    }

    companion object {
        fun factory(showId: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { TmdbShowDetailViewModel(showId, categoryRepository, itemRepository) }
        }
    }
}