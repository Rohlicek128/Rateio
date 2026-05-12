package com.example.rateio.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class ProfileItemState(
    val itemCount: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ProfileViewModel(
    private val itemRepository: RateItemRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileItemState())
    val state: StateFlow<ProfileItemState> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO ) {
            _state.update { it.copy(isLoading = true) }
            try {
                val itemCount = itemRepository.observeRatedItemCount()
                _state.update { it.copy(itemCount = itemCount, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(itemRepository: RateItemRepository, categoryRepository: CategoryRepository) = viewModelFactory {
            initializer { ProfileViewModel(itemRepository, categoryRepository) }
        }
    }
}