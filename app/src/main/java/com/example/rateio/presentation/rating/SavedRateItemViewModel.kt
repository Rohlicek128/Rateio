package com.example.rateio.presentation.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.toRateItem
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SavedRateItemState(
    val item: RateItem? = null,
    val category: Category? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SavedRateItemViewModel(
    private val id: Long,
    private val itemRepository: RateItemRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SavedRateItemState())
    val state: StateFlow<SavedRateItemState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val item = itemRepository.getById(id)
                val category = if (item != null) categoryRepository.getCategoryById(item.categoryId) else null
                _state.update { it.copy(item = item, category = category, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun saveRating(rating: Float?) {
        viewModelScope.launch {
            if (rating != null) {
                itemRepository.rate(id, rating)
            }
        }
    }

    fun findOrCreateEpisodeAndNavigate(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
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
            val seasonDetail = runCatching {
                TmdbClient.tmdb.getSeason(showId, seasonNumber)
            }.getOrNull()
            if (seasonDetail == null) return@launch

            val seasonId = itemRepository.findOrCreate(
                externalId = seasonDetail.id.toString(),
                categoryId = showItem.categoryId,
                parentId = showItem.id,
            ) {
                seasonDetail.toRateItem(showItem.categoryId, parentId = showItem.id)
            }

            // Episode
            val episodeDetail = runCatching {
                TmdbClient.tmdb.getEpisode(showId, seasonNumber, episodeNumber)
            }.getOrNull()
            if (episodeDetail == null) return@launch

            val episodeId = itemRepository.findOrCreate(
                externalId = episodeDetail.id.toString(),
                categoryId = showItem.categoryId,
                parentId = seasonId,
            ) {
                episodeDetail.toRateItem(showItem.categoryId, showId = showId, parentId = seasonId)
            }

            val savedShowId = itemRepository.getById(seasonId)?.parentId ?: 0

            // Navigate
            onNavigate(episodeId, savedShowId)
        }
    }

    companion object {
        fun factory(id: Long, itemRepository: RateItemRepository, categoryRepository: CategoryRepository) = viewModelFactory {
            initializer { SavedRateItemViewModel(id, itemRepository, categoryRepository) }
        }
    }
}