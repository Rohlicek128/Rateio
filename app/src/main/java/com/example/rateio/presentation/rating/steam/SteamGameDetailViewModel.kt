package com.example.rateio.presentation.rating.steam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.remote.steam.SteamClient
import com.example.rateio.data.remote.steam.SteamGameDetail
import com.example.rateio.data.remote.steam.SteamGameReviewsSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SteamGameDetailState(
    val game: SteamGameDetail? = null,
    val reviews: SteamGameReviewsSummary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

class SteamGameDetailViewModel(
    appId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(SteamGameDetailState())
    val state: StateFlow<SteamGameDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val game = SteamClient.steamStore.getGames(appId)[appId]?.data
                _state.update { it.copy(game = game, isLoading = false) }

                launch {
                    val reviews = SteamClient.steamStore.getGameReviews(appId)
                    _state.update { it.copy(reviews = reviews.querySummary) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(appId: String) = viewModelFactory {
            initializer { SteamGameDetailViewModel(appId) }
        }
    }
}