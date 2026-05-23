package com.example.rateio.data.remote

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class TmdbRepository {
    suspend fun getAllEpisodes(
        showId: Int,
        seasonNumbers: List<Int>,
    ): Map<Int, List<TmdbEpisodeSummary>> = coroutineScope {
        seasonNumbers
            .filter { it > 0 }
            .map { seasonNumber ->
                async {
                    runCatching {
                        seasonNumber to TmdbClient.tmdb.getSeason(showId, seasonNumber).episodes
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