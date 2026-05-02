package com.example.rateio.data.remote.imdb

import com.google.gson.annotations.SerializedName


data class ImdbTitleDetails(
    @SerializedName("id") val id: String,
    @SerializedName("primaryTitle") val primaryTitle: String,
    @SerializedName("rating") val rating: ImdbRating,
)

data class ImdbRating(
    @SerializedName("aggregateRating") val aggregateRating: Float?,
    @SerializedName("voteCount") val voteCount: Int?,
) {
    val normalizedRating: Float?
        get() = aggregateRating?.div(10f)
}