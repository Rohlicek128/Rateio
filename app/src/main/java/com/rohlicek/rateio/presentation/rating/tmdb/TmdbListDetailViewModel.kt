package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.db.ImdbRatingEntity
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbListDetail
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.leaderboard.RatingData
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
import kotlin.collections.plus
import kotlin.math.ceil

data class TmdbListDetailState(
    val list: TmdbListDetail? = null,
    val tmdbRatings: Map<Int, RatingData> = emptyMap(),
    val imdbRatings: Map<Int, RatingData> = emptyMap(),

    val selectedRatingsSource: RatingsSource = RatingsSource.IMDB,

    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbListDetailViewModel(
    listId: Int,
    private val imdbRepository: ImdbRatingRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbListDetailState())
    val state: StateFlow<TmdbListDetailState> = _state.asStateFlow()

    val pageCount = 100

    private val networkSemaphore = Semaphore(15)

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val pageNumbers = ceil(pageCount / 20f).toInt()

                val allListResults = coroutineScope {
                    (1..pageNumbers).map { page ->
                        async(Dispatchers.IO) {
                            RateioApplication.instance.tmdbClient.tmdb.getList(listId, page = page)
                        }
                    }.awaitAll()
                }
                val allResults = allListResults.flatMap { l -> l.items }

                val tmdbRatings = allResults.associate { item ->
                    item.id to RatingData(
                        rating = item.voteAverage?.div(10f).takeIf { it != null && it > 0f },
                        votes = item.voteCount?.let { if (it > 0) it else null },
                    )
                }

                _state.update {
                    it.copy(
                        list = allListResults.takeIf { lists -> lists.isNotEmpty() }?.firstOrNull()?.copy(items = allResults),
                        tmdbRatings = tmdbRatings,
                        isLoading = false
                    )
                }

                launch {
                    fetchImdbRatings(tmdbRatings.keys.toList())
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun fetchImdbRatings(tmdbIds: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedRatings = imdbRepository.getRatingByTmdbIdBatch(tmdbIds).associate {
                (it.tmdbId ?: 0) to RatingData(rating = it.averageRating, votes = it.numVotes)
            }

            _state.update { currentState ->
                currentState.copy(imdbRatings = cachedRatings)
            }

            if (cachedRatings.size >= pageCount) return@launch

            tmdbIds.filter { it !in cachedRatings }.chunked(25).forEach { tmdbIdBatch ->
                val ratingsBatch: Map<Int, RatingData> = coroutineScope {
                    tmdbIdBatch.map { tmdbId ->
                        async {
                            networkSemaphore.withPermit {
                                try {
                                    val imdbId = RateioApplication.instance.tmdbClient.tmdb.getMovieExternalIds(tmdbId).imdbId
                                    val rating = if (!imdbId.isNullOrBlank()) {
                                        imdbRepository.getRatingByImdbId(imdbId)
                                    } else null

                                    imdbRepository.linkImdbToTmdb(imdbId, tmdbId)

                                    tmdbId to RatingData(rating = rating?.averageRating, votes = rating?.numVotes)
                                } catch (_: Exception) {
                                    tmdbId to null
                                }
                            }
                        }
                    }
                        .awaitAll()
                        .mapNotNull { (tmdbId, ratingData) ->
                            ratingData?.let { tmdbId to it }
                        }
                        .toMap()
                }

                _state.update { currentState ->
                    currentState.copy(imdbRatings = currentState.imdbRatings + ratingsBatch)
                }
            }
        }
    }

    fun onSelectRatings(ratings: RatingsSource) {
        _state.update { it.copy(selectedRatingsSource = ratings) }
    }

    companion object {
        fun factory(
            listId: Int,
            imdbRepository: ImdbRatingRepository
        ) = viewModelFactory {
            initializer {
                TmdbListDetailViewModel(
                    listId,
                    imdbRepository,
                )
            }
        }
    }
}