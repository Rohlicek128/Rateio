package com.example.rateio.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


data class CategoryCardData(
    val category: Category,
    val icon: ImageVector?,
    val count: Int,
)

data class HomeState(
    val categories: List<CategoryCardData> = emptyList(),
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
                CategoryCardData(
                    category = cat,
                    icon = when (cat.type) {
                        CategoryType.TMDB_SHOWS -> Icons.Default.Tv
                        CategoryType.TMDB_MOVIES -> Icons.Default.Movie
                        CategoryType.STEAM_GAMES -> Icons.Default.VideogameAsset
                        CategoryType.OPEN_LIBRARY_BOOKS -> Icons.AutoMirrored.Filled.MenuBook
                        CategoryType.TMDB_PEOPLE -> Icons.Default.Person
                        else -> null
                    },
                    count = counts[cat.id] ?: 0,
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    companion object {
        fun factory(categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { HomeViewModel(categoryRepository, itemRepository) }
        }
    }
}