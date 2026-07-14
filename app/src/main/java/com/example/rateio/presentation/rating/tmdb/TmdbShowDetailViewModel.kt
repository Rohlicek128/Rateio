package com.example.rateio.presentation.rating.tmdb

import androidx.compose.runtime.mutableStateSetOf
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
import com.example.rateio.data.remote.tmdb.TmdbShowDetail
import com.example.rateio.data.remote.tmdb.toRateItem
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.HasDisplayName
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.SortOrder
import com.example.rateio.presentation.components.rating.DisplayMode
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


enum class SortModeShow(override val displayName: String): HasDisplayName {
    SEASON("Season"),
    RATING("Rating"),
    RUNTIME("Runtime"),
    NAME("Alphabetically"),
}

data class TmdbShowDetailState(
    val show: TmdbShowDetail? = null,
    val imdbRating: ImdbRatingEntity? = null,
    val images: TmdbImageResponse? = null,
    val reviews: TmdbReviews? = null,
    val savedItem: RateItem? = null,

    val selectedDisplayMode: DisplayMode = DisplayMode.LIST,
    val selectedSortMode: SortModeShow = SortModeShow.SEASON,
    val selectedSortOrder: SortOrder = SortOrder.DESCENDING,
    val collapsedHeaders: MutableSet<String> = mutableStateSetOf(),
    val expandedSeasons: MutableSet<String?> = mutableStateSetOf(),

    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbShowDetailViewModel(
    showId: Int,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
    private val imdbRepository: ImdbRatingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbShowDetailState())
    val state: StateFlow<TmdbShowDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val show = TmdbClient.tmdb.getShow(showId)
                _state.update { it.copy(show = show, isLoading = false) }

                launch {
                    _state.update { it.copy(imdbRating = imdbRepository.getRating(show.externalIds?.imdbId)) }
                }

                launch {
                    val images = TmdbClient.tmdb.getShowImages(showId)
                    _state.update { it.copy(images = images) }
                }

                launch {
                    val reviews = TmdbClient.tmdb.getShowReviews(showId)
                    _state.update { it.copy(reviews = reviews) }
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

    fun onDisplayModeSelect(selectedMode: DisplayMode) {
        _state.update { it.copy(selectedDisplayMode = selectedMode) }
    }

    fun onSortModeSelect(sortMode: SortModeShow) {
        _state.update { it.copy(selectedSortMode = sortMode) }
    }

    fun onSortOrderChange(order: SortOrder) {
        _state.update { it.copy(selectedSortOrder = order) }
    }

    companion object {
        fun factory(showId: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbShowDetailViewModel(showId, categoryRepository, itemRepository, imdbRepository) }
        }
    }
}