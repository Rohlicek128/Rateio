package com.example.rateio.data.remote.imdb

import com.google.gson.annotations.SerializedName


data class ImdbTitleDetails(
    @SerializedName("id") val id: String,
    @SerializedName("primaryTitle") val primaryTitle: String,
    @SerializedName("rating") val rating: ImdbRating,
)


data class ImdbEpisodesResponse(
    @SerializedName("episodes") val episodes: List<ImdbEpisode>?,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("nextPageToken") val nextPageToken: String?,
)

data class ImdbEpisode(
    @SerializedName("id") val id: String,
    @SerializedName("season") val season: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("rating") val rating: ImdbRating?,
) {
    val seasonNumber: Int get() = season.trim().toInt()
}


data class ImdbBatchResponse(
    @SerializedName("titles") val titles: List<ImdbTitleDetails>,
)


data class ImdbRating(
    @SerializedName("aggregateRating") val aggregateRating: Float?,
    @SerializedName("voteCount") val voteCount: Int?,
) {
    val normalizedRating: Float? get() = aggregateRating?.div(10f)
}