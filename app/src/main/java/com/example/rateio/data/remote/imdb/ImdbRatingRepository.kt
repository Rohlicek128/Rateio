package com.example.rateio.data.remote.imdb

import com.example.rateio.data.db.ImdbRatingDao
import com.example.rateio.data.db.ImdbRatingEntity


class ImdbRatingRepository(private val dao: ImdbRatingDao) {

    suspend fun insertRatings(ratings: List<ImdbRatingEntity>) =
        dao.insertRatings(ratings)

    suspend fun getRating(imdbId: String?): ImdbRatingEntity? {
        if (imdbId == null) return null
        return dao.getRating(imdbId)
    }

    suspend fun clearAllRatings() =
        dao.clearAllRatings()

}