package com.example.rateio.presentation.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rateio.data.remote.TmdbClient
import com.example.rateio.data.remote.TmdbShow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class BrowseState(
    val query: String = "",
    val results: List<TmdbShow> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class BrowseViewModel : ViewModel() {
    private val _state = MutableStateFlow(BrowseState())
    val state: StateFlow<BrowseState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, error = null) }

        // Cancel any in-flight search
        searchJob?.cancel()

        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            _state.update { it.copy(isLoading = true) }

            try {
                val response = TmdbClient.service.searchShows(query)
                _state.update { it.copy(results = response.results, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}