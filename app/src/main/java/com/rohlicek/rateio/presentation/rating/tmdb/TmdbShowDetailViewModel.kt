package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.db.ImdbRatingEntity
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeGroup
import com.rohlicek.rateio.data.remote.tmdb.TmdbImageResponse
import com.rohlicek.rateio.data.remote.tmdb.TmdbReviews
import com.rohlicek.rateio.data.remote.tmdb.TmdbShowDetail
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.components.rating.DisplayMode
import com.rohlicek.rateio.presentation.components.statistics.RatingBarChartType
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

enum class RatingsSource(override val displayName: String): HasDisplayName {
    IMDB("IMDb"),
    TMDB("TMDB"),
    USER("Yours"),
}

enum class ShowTabs(override val displayName: String): HasDisplayName {
    EPISODES("Episodes"),
    STATISTICS("Statistics"),
    IMAGES("Images"),
    REVIEWS("Reviews"),
    RECOMMENDATIONS("Next to Watch"),
}

data class TmdbShowDetailState(
    val show: TmdbShowDetail? = null,
    val imdbRating: ImdbRatingEntity? = null,
    val images: TmdbImageResponse? = null,
    val reviews: TmdbReviews? = null,
    val savedItem: RateItem? = null,

    val selectedGroupId: String? = null,
    val episodeGroups: List<TmdbEpisodeGroup> = emptyList(),

    val selectedTab: ShowTabs = ShowTabs.EPISODES,
    val selectedRatingSource: RatingsSource = RatingsSource.IMDB,
    val selectedDisplayMode: DisplayMode = DisplayMode.LIST,
    val selectedSortMode: SortModeShow = SortModeShow.SEASON,
    val selectedSortOrder: SortOrder = SortOrder.DESCENDING,
    val chartType: RatingBarChartType = RatingBarChartType.BUCKETS,
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
    isSaved: Boolean,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbShowDetailState())
    val state: StateFlow<TmdbShowDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val show = RateioApplication.instance.tmdbClient.tmdb.getShow(showId)
                _state.update { it.copy(
                    show = show,
                    isLoading = false,
                    selectedRatingSource = if (!isSaved) RatingsSource.IMDB else RatingsSource.USER
                ) }

                launch {
                    imdbRepository.linkImdbToTmdb(show.externalIds?.imdbId, showId)
                    _state.update { it.copy(imdbRating = imdbRepository.getRatingByTmdbId(showId)) }
                }

                launch {
                    val images = RateioApplication.instance.tmdbClient.tmdb.getShowImages(showId)
                    _state.update { it.copy(images = images) }
                }

                launch {
                    val reviews = RateioApplication.instance.tmdbClient.tmdb.getShowReviews(showId)
                    _state.update { it.copy(reviews = reviews) }
                }

                launch {
                    val groups = RateioApplication.instance.tmdbClient.tmdb.getEpisodeGroups(showId)
                        .results.sortedBy { it.type }
                    _state.update { it.copy(episodeGroups = groups) }
                }

                launch {
                    val showsCategory = categoryRepository.getCategoryByType(CategoryType.TMDB_SHOWS)
                    showsCategory?.let { category ->
                        val existing = itemRepository.findAndUpdateMetadata(
                            externalId = show.id.toString(),
                            categoryId = category.id,
                        ) { show.toRateItem() }
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
    val userRatingsByIdState: StateFlow<Map<Int, Float?>> = state
        .map { it.savedItem?.id }
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyMap())
            } else {
                itemRepository.observeChildrenExternalIdToRatings(id)
                    .map { ratingMap ->
                        ratingMap.mapNotNull { (key, value) ->
                            key.toIntOrNull()?.let { intKey -> intKey to value }
                        }.toMap()
                    }
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

    fun onGroupSelect(groupId: String?) {
        _state.update { it.copy(selectedGroupId = groupId) }
    }

    fun onTabSelect(tab: ShowTabs) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun onRatingSourceSelect(source: RatingsSource) {
        _state.update { it.copy(selectedRatingSource = source) }
    }

    fun onDisplayModeSelect(selectedMode: DisplayMode) {
        _state.update { it.copy(selectedDisplayMode = selectedMode) }
    }

    fun onChartTypeSelect(type: RatingBarChartType) {
        _state.update { it.copy(chartType = type) }
    }

    fun onSortModeSelect(sortMode: SortModeShow) {
        _state.update { it.copy(selectedSortMode = sortMode) }
    }

    fun onSortOrderChange(order: SortOrder) {
        _state.update { it.copy(selectedSortOrder = order) }
    }

    companion object {
        fun factory(showId: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository, imdbRepository: ImdbRatingRepository, isSaved: Boolean) = viewModelFactory {
            initializer { TmdbShowDetailViewModel(showId, categoryRepository, itemRepository, imdbRepository, isSaved) }
        }
    }
}