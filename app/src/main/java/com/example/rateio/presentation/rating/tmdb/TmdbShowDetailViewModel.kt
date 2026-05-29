package com.example.rateio.presentation.rating.tmdb

import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbImageResponse
import com.example.rateio.data.remote.TmdbShowDetail
import com.example.rateio.data.remote.TmdbShowMetadata
import com.example.rateio.data.remote.imdb.ImdbRating
import com.example.rateio.data.remote.imdb.ImdbRatingFetcher
import com.example.rateio.data.remote.toRateItem
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class SortMode {
    BY_SEASON,
    BY_RATING_BEST,
    BY_RATING_WORST,
    BY_RUNTIME,
    BY_NAME,
}

data class TmdbShowDetailState(
    val show: TmdbShowDetail? = null,
    val imdbRating: ImdbRating? = null,
    val images: TmdbImageResponse? = null,
    val savedItem: RateItem? = null,

    val selectedEpisodeMode: Int = 0,
    val sortMode: SortMode = SortMode.BY_SEASON,
    val expandedSeasons: MutableSet<Int> = mutableStateSetOf(),

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
                        if (existing != null) _state.update { it.copy(savedItem = existing) }
                        //itemRepository.deleteId(271)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val userRatingsState: StateFlow<Map<Int, Map<Int, Float?>>> = state
        .map { it.savedItem?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap())
            } else {
                itemRepository.observeSeasonEpisodeRatings(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )


    fun onToggleSaved(show: TmdbShowDetail) {
        viewModelScope.launch {
            val state = _state.value
            if (state.savedItem != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItem.id,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.TMDB_SHOWS)?.id ?: 0,
                    title = show.name,
                ))
                _state.update { it.copy(savedItem = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.TMDB_SHOWS)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.TMDB_SHOWS)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.TMDB_SHOWS)!!.copy(id = id) }

                val id = itemRepository.save(show.toRateItem(cat.id))
                val item = itemRepository.getById(id)
                _state.update { it.copy(savedItem = item) }
            }
        }
    }

    fun updateSavedItem() {
        if (_state.value.savedItem != null) {
            viewModelScope.launch {
                val item = itemRepository.getById(_state.value.savedItem!!.id)
                _state.update { it.copy(savedItem = item) }
            }
        }
    }

    fun onModeSelect(selectedMode: Int) {
        _state.update { it.copy(selectedEpisodeMode = selectedMode) }
    }

    fun onSortModeSelect(sortMode: SortMode) {
        _state.update { it.copy(sortMode = sortMode) }
    }

    companion object {
        fun factory(showId: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { TmdbShowDetailViewModel(showId, categoryRepository, itemRepository) }
        }
    }
}