package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeSummary
import com.rohlicek.rateio.data.remote.tmdb.TmdbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit


data class TmdbEpisodesState(
    val seasonEpisodes: Map<Int, List<TmdbEpisodeSummary>> = emptyMap(),
    val imdbRatings: Map<Int, Float?> = emptyMap(),
    val isLoadingEpisodes: Boolean = false,
    val isLoadingRatings: Boolean = true,
    val error: String? = null,
)

class TmdbEpisodesViewModel(
    private val showId: Int,
    private val seasonNumbers: List<Int>,
    private val groupId: String?,
    fetchRatings: Boolean,
    private val imdbRepository: ImdbRatingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbEpisodesState())
    val state: StateFlow<TmdbEpisodesState> = _state.asStateFlow()

    private val repository = TmdbRepository()
    private val networkSemaphore = Semaphore(15)

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEpisodes = true) }
            try {
                val episodes = if (groupId != null) repository.getEpisodeGroup(groupId)
                else repository.getAllEpisodes(showId, seasonNumbers)
                _state.update { it.copy(seasonEpisodes = episodes, isLoadingEpisodes = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoadingEpisodes = false) }
            }

            if (fetchRatings) {
                fetchImdbRatings()
            } else {
                _state.update { it.copy(isLoadingRatings = false) }
            }
        }
    }

    fun fetchImdbRatings() {
        viewModelScope.launch(Dispatchers.IO) {
            val episodeIdsBySeason = _state.value.seasonEpisodes.mapValues { episodes -> episodes.value.map { it.id } }

            _state.update { currentState ->
                currentState.copy(imdbRatings = emptyMap(), isLoadingRatings = true)
            }

            episodeIdsBySeason.forEach { (seasonNum, episodeIds) ->
                val seasonRatingsMap: Map<Int, Float?> = coroutineScope {
                    episodeIds.mapIndexed { index, tmdbId ->
                        val epNum = index + 1
                        async {
                            networkSemaphore.withPermit {
                                val ratingValue = try {
                                    val cachedRating = imdbRepository.getRatingByTmdbId(tmdbId)
                                    if (cachedRating != null) {
                                        cachedRating.averageRating
                                    } else {
                                        val imdbId = RateioApplication.instance.tmdbClient.tmdb.getEpisodeExternalIds(
                                            showId = showId,
                                            seasonNumber = seasonNum,
                                            episodeNumber = epNum
                                        ).imdbId

                                        val rating = if (!imdbId.isNullOrBlank()) {
                                            imdbRepository.getRatingByImdbId(imdbId)
                                        } else null

                                        imdbRepository.linkImdbToTmdb(imdbId, tmdbId)

                                        rating?.averageRating
                                    }
                                } catch (_: Exception) {
                                    null
                                }

                                tmdbId to ratingValue
                            }
                        }
                    }
                        .awaitAll()
                        .toMap()
                }

                // Progressive merge
                _state.update { currentState ->
                    currentState.copy(
                        imdbRatings = currentState.imdbRatings + seasonRatingsMap
                    )
                }
            }

            _state.update { it.copy(isLoadingRatings = false) }
        }
    }

    companion object {
        fun factory(showId: Int, seasonNumbers: List<Int>, groupId: String?, fetchRatings: Boolean, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbEpisodesViewModel(showId, seasonNumbers, groupId, fetchRatings, imdbRepository) }
        }
    }
}