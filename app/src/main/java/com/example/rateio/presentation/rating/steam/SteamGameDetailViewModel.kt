package com.example.rateio.presentation.rating.steam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.steam.SteamClient
import com.example.rateio.data.remote.steam.SteamGameDetail
import com.example.rateio.data.remote.steam.SteamGameReviewsSummary
import com.example.rateio.data.remote.steam.toRateItem
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SteamGameDetailState(
    val game: SteamGameDetail? = null,
    val reviews: SteamGameReviewsSummary? = null,
    val savedItemId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SteamGameDetailViewModel(
    appId: String,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SteamGameDetailState())
    val state: StateFlow<SteamGameDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val game = SteamClient.steamStore.getGames(appId)[appId]?.data
                _state.update { it.copy(game = game, isLoading = false) }

                launch {
                    val reviews = SteamClient.steamStore.getGameReviews(appId)
                    _state.update { it.copy(reviews = reviews.querySummary) }
                }

                launch {
                    val gamesCategory = categoryRepository.getCategoryByType(CategoryType.STEAM_GAMES)
                    gamesCategory?.let { cat ->
                        val existing = itemRepository.getByExternalId(
                            externalId = game?.steamAppId.toString(),
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

    fun onToggleSaved(game: SteamGameDetail) {
        viewModelScope.launch {
            val state = _state.value
            if (state.savedItemId != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItemId,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.STEAM_GAMES)?.id ?: 0,
                    title = game.name,
                ))
                _state.update { it.copy(savedItemId = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.STEAM_GAMES)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.STEAM_GAMES)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.STEAM_GAMES)!!.copy(id = id) }

                val id = itemRepository.save(game.toRateItem(cat.id))
                _state.update { it.copy(savedItemId = id) }
            }
        }
    }

    companion object {
        fun factory(appId: String, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { SteamGameDetailViewModel(appId, categoryRepository, itemRepository) }
        }
    }
}