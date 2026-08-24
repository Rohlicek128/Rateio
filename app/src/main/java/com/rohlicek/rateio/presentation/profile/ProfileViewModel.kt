package com.rohlicek.rateio.presentation.profile

import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.components.statistics.RatingBarChartType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


data class ProfileItemState(
    val items: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class ProfileSettingsState(
    val selectedCategories: MutableSet<CategoryType> = mutableStateSetOf(),
    val chartType: RatingBarChartType = RatingBarChartType.RATINGS,
)

class ProfileViewModel(
    itemRepository: RateItemRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _settingsState = MutableStateFlow(ProfileSettingsState())
    val settingsState: StateFlow<ProfileSettingsState> = _settingsState.asStateFlow()

    val state: StateFlow<ProfileItemState> = itemRepository.observeItems()
        .map { items -> ProfileItemState(items = items, isLoading = false) }
        .catch { e -> emit(ProfileItemState(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileItemState(isLoading = true),
        )

    fun onChartTypeSelect(type: RatingBarChartType) {
        _settingsState.update { it.copy(chartType = type) }
    }

    companion object {
        fun factory(itemRepository: RateItemRepository, categoryRepository: CategoryRepository) = viewModelFactory {
            initializer { ProfileViewModel(itemRepository, categoryRepository) }
        }
    }
}