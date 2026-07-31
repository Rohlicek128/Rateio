package com.rohlicek.rateio.data.remote.imdb

import com.rohlicek.rateio.data.db.ImdbRatingDao
import com.rohlicek.rateio.data.db.ImdbRatingEntity


class ImdbRatingRepository(private val dao: ImdbRatingDao) {

    suspend fun insertRatings(ratings: List<ImdbRatingEntity>) =
        dao.insertRatings(ratings)

    suspend fun upsertRatings(ratings: List<ImdbRatingEntity>) =
        dao.upsertRatings(ratings)

    suspend fun getRatingByImdbId(imdbId: String?): ImdbRatingEntity? {
        if (imdbId == null) return null
        return dao.getRatingByImdbId(imdbId)
    }
    suspend fun getRatingByImdbIdBatch(imdbIds: List<String>): List<ImdbRatingEntity> {
        if (imdbIds.isEmpty()) return emptyList()
        return dao.getRatingsByImdbIdBatch(imdbIds)
    }

    suspend fun getRatingByTmdbId(tmdbId: Int?): ImdbRatingEntity? {
        if (tmdbId == null) return null
        return dao.getRatingByTmdbId(tmdbId)
    }
    suspend fun getRatingByTmdbIdBatch(tmdbIds: List<Int>): List<ImdbRatingEntity> {
        if (tmdbIds.isEmpty()) return emptyList()
        return dao.getRatingsByTmdbIdBatch(tmdbIds)
    }

    suspend fun linkImdbToTmdb(imdbId: String?, tmdbId: Int?) {
        if (imdbId != null && tmdbId != null) {
            dao.linkImdbToTmdbTransaction(imdbId, tmdbId)
        }
    }


    suspend fun clearAllRatings() =
        dao.clearAllRatings()

}