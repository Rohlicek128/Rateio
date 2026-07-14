package com.example.rateio.presentation.rating.tmdb

import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.db.ImdbRatingEntity
import com.example.rateio.data.remote.imdb.ImdbRatingRepository
import com.example.rateio.data.remote.tmdb.TmdbClient
import com.example.rateio.data.remote.tmdb.TmdbEpisodeDetail
import com.example.rateio.data.remote.tmdb.TmdbEpisodeImageResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TmdbEpisodeDetailState(
    val episode: TmdbEpisodeDetail? = null,
    val previousEpisode: Pair<Int, Int>? = null,
    val nextEpisode: Pair<Int, Int>? = null,
    val imdbRating: ImdbRatingEntity? = null,
    val images: TmdbEpisodeImageResponse? = null,

    val collapsedHeaders: MutableSet<String> = mutableStateSetOf(),

    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbEpisodeDetailViewModel(
    showId: Int,
    seasonNumber: Int,
    episodeNumber: Int,
    private val imdbRepository: ImdbRatingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbEpisodeDetailState())
    val state: StateFlow<TmdbEpisodeDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val episode = TmdbClient.tmdb.getEpisode(showId, seasonNumber, episodeNumber)
                val seasonDetail = TmdbClient.tmdb.getSeason(showId, seasonNumber)
                val allEpisodes = seasonDetail.episodes.sortedBy { it.episodeNumber }
                val currentIndex = allEpisodes.indexOfFirst { it.episodeNumber == episodeNumber }

                _state.update { it ->
                    it.copy(
                        episode = episode,
                        isLoading = false,
                        previousEpisode = when {
                            currentIndex > 0 -> seasonNumber to allEpisodes[currentIndex - 1].episodeNumber
                            seasonNumber > 1 -> {
                                val prevSeason = TmdbClient.tmdb.getSeason(showId, seasonNumber - 1)
                                val lastEp = prevSeason.episodes.maxByOrNull { it.episodeNumber }
                                prevSeason.seasonNumber to (lastEp?.episodeNumber ?: 1)
                            }
                            else -> null
                        },
                        nextEpisode = when {
                            currentIndex < allEpisodes.size - 1 -> seasonNumber to allEpisodes[currentIndex + 1].episodeNumber
                            else -> {
                                var nextSeasonDetail = runCatching {
                                    TmdbClient.tmdb.getSeason(showId, seasonNumber + 1)
                                }.getOrNull()
                                nextSeasonDetail?.episodes?.size?.let { if (it <= 0) nextSeasonDetail = null }
                                nextSeasonDetail?.let { s ->
                                    s.seasonNumber to (s.episodes.minByOrNull { it.episodeNumber }?.episodeNumber ?: 1)
                                }
                            }
                        }
                    )
                }

                launch {
                    _state.update { it.copy(imdbRating = imdbRepository.getRatingByImdbId(episode.externalIds?.imdbId)) }
                }

                launch {
                    val images = TmdbClient.tmdb.getEpisodeImages(showId, seasonNumber, episodeNumber)
                    _state.update { it.copy(images = images) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(showId: Int, seasonNumber: Int, episodeNumber: Int, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbEpisodeDetailViewModel(showId, seasonNumber, episodeNumber, imdbRepository) }
        }
    }
}