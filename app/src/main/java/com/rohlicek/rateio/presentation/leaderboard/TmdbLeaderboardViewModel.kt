package com.rohlicek.rateio.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.rating.tmdb.RatingsSource
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
import kotlin.div
import kotlin.math.ceil

data class RatingData(
    val rating: Float?,
    val votes: Int?,
)

data class TmdbLeaderboardState(
    val results: List<RateItem> = emptyList(),
    val selectedRatingsSource: RatingsSource = RatingsSource.IMDB,
    val tmdbRatings: Map<Int, RatingData> = emptyMap(),
    val imdbRatings: Map<Int, RatingData> = emptyMap(),

    val completedItems: Set<String> = emptySet(),

    val isLoading: Boolean = false,
    val error: String? = null,
)

class TmdbLeaderboardViewModel(
    private val category: CategoryType,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
    private val imdbRepository: ImdbRatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbLeaderboardState())
    val state: StateFlow<TmdbLeaderboardState> = _state.asStateFlow()

    val itemNumber = 250

    private val networkSemaphore = Semaphore(15)

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val pageNumbers = ceil(itemNumber / 20f).toInt()

                val allPagesResults = coroutineScope {
                    (1..pageNumbers).map { page ->
                        async(Dispatchers.IO) {
                            if (category == CategoryType.TMDB_MOVIES) {
                                RateioApplication.instance.tmdbClient.tmdb.topRatedMovies(
                                    page = page,
                                    voteCountGte = 2500f,
                                ).results.map { it.toRateItem(weight = it.voteCount?.toFloat() ?: -1f)}
                            }
                            else {
                                RateioApplication.instance.tmdbClient.tmdb.topRatedShows(
                                    page = page,
                                    voteCountGte = 1000f,
                                ).results.map { it.toRateItem(weight = it.voteCount?.toFloat() ?: -1f)}
                            }
                        }
                    }.awaitAll()
                }.flatten().take(itemNumber)

                val tmdbRatings = allPagesResults.associate { item ->
                        (item.externalId?.toInt() ?: 0) to RatingData(
                            rating = item.rating.takeIf { it != null && it > 0f },
                            votes = item.ratingWeight.let { if (it > 0) it.toInt() else null },
                        )
                    }

                _state.update { it.copy(results = allPagesResults, tmdbRatings = tmdbRatings, isLoading = false) }

                launch {
                    fetchImdbRatings(tmdbRatings.keys.toList())
                }

                launch {
                    val showsCategory = categoryRepository.getCategoryByType(category)
                    showsCategory?.id?.let { categoryId ->
                        val externalIds = allPagesResults.mapNotNull { it.externalId }
                        if (externalIds.isNotEmpty()) {
                            itemRepository.getByExternalIdBatch(externalIds, categoryId)
                                .collect { savedItems ->
                                    _state.update { currentState ->
                                        currentState.copy(
                                            completedItems = savedItems
                                                .filter { it.status == ItemStatus.COMPLETED }
                                                .mapNotNull { it.externalId }
                                                .toSet()
                                        )
                                    }
                                }
                        }
                    }
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
            //val cachedRatings: Map<Int, RatingData> = emptyMap()

            _state.update { currentState ->
                currentState.copy(imdbRatings = cachedRatings)
            }

            if (cachedRatings.size >= itemNumber) return@launch

            tmdbIds.filter { it !in cachedRatings }.chunked(25).forEach { tmdbIdBatch ->
                val ratingsBatch: Map<Int, RatingData> = coroutineScope {
                    tmdbIdBatch.map { tmdbId ->
                        async {
                            networkSemaphore.withPermit {
                                try {
                                    val imdbId = if (category == CategoryType.TMDB_MOVIES) {
                                        RateioApplication.instance.tmdbClient.tmdb.getMovieExternalIds(tmdbId).imdbId
                                    }
                                    else {
                                        RateioApplication.instance.tmdbClient.tmdb.getShowExternalIds(tmdbId).imdbId
                                    }
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
        fun factory(category: CategoryType, categoryRepository: CategoryRepository, itemRepository: RateItemRepository, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbLeaderboardViewModel(category, categoryRepository, itemRepository, imdbRepository) }
        }
    }
}