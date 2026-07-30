package com.rohlicek.rateio.presentation.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.TmdbRepository
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SavedRateItemState(
    val item: RateItem? = null,
    val category: Category? = null,
    val itemRank: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SavedRateItemViewModel(
    private val id: Long,
    private val itemRepository: RateItemRepository,
    private val categoryRepository: CategoryRepository,
    private val tmdbRepository: TmdbRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SavedRateItemState())
    val state: StateFlow<SavedRateItemState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val item = itemRepository.getById(id)
                val category = if (item != null) categoryRepository.getCategoryById(item.categoryId) else null

                val itemRank = if (item?.externalSource != null && item.rating != null)
                    itemRepository.getRankInExternalSource(id, item.externalSource)
                    else null
                _state.update { it.copy(item = item, category = category, itemRank = itemRank, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun saveRating(rating: Float?) {
        viewModelScope.launch {
            itemRepository.rate(id, rating)
        }
    }
    fun saveRatingAndComplete(rating: Float?) {
        viewModelScope.launch {
            itemRepository.rate(id, rating)
            itemRepository.setStatus(id, ItemStatus.COMPLETED)
        }
    }

    fun updateWeight(weight: Float?) {
        viewModelScope.launch {
            itemRepository.setWeight(id, weight)
        }
    }

    fun updateStatus(status: ItemStatus) {
        viewModelScope.launch {
            itemRepository.setStatus(id, status)
        }
    }

    fun updateCoverOverride(override: String?) {
        viewModelScope.launch {
            itemRepository.setCoverOverride(id, override)
        }
    }

    fun updateMetadata(metadata: String?) {
        viewModelScope.launch {
            itemRepository.setMetadata(id, metadata)
        }
    }

    fun updateItem(item: RateItem) {
        viewModelScope.launch {
            itemRepository.update(item)
        }
    }

    fun findOrCreateEpisodeAndNavigate(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        seasonEpisodeCount: Int?,
        onNavigate: (childId: Long, parentId: Long) -> Unit,
    ) {
        viewModelScope.launch {
            val item = _state.value.item ?: return@launch

            val showItem = when (item.externalSource) {
                CategoryType.TMDB_SHOWS -> {
                    item
                }
                CategoryType.TMDB_SEASONS if item.parentId != null -> {
                    itemRepository.getById(item.parentId)
                }
                CategoryType.TMDB_EPISODES if item.parentId != null -> {
                    itemRepository.getParentById(item.parentId)
                }
                else -> null
            }
            if (showItem == null) return@launch


            // Season
            val seasonDetail = tmdbRepository.getSeason(showId, seasonNumber) ?: return@launch
            val seasonId = itemRepository.findOrCreate(
                externalId = seasonDetail.id.toString(),
                categoryId = showItem.categoryId,
                parentId = showItem.id,
            ) {
                seasonDetail.toRateItem(showItem.categoryId, parentId = showItem.id)
            }

            // Episode
            val episodeFromSeason = seasonDetail.episodes.find { it.episodeNumber == episodeNumber }

            val episodeItem = episodeFromSeason?.toRateItem(
                showItem.categoryId, showId = showId, parentId = seasonId, seasonEpisodeCount = seasonEpisodeCount
            ) ?: runCatching {
                TmdbClient.tmdb.getEpisode(showId, seasonNumber, episodeNumber)
            }.getOrNull()?.toRateItem(
                showItem.categoryId, showId = showId, parentId = seasonId, seasonEpisodeCount = seasonEpisodeCount
            )
            if (episodeItem == null || episodeItem.externalId == null) return@launch

            val episodeId = itemRepository.findOrCreate(
                externalId = episodeItem.externalId,
                categoryId = showItem.categoryId,
                parentId = seasonId,
            ) {
                episodeItem
            }

            val savedShowId = itemRepository.getById(seasonId)?.parentId ?: 0

            // Navigate
            onNavigate(episodeId, savedShowId)
        }
    }

    fun findOrCreateChildAndNavigate(
        parentItem: RateItem,
        childItem: RateItem,
        onNavigate: (childId: Long, parentId: Long) -> Unit,
    ) {
        viewModelScope.launch {
            // Parent
            if (parentItem.externalId == null) return@launch
            val parentId = itemRepository.findOrCreate(
                externalId = parentItem.externalId,
                categoryId = parentItem.categoryId,
                parentId = parentItem.parentId,
            ) {
                parentItem
            }

            // Child
            if (childItem.externalId == null) return@launch
            val childId = itemRepository.findOrCreate(
                externalId = childItem.externalId,
                categoryId = parentItem.categoryId,
                parentId = parentId,
            ) {
                childItem
            }

            val grandparentId = itemRepository.getById(parentId)?.parentId ?: 0

            // Navigate
            onNavigate(childId, grandparentId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_state.value.category?.type != null && _state.value.category?.type == CategoryType.TMDB_SHOWS) {
            tmdbRepository.clearSeasonCache()
        }
    }

    companion object {
        fun factory(
            id: Long,
            itemRepository: RateItemRepository,
            categoryRepository:
            CategoryRepository,
            tmdbRepository: TmdbRepository,
        ) = viewModelFactory {
            initializer { SavedRateItemViewModel(id, itemRepository, categoryRepository, tmdbRepository) }
        }
    }
}