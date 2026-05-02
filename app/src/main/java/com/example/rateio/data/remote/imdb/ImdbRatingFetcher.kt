package com.example.rateio.data.remote.imdb

import com.example.rateio.data.remote.TmdbClient


class ImdbRatingFetcher {
    suspend fun fetch(imdbId: String?): Float? {
        imdbId ?: return null
        return runCatching {
            TmdbClient.imdb.getTitle(imdbId).rating.normalizedRating
        }.getOrNull()
    }
}