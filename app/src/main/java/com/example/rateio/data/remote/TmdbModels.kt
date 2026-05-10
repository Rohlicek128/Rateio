package com.example.rateio.data.remote

import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.CarouselImage
import com.google.gson.annotations.SerializedName


data class TmdbShowSearchResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<TmdbShow>,
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
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,

    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("last_air_date") val lastAirDate: String?,
    @SerializedName("last_episode_to_air") val lastEpisodeToAir: TmdbEpisodeSummary?,
    @SerializedName("next_episode_to_air") val nextEpisodeToAir: TmdbEpisodeSummary?,

    @SerializedName("episode_run_time") val episodeRuntime: List<Int>,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int,
    @SerializedName("number_of_seasons") val numberOfSeasons: Int,

    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("status") val status: String?,
    @SerializedName("genres") val genres: List<TmdbGenre>,
    @SerializedName("seasons") val seasons: List<TmdbSeason>,
    @SerializedName("created_by") val createdBy: List<TmdbCreator>,
    @SerializedName("credits") val credits: TmdbCredits?,
    @SerializedName("external_ids") val externalIds: TmdbExternalIds?,
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
    @SerializedName("guest_stars") val guest: List<TmdbCastMember>,
    @SerializedName("crew") val crew: List<TmdbCrewMember>,
)

data class TmdbCastMember(
    @SerializedName("id") val id: Int,
    @SerializedName("credit_id") val creditId: String,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?,
)

data class TmdbCrewMember(
    @SerializedName("id") val id: Int,
    @SerializedName("credit_id") val creditId: String,
    @SerializedName("department") val department: String,
    @SerializedName("job") val job: String,
    @SerializedName("name") val name: String,
    @SerializedName("profile_path") val profilePath: String?,
)

data class TmdbCreator(
    @SerializedName("id")           val id: Int,
    @SerializedName("credit_id")    val creditId: String,
    @SerializedName("name")         val name: String,
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


data class TmdbImageResponse(
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



data class TmdbMovieSearchResponse(
    @SerializedName("results") val results: List<TmdbMovie>
)

data class TmdbMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("original_language") val originalLanguage: String?,
)

data class TmdbMovieDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("imdb_id") val imdbId: String?,

    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("genres") val genres: List<TmdbGenre>,
    @SerializedName("runtime") val runtime: Int,

    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("origin_country") val originCountry: List<String>,

    @SerializedName("revenue") val revenue: Long,
    @SerializedName("budget") val budget: Int,

    @SerializedName("credits") val credits: TmdbCredits?,
)




fun TmdbShow.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = name,
    subtitle = firstAirDate?.take(4),
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_SHOWS,
)
fun TmdbShowDetail.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = name,
    subtitle = firstAirDate?.take(4),
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_SHOWS,
)

fun TmdbMovie.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = title,
    subtitle = releaseDate?.take(4),
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_MOVIES,
)
fun TmdbMovieDetail.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = title,
    subtitle = releaseDate?.take(4),
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_MOVIES,
)

fun TmdbImage.toCarouselImage() = CarouselImage(
    filePath = filePath,
    aspectRatio = aspectRatio,
)