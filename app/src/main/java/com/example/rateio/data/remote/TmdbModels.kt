package com.example.rateio.data.remote

import com.google.gson.annotations.SerializedName


data class TmdbSearchResponse(
    @SerializedName("results") val results: List<TmdbShow>
)

data class TmdbShow(
    @SerializedName("id")            val id: Int,
    @SerializedName("name")          val name: String,
    @SerializedName("overview")      val overview: String?,
    @SerializedName("poster_path")   val posterPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average")  val voteAverage: Float?,
)

data class TmdbShowDetail(
    @SerializedName("id")            val id: Int,
    @SerializedName("name")          val name: String,
    @SerializedName("overview")      val overview: String?,
    @SerializedName("poster_path")   val posterPath: String?,
    @SerializedName("backdrop_path")   val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average")  val voteAverage: Float?,
    @SerializedName("status")        val status: String?,
    @SerializedName("genres")        val genres: List<TmdbGenre>,
    @SerializedName("seasons")       val seasons: List<TmdbSeason>,
    @SerializedName("credits")       val credits: TmdbCredits?,
)

data class TmdbGenre(
    @SerializedName("id")   val id: Int,
    @SerializedName("name") val name: String,
)

data class TmdbSeason(
    @SerializedName("id")            val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("episode_count") val episodeCount: Int,
    @SerializedName("air_date")      val airDate: String?,
    @SerializedName("poster_path")   val posterPath: String?,
)

data class TmdbCredits(
    @SerializedName("cast") val cast: List<TmdbCastMember>,
)

data class TmdbCastMember(
    @SerializedName("name")         val name: String,
    @SerializedName("character")    val character: String,
    @SerializedName("profile_path") val profilePath: String?,
)