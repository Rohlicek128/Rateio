package com.example.rateio.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ImdbRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRatings(ratings: List<ImdbRatingEntity>)

    @Query("SELECT * FROM imdb_ratings WHERE tconst = :tconst")
    suspend fun getRating(tconst: String): ImdbRatingEntity?

    @Query("SELECT * FROM imdb_ratings WHERE tconst IN (:tconsts)")
    suspend fun getRatingsBatch(tconsts: List<String>): List<ImdbRatingEntity>

    @Query("DELETE FROM imdb_ratings")
    suspend fun clearAllRatings()
}