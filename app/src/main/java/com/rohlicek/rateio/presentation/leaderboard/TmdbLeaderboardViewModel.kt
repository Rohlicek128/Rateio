package com.rohlicek.rateio.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.RateioApplication
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.toRateItem
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.category.GroupByLibrary
import com.rohlicek.rateio.presentation.components.SortOrder
import com.rohlicek.rateio.presentation.rating.tmdb.RatingsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.math.ceil


enum class DiscoverSortBy(override val displayName: String, val value: String): HasDisplayName {
    VOTE_AVERAGE("Rating", "vote_average"),
    VOTE_COUNT("Votes", "vote_count"),
    POPULARITY("Popularity", "popularity"),
    REVENUE("Revenue", "revenue"),
    TITLE("Name", "title"),
    RELEASE_DATE("Release Date", "primary_release_date"),
}

data class DiscoverQueryParams(
    val sortBy: DiscoverSortBy = DiscoverSortBy.VOTE_AVERAGE,
    val sortOrder: SortOrder = SortOrder.DESCENDING,
    val minVoteCount: Int = 100,
    val withoutGenres: String = "99,10755",
    val itemNumber: Int = 250,
) {
    val sortByParam: String
        get() = "${sortBy.value}.${sortOrder.value}"
}

data class RatingData(
    val rating: Float?,
    val votes: Int?,
)

data class TmdbLeaderboardState(
    val results: List<RateItem> = emptyList(),
    val tmdbRatings: Map<Int, RatingData> = emptyMap(),
    val imdbRatings: Map<Int, RatingData> = emptyMap(),

    val selectedRatingsSource: RatingsSource = RatingsSource.IMDB,
    val groupByMode: GroupByLibrary = GroupByLibrary.NONE,
    val groupByOrder: SortOrder = SortOrder.DESCENDING,
    val globalRank: Boolean = false,

    val completedItems: Set<String> = emptySet(),

    val isLoading: Boolean = false,
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TmdbLeaderboardViewModel(
    private val category: CategoryType,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
    private val imdbRepository: ImdbRatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TmdbLeaderboardState())
    val state: StateFlow<TmdbLeaderboardState> = _state.asStateFlow()

    private val defaultMinVoteCount = if (category == CategoryType.TMDB_MOVIES) 2500 else 1000

    private val _queryParams = MutableStateFlow(
        DiscoverQueryParams(minVoteCount = defaultMinVoteCount)
    )
    val queryParams: StateFlow<DiscoverQueryParams> = _queryParams.asStateFlow()

    private val networkSemaphore = Semaphore(15)

    init {
        viewModelScope.launch {
            _queryParams
                .flatMapLatest { params ->
                    flow {
                        emit(FetchResult.Loading)
                        try {
                            val results = fetchLeaderboard(params)
                            emit(FetchResult.Success(results))
                        } catch (e: Exception) {
                            emit(FetchResult.Error(e.message))
                        }
                    }
                }
                .collect { result ->
                    when (result) {
                        is FetchResult.Loading -> _state.update { currentState ->
                            currentState.copy(isLoading = true, error = null)
                        }
                        is FetchResult.Success -> _state.update { currentState ->
                            val tmdbRatings = result.items.associate { item ->
                                (item.externalId?.toInt() ?: 0) to RatingData(
                                    rating = item.rating.takeIf { it != null && it > 0f },
                                    votes = item.ratingWeight.let { if (it > 0) it.toInt() else null },
                                )
                            }

                            launch {
                                fetchImdbRatings(tmdbRatings.keys.toList(), _queryParams.value)
                            }

                            launch {
                                val showsCategory = categoryRepository.getCategoryByType(category)
                                showsCategory?.id?.let { categoryId ->
                                    val externalIds = result.items.mapNotNull { it.externalId }
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

                            currentState.copy(results = result.items, tmdbRatings = tmdbRatings, isLoading = false)
                        }
                        is FetchResult.Error -> _state.update { currentState ->
                            currentState.copy(error = result.message, isLoading = false)
                        }
                    }
                }
        }
    }

    private suspend fun fetchLeaderboard(params: DiscoverQueryParams): List<RateItem> {
        val pageNumbers = ceil(params.itemNumber / 20f).toInt()

        return coroutineScope {
            (1..pageNumbers).map { page ->
                async(Dispatchers.IO) {
                    networkSemaphore.withPermit {
                        if (category == CategoryType.TMDB_MOVIES) {
                            RateioApplication.instance.tmdbClient.tmdb.discoverMovies(
                                page = page,
                                sortBy = params.sortByParam,
                                minVoteCount = if (params.sortBy == DiscoverSortBy.VOTE_AVERAGE) params.minVoteCount else 200,
                                withoutGenres = params.withoutGenres,
                            ).results.map { it.toRateItem(weight = it.voteCount?.toFloat() ?: -1f, subtitleOverride = params.sortBy) }
                        } else {
                            RateioApplication.instance.tmdbClient.tmdb.discoverShows(
                                page = page,
                                sortBy = params.sortByParam,
                                minVoteCount = if (params.sortBy == DiscoverSortBy.VOTE_AVERAGE) params.minVoteCount else 200,
                                withoutGenres = params.withoutGenres,
                            ).results.map { it.toRateItem(weight = it.voteCount?.toFloat() ?: -1f, subtitleOverride = params.sortBy) }
                        }
                    }
                }
            }.awaitAll()
        }.flatten().take(params.itemNumber)
    }

    fun fetchImdbRatings(tmdbIds: List<Int>, params: DiscoverQueryParams) {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedRatings = imdbRepository.getRatingByTmdbIdBatch(tmdbIds).associate {
                (it.tmdbId ?: 0) to RatingData(rating = it.averageRating, votes = it.numVotes)
            }
            //val cachedRatings: Map<Int, RatingData> = emptyMap()

            _state.update { currentState ->
                currentState.copy(imdbRatings = cachedRatings)
            }

            if (cachedRatings.size >= params.itemNumber) return@launch

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

    fun updateSortBy(sortBy: DiscoverSortBy) {
        _queryParams.update { it.copy(sortBy = sortBy) }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _queryParams.update { it.copy(sortOrder = sortOrder) }
    }

    fun updateMinVoteCount(minVoteCount: Int) {
        _queryParams.update { it.copy(minVoteCount = minVoteCount) }
    }

    fun updateWithoutGenres(genreIds: String) {
        _queryParams.update { it.copy(withoutGenres = genreIds) }
    }


    fun onSelectRatings(ratings: RatingsSource) {
        _state.update { it.copy(selectedRatingsSource = ratings) }
    }

    fun onSelectGroupBy(groupBy: GroupByLibrary) {
        _state.update { it.copy(groupByMode = groupBy) }
    }

    fun onSelectGroupOrder(order: SortOrder) {
        _state.update { it.copy(groupByOrder = order) }
    }

    fun onGlobalRankChange(global: Boolean) {
        _state.update { it.copy(globalRank = global) }
    }


    private sealed interface FetchResult {
        data object Loading : FetchResult
        data class Success(val items: List<RateItem>) : FetchResult
        data class Error(val message: String?) : FetchResult
    }

    companion object {
        fun factory(category: CategoryType, categoryRepository: CategoryRepository, itemRepository: RateItemRepository, imdbRepository: ImdbRatingRepository) = viewModelFactory {
            initializer { TmdbLeaderboardViewModel(category, categoryRepository, itemRepository, imdbRepository) }
        }
    }
}