package com.rohlicek.rateio.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


enum class SortModeLibrary(override val displayName: String): HasDisplayName {
    NAME("Alphabetically"),
    RATING("Rating"),
    UPDATED("Last Updated"),
    CREATED("Date Added"),
}

data class LibraryCategoryState(
    val category: Category? = null,
    val items: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
)

class LibraryCategoryViewModel(
    private val categoryId: Long,
    categoryRepository: CategoryRepository,
    itemRepository: RateItemRepository,
) : ViewModel() {

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

    companion object {
        fun factory(categoryId: Long, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory { initializer {
            LibraryCategoryViewModel(categoryId, categoryRepository, itemRepository)
        }}
    }
}