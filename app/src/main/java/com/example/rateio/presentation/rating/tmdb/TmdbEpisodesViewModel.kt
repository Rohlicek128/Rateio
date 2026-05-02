package com.example.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.TmdbEpisodeSummary
import com.example.rateio.data.remote.TmdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbEpisodesState(
    val seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbEpisodesViewModel(
    private val showId: Int,
    private val seasonNumbers: List<Int>,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbEpisodesState())
    val state: StateFlow<TmdbEpisodesState> = _state.asStateFlow()

    private val repository = TmdbRepository()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val episodes = repository.getAllEpisodes(showId, seasonNumbers)
                _state.update { it.copy(seasonEpisodes = episodes, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(showId: Int, seasonNumbers: List<Int>) = viewModelFactory {
            initializer { TmdbEpisodesViewModel(showId, seasonNumbers) }
        }
    }
}