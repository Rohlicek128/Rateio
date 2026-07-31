package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.db.ImdbRatingEntity
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeDetail
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeImageResponse
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class EpisodeMoveData(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeCount: Int?,
)

data class TmdbEpisodeDetailState(
    val episode: TmdbEpisodeDetail? = null,
    val previousEpisode: EpisodeMoveData? = null,
    val nextEpisode: EpisodeMoveData? = null,
    val imdbRating: ImdbRatingEntity? = null,
    val images: TmdbEpisodeImageResponse? = null,

    val collapsedHeaders: MutableSet<String> = mutableStateSetOf(),

    val savedItem: RateItem? = null, // TODO: Get saved episode RateItem from database
    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbEpisodeDetailViewModel(
    showId: Int,
    seasonNumber: Int,
    episodeNumber: Int,
    seasonEpisodeCount: Int?,
    private val imdbRepository: ImdbRatingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbEpisodeDetailState())
    val state: StateFlow<TmdbEpisodeDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val episode = RateioApplication.instance.tmdbClient.tmdb.getEpisode(showId, seasonNumber, episodeNumber)
                val episodeCount = seasonEpisodeCount ?: RateioApplication.instance.tmdbClient.tmdb.getSeason(showId, seasonNumber).episodes.size

                _state.update { it ->
                    it.copy(
                        episode = episode,
                        isLoading = false,
                        previousEpisode = when {
                            episodeNumber > 1 -> EpisodeMoveData(seasonNumber, episodeNumber - 1, episodeCount)
                            seasonNumber > 1 -> {
                                val prevSeason = RateioApplication.instance.tmdbClient.tmdb.getSeason(showId, seasonNumber - 1)
                                val lastEp = prevSeason.episodes.maxByOrNull { it.episodeNumber }
                                EpisodeMoveData(
                                    prevSeason.seasonNumber,
                                    (lastEp?.episodeNumber ?: 1),
                                    prevSeason.episodes.size
                                )
                            }
                            else -> null
                        },
                        nextEpisode = when {
                            episodeNumber < episodeCount -> EpisodeMoveData(seasonNumber, episodeNumber + 1, episodeCount)
                            else -> {
                                val nextSeasonDetail = runCatching {
                                    RateioApplication.instance.tmdbClient.tmdb.getSeason(showId, seasonNumber + 1)
                                }.getOrNull()
                                nextSeasonDetail?.let { season ->
                                    if (season.episodes.isNotEmpty()) {
                                        EpisodeMoveData(
                                            season.seasonNumber,
                                            1,
                                            season.episodes.size,
                                        )
                                    } else null
                                }
                            }
                        }
                    )
                }

                launch {
                    _state.update { it.copy(imdbRating = imdbRepository.getRatingByImdbId(episode.externalIds?.imdbId)) }
                }

                launch {
                    val images = RateioApplication.instance.tmdbClient.tmdb.getEpisodeImages(showId, seasonNumber, episodeNumber)
                    _state.update { it.copy(images = images) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(
            showId: Int,
            seasonNumber: Int,
            episodeNumber: Int,
            seasonEpisodeCount: Int?,
            imdbRepository: ImdbRatingRepository
        ) = viewModelFactory {
            initializer {
                TmdbEpisodeDetailViewModel(
                    showId,
                    seasonNumber,
                    episodeNumber,
                    seasonEpisodeCount,
                    imdbRepository,
                )
            }
        }
    }
}