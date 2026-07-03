package com.example.rateio.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


data class HomeState(
    val categories: List<Pair<Category, Int>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel(
    categoryRepository: CategoryRepository,
    itemRepository: RateItemRepository,
) : ViewModel() {
    val state: StateFlow<HomeState> = combine(
        categoryRepository.observeUserCategories(),
        itemRepository.observeRootItemCounts(),
    ) { categories, counts ->
        HomeState(
            categories = categories.map { cat ->
                cat to (counts[cat.id] ?: 0)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    companion object {
        fun factory(categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { HomeViewModel(categoryRepository, itemRepository) }
        }
    }
}