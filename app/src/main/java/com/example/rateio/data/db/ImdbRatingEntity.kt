package com.example.rateio.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "imdb_ratings")
data class ImdbRatingEntity(
    @PrimaryKey val tconst: String,
    val averageRating: Float,
    val numVotes: Int,
)