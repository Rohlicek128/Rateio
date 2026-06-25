package com.example.rateio.presentation.category

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.Category
import com.example.rateio.model.ItemStatus
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.rating.tmdb.TmdbMovieDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class LibraryCategoryState(
    val category: Category? = null,
    val items: List<RateItem> = emptyList(),
    val isLoading: Boolean = false,
)

class LibraryCategoryViewModel(
    private val categoryId: Long,
    categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
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
            state.value.items.forEach { item ->
                if (item.status == ItemStatus.NONE) {
                    itemRepository.setStatus(item.id, ItemStatus.COMPLETED)
                }
            }
        }
    }

    companion object {
        fun factory(categoryId: Long, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory { initializer {
            LibraryCategoryViewModel(categoryId, categoryRepository, itemRepository)
        }}
    }
}