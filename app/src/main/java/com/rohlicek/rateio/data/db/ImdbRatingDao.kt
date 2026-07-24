package com.rohlicek.rateio.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface ImdbRatingDao {

    @Query("""
        INSERT INTO imdb_ratings (tconst, averageRating, numVotes, tmdbId) 
        VALUES (:tconst, :averageRating, :numVotes, NULL)
        ON CONFLICT(tconst) DO UPDATE SET 
            averageRating = excluded.averageRating,
            numVotes = excluded.numVotes
    """)
    suspend fun upsertRating(tconst: String, averageRating: Float, numVotes: Int)

    @Transaction
    suspend fun upsertRatings(ratings: List<ImdbRatingEntity>) {
        for (rating in ratings) {
            upsertRating(rating.tconst, rating.averageRating, rating.numVotes)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatings(ratings: List<ImdbRatingEntity>)


    @Query("SELECT * FROM imdb_ratings WHERE tconst = :tconst")
    suspend fun getRatingByImdbId(tconst: String): ImdbRatingEntity?

    @Query("SELECT * FROM imdb_ratings WHERE tconst IN (:tconsts)")
    suspend fun getRatingsByImdbIdBatch(tconsts: List<String>): List<ImdbRatingEntity>


    @Query("SELECT * FROM imdb_ratings WHERE tmdbId = :tmdbId LIMIT 1")
    suspend fun getRatingByTmdbId(tmdbId: Int): ImdbRatingEntity?

    @Query("SELECT * FROM imdb_ratings WHERE tmdbId IN (:tmdbIds)")
    suspend fun getRatingsByTmdbIdBatch(tmdbIds: List<Int>): List<ImdbRatingEntity>

    @Query("UPDATE imdb_ratings SET tmdbId = :tmdbId WHERE tconst = :tconst")
    suspend fun updateTmdbId(tconst: String, tmdbId: Int)


    @Query("DELETE FROM imdb_ratings")
    suspend fun clearAllRatings()
}