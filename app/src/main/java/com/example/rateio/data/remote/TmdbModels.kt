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
    @SerializedName("origin_country") val originCountry: List<String>,
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
    @SerializedName("external_ids")  val externalIds: TmdbExternalIds?,
)

data class TmdbExternalIds(
    @SerializedName("imdb_id") val imdbId: String?,
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
    @SerializedName("vote_average")  val voteAverage: Float?,
)

data class TmdbCredits(
    @SerializedName("cast") val cast: List<TmdbCastMember>,
)

data class TmdbCastMember(
    @SerializedName("id")           val id: Int,
    @SerializedName("credit_id")    val creditId: String,
    @SerializedName("name")         val name: String,
    @SerializedName("character")    val character: String,
    @SerializedName("profile_path") val profilePath: String?,
)


data class TmdbSeasonDetail(
    @SerializedName("id")             val id: Int,
    @SerializedName("season_number")  val seasonNumber: Int,
    @SerializedName("episodes")       val episodes: List<TmdbEpisodeSummary>,
)

data class TmdbEpisodeSummary(
    @SerializedName("id")             val id: Int,
    @SerializedName("name")           val name: String,
    @SerializedName("overview")       val overview: String?,
    @SerializedName("runtime")        val runtime: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number")  val seasonNumber: Int,
    @SerializedName("vote_average")   val voteAverage: Float?,
    @SerializedName("still_path")     val stillPath: String?,
    @SerializedName("air_date")       val airDate: String?,
)

data class TmdbEpisodeDetail(
    @SerializedName("id")             val id: Int,
    @SerializedName("name")           val name: String,
    @SerializedName("overview")       val overview: String?,
    @SerializedName("runtime")        val runtime: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number")  val seasonNumber: Int,
    @SerializedName("vote_average")   val voteAverage: Float?,
    @SerializedName("still_path")     val stillPath: String?,
    @SerializedName("air_date")       val airDate: String?,
    @SerializedName("production_code")val productionCode: String?,
    @SerializedName("credits")       val credits: TmdbCredits?,
    @SerializedName("external_ids")  val externalIds: TmdbExternalIds?,
)


data class TmdbShowImageResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("backdrops") val backdrops: List<TmdbImage>,
    @SerializedName("posters") val posters: List<TmdbImage>,
)

data class TmdbEpisodeImageResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("stills") val stills: List<TmdbImage>,
)

data class TmdbImage(
    @SerializedName("file_path") val filePath: String,
    @SerializedName("aspect_ratio") val aspectRatio: Float,
    @SerializedName("vote_count") val voteCount: Int,
)