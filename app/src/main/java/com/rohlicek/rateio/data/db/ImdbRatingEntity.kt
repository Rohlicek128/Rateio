package com.rohlicek.rateio.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "imdb_ratings",
    indices = [Index(value = ["tmdbId"])]
)
data class ImdbRatingEntity(
    @PrimaryKey val tconst: String,
    val averageRating: Float,
    val numVotes: Int,
    val tmdbId: Int? = null,
)