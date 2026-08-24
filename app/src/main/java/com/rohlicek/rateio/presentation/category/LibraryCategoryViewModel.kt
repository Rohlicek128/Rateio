package com.rohlicek.rateio.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.components.statistics.RatingBarChartType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class SortModeLibrary(override val displayName: String): HasDisplayName {
    NAME("Alphabetically"),
    RATING("Rating"),
    UPDATED("Last Updated"),
    CREATED("Date Added"),
}

enum class GroupByLibrary(override val displayName: String): HasDisplayName {
    YEAR("Year"),
    GENRE("Genre (Not Implemented)"),
    STATUS("Status (Not Implemented)"),
    NONE("None"),
}

data class LibraryCategoryState(
    val category: Category? = null,
    val items: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
)

data class LibraryCategorySettingsState(
    val statusFilter: ItemStatus? = null,

    val chartType: RatingBarChartType = RatingBarChartType.BUCKETS,

    val sortMode: SortModeLibrary = SortModeLibrary.RATING,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val globalRank: Boolean = false,

    val groupByMode: GroupByLibrary = GroupByLibrary.NONE,
    val groupByOrder: SortOrder = SortOrder.DESCENDING,
)

class LibraryCategoryViewModel(
    private val categoryId: Long,
    categoryRepository: CategoryRepository,
    itemRepository: RateItemRepository,
) : ViewModel() {
    private val _settingsState = MutableStateFlow(LibraryCategorySettingsState())
    val settingsState: StateFlow<LibraryCategorySettingsState> = _settingsState.asStateFlow()

    val state: StateFlow<LibraryCategoryState> = combine(
        categoryRepository.observeUserCategories()
            .map { cats -> cats.firstOrNull { it.id == categoryId } },
        itemRepository.observeRootItems(categoryId),
    ) { category, items ->
        LibraryCategoryState(category = category, items = items, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryCategoryState(isLoading = true))

    fun editAll() {
        viewModelScope.launch {
            state.value.items.sortedByDescending { it.rating }.forEach { item ->
                println("${item.rating}; ${item.title}; ${item.id}; ${item.externalId}")
            }
        }
    }

    fun onStatusFilterSelect(status: ItemStatus?) {
        _settingsState.update { it.copy(statusFilter = status) }
    }

    fun onChartTypeSelect(type: RatingBarChartType) {
        _settingsState.update { it.copy(chartType = type) }
    }

    fun onSortModeSelect(sortMode: SortModeLibrary) {
        _settingsState.update { it.copy(sortMode = sortMode) }
    }
    fun onSortOrderChange(order: SortOrder) {
        _settingsState.update { it.copy(sortOrder = order) }
    }
    fun onGlobalRankChange(global: Boolean) {
        _settingsState.update { it.copy(globalRank = global) }
    }

    fun onGroupByModeSelect(groupByMode: GroupByLibrary) {
        _settingsState.update { it.copy(groupByMode = groupByMode) }
    }
    fun onGroupByOrderChange(order: SortOrder) {
        _settingsState.update { it.copy(groupByOrder = order) }
    }

    companion object {
        fun factory(categoryId: Long, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory { initializer {
            LibraryCategoryViewModel(categoryId, categoryRepository, itemRepository)
        }}
    }
}