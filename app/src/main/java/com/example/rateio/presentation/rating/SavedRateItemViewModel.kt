package com.example.rateio.presentation.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SavedRateItemState(
    val item: RateItem? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SavedRateItemViewModel(
    private val id: Long,
    private val itemRepository: RateItemRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SavedRateItemState())
    val state: StateFlow<SavedRateItemState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val item = itemRepository.getById(id)
                _state.update { it.copy(item = item, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun saveRating(rating: Float?) {
        viewModelScope.launch {
            itemRepository.rate(id, rating ?: 0f)
        }
    }

    companion object {
        fun factory(id: Long, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { SavedRateItemViewModel(id, itemRepository) }
        }
    }
}