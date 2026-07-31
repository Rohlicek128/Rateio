package com.rohlicek.rateio.data.remote.tmdb

import android.util.Log
import com.rohlicek.rateio.RateioApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TmdbRepository {
    private val seasonCache = java.util.concurrent.ConcurrentHashMap<String, TmdbSeasonDetail>()

    suspend fun getSeason(showId: Int, seasonNumber: Int): TmdbSeasonDetail? {
        val cacheKey = "${showId}_$seasonNumber"
        seasonCache[cacheKey]?.let { return it }

        val seasonDetail = runCatching {
            RateioApplication.instance.tmdbClient.tmdb.getSeason(showId, seasonNumber)
        }.getOrNull()

        if (seasonDetail != null) {
            seasonCache[cacheKey] = seasonDetail
        }

        return seasonDetail
    }

    fun clearSeasonCache() {
        seasonCache.clear()
    }

    suspend fun getAllEpisodes(
        showId: Int,
        seasonNumbers: List<Int>,
    ): Map<Int, List<TmdbEpisodeSummary>> = coroutineScope {
        seasonNumbers
            .filter { it > 0 }
            .map { seasonNumber ->
                async {
                    runCatching {
                        seasonNumber to RateioApplication.instance.tmdbClient.tmdb.getSeason(showId, seasonNumber).episodes
                    }.getOrElse { e ->
                        Log.e("TmdbRepository", "Failed season $seasonNumber: ${e.message}")
                        null
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
            .toMap()
    }
}