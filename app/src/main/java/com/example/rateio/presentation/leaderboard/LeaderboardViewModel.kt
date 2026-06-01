package com.example.rateio.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.category.LibraryCategoryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class LeaderboardState(
    val items: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class LeaderboardViewModel(
    type: CategoryType,
    itemRepository: RateItemRepository,
) : ViewModel() {

    val state: StateFlow<LeaderboardState> = itemRepository.observeBySource(type)
        .map { items -> LeaderboardState(items = items, isLoading = false) }
        .catch { e -> emit(LeaderboardState(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardState(isLoading = true),
        )


    companion object {
        fun factory(type: CategoryType, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { LeaderboardViewModel(type, itemRepository) }
        }
    }
}