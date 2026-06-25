package com.example.rateio.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.leaderboard.LeaderboardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ProfileItemState(
    val items: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ProfileViewModel(
    itemRepository: RateItemRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val state: StateFlow<ProfileItemState> = itemRepository.observeItems()
        .map { items -> ProfileItemState(items = items, isLoading = false) }
        .catch { e -> emit(ProfileItemState(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileItemState(isLoading = true),
        )

    companion object {
        fun factory(itemRepository: RateItemRepository, categoryRepository: CategoryRepository) = viewModelFactory {
            initializer { ProfileViewModel(itemRepository, categoryRepository) }
        }
    }
}