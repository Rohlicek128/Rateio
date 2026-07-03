package com.example.rateio.data.remote.imdb

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.example.rateio.data.remote.tmdb.TmdbClient
import kotlinx.coroutines.Dispatchers


class ImdbRatingFetcher {
    suspend fun fetch(imdbId: String?): ImdbRating? {
        imdbId ?: return null
        return runCatching {
            TmdbClient.imdb.getTitle(imdbId).rating
        }.getOrNull()
    }
    
    
    private val cache = mutableMapOf<String, Map<Int, Float?>>()

    fun ratingsForShow(
        imdbId: String,
        seasonNumbers: List<Int>,
    ): Flow<Map<Int, Map<Int, Float?>>> = flow {
        // season → (episodeNumber → rating)
        val accumulated = mutableMapOf<Int, Map<Int, Float?>>()

        // Emit cached seasons immediately so the UI isn't blank
        seasonNumbers.forEach { season ->
            val cacheKey = "$imdbId-s$season"
            cache[cacheKey]?.let { cached ->
                accumulated[season] = cached
            }
        }
        if (accumulated.isNotEmpty()) emit(accumulated.toMap())

        // Fetch missing seasons
        val missingSeasons = seasonNumbers.filter { season ->
            !cache.containsKey("$imdbId-s$season")
        }

        missingSeasons.forEach { season ->
            val ratings = fetchAllPagesForSeason(imdbId, season)
            val cacheKey = "$imdbId-s$season"
            cache[cacheKey] = ratings
            accumulated[season] = ratings
            emit(accumulated.toMap()) // emit after each season so UI updates
        }
    }.flowOn(Dispatchers.IO) // network off the main thread

    private suspend fun fetchAllPagesForSeason(
        imdbId: String,
        season: Int,
    ): Map<Int, Float?> {
        val result = mutableMapOf<Int, Float?>()
        var pageToken: String? = null

        do {
            val response = runCatching {
                TmdbClient.imdb.getEpisodes(
                    titleId = imdbId,
                    season = season.toString(),
                    pageSize = 50,
                    pageToken = pageToken,
                )
            }.getOrNull() ?: break

            response.episodes?.forEach { episode ->
                result[episode.episodeNumber] = episode.rating?.normalizedRating
            }

            pageToken = response.nextPageToken
        } while (pageToken != null)

        return result
    }
}