package com.example.rateio.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


data class LeaderboardState(
    val items: List<RateItem> = emptyList(),
    val types: List<CategoryType> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LeaderboardViewModel(
    itemRepository: RateItemRepository,
) : ViewModel() {
    private val _currentType = MutableStateFlow(listOf(CategoryType.TMDB_EPISODES))

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<LeaderboardState> = _currentType
        .flatMapLatest { selectedTypes ->
            itemRepository.observeBySources(selectedTypes)
                .map { items ->
                    LeaderboardState(types = selectedTypes, items = items, isLoading = false)
                }
                .catch { e ->
                    emit(LeaderboardState(types = selectedTypes, error = e.message, isLoading = false))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardState(types = listOf(CategoryType.TMDB_EPISODES), isLoading = true),
        )

    fun updateType(newType: CategoryType) {
        val currentList = _currentType.value

        _currentType.value = if (newType in currentList) {
            currentList - newType
        } else {
            currentList + newType
        }
    }

    companion object {
        fun factory(itemRepository: RateItemRepository) = viewModelFactory {
            initializer { LeaderboardViewModel(itemRepository) }
        }
    }
}