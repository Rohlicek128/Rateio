package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.TmdbPersonDetail
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TmdbPersonDetailState(
    val person: TmdbPersonDetail? = null,
    val savedItemId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbPersonDetailViewModel(
    id: Int,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbPersonDetailState())
    val state: StateFlow<TmdbPersonDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val person = RateioApplication.instance.tmdbClient.tmdb.getPerson(id)
                _state.update { it.copy(person = person, isLoading = false) }

                launch {
                    val peopleCategory = categoryRepository.getCategoryByType(CategoryType.TMDB_PEOPLE)
                    peopleCategory?.let { cat ->
                        val existing = itemRepository.getByExternalId(
                            externalId = person.id.toString(),
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

    fun onToggleSaved(person: TmdbPersonDetail) {
        viewModelScope.launch {
            val state = _state.value
            if (state.savedItemId != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItemId,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.TMDB_PEOPLE)?.id ?: 0,
                    title = person.name,
                ))
                _state.update { it.copy(savedItemId = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.TMDB_PEOPLE)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.TMDB_PEOPLE)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.TMDB_PEOPLE)!!.copy(id = id) }

                val id = itemRepository.save(person.toRateItem(cat.id))
                _state.update { it.copy(savedItemId = id) }
            }
        }
    }

    companion object {
        fun factory(id: Int, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { TmdbPersonDetailViewModel(id, categoryRepository, itemRepository) }
        }
    }
}