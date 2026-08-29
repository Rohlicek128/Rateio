package com.rohlicek.rateio.data.remote.tmdb

import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.components.CarouselImage
import com.google.gson.annotations.SerializedName
import com.rohlicek.rateio.presentation.leaderboard.DiscoverSortBy
import com.rohlicek.rateio.utils.formatDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale


data class TmdbShowSearchResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<TmdbShow>,
)

data class TmdbShow(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("popularity") val popularity: Float?,
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
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("popularity") val popularity: Float?,

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

data class TmdbSeasonDetail(
    @SerializedName("id")             val id: Int,
    @SerializedName("season_number")  val seasonNumber: Int,
    @SerializedName("episodes")       val episodes: List<TmdbEpisodeSummary>,
    @SerializedName("overview")       val overview: String?,
    @SerializedName("vote_average")   val voteAverage: Float?,
    @SerializedName("air_date")       val airDate: String?,
    @SerializedName("poster_path")   val posterPath: String?,
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
    @SerializedName("popularity") val popularity: Float,
)

data class TmdbCrewMember(
    @SerializedName("id") val id: Int,
    @SerializedName("credit_id") val creditId: String,
    @SerializedName("department") val department: String,
    @SerializedName("job") val job: String,
    @SerializedName("name") val name: String,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("popularity") val popularity: Float,
)

data class TmdbCreator(
    @SerializedName("id")           val id: Int,
    @SerializedName("credit_id")    val creditId: String,
    @SerializedName("name")         val name: String,
    @SerializedName("profile_path") val profilePath: String?,
)


data class TmdbEpisodeSummary(
    @SerializedName("id")             val id: Int,
    @SerializedName("name")           val name: String,
    @SerializedName("overview")       val overview: String?,
    @SerializedName("runtime")        val runtime: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number")  val seasonNumber: Int,
    @SerializedName("vote_average")   val voteAverage: Float?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("still_path")     val stillPath: String?,
    @SerializedName("air_date")       val airDate: String?,
) {
    override fun equals(other: Any?) = other is TmdbEpisodeSummary && other.id == id
    override fun hashCode() = id.hashCode()
}

data class TmdbEpisodeDetail(
    @SerializedName("id")             val id: Int,
    @SerializedName("name")           val name: String,
    @SerializedName("overview")       val overview: String?,
    @SerializedName("runtime")        val runtime: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number")  val seasonNumber: Int,
    @SerializedName("vote_average")   val voteAverage: Float?,
    @SerializedName("vote_count") val voteCount: Int?,
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

data class TmdbNetwork(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("logo_path") val logoPath: String?,
    @SerializedName("origin_country") val originCountry: String?,
)

data class TmdbEpisodeGroupsResponse(
    @SerializedName("id") val showId: Int,
    @SerializedName("results") val results: List<TmdbEpisodeGroup>,
)

data class TmdbEpisodeGroup(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: Int?,

    @SerializedName("episode_count") val episodeCount: Int?,
    @SerializedName("group_count") val seasonCount: Int?,
    @SerializedName("network") val network: TmdbNetwork?,
)

data class TmdbEpisodeGroupResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: Int?,

    @SerializedName("episode_count") val episodeCount: Int?,
    @SerializedName("group_count") val seasonCount: Int?,
    @SerializedName("network") val network: TmdbNetwork?,

    @SerializedName("groups") val groups: List<TmdbEpisodeGroupDetails>?,
)
data class TmdbEpisodeGroupDetails(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("order") val order: Int?,
    @SerializedName("episodes") val episodes: List<TmdbEpisodeGroupEpisode>?,
)
data class TmdbEpisodeGroupEpisode(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("still_path") val stillPath: String?,
    @SerializedName("air_date") val airDate: String?,
    @SerializedName("runtime") val runtime: Int,

    @SerializedName("episode_number") val episodeNumber: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    @SerializedName("production_code") val productionCode: String?,

    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("order") val order: Int?,
) {
    override fun equals(other: Any?) = other is TmdbEpisodeGroupEpisode && other.id == id
    override fun hashCode() = id.hashCode()
}



data class TmdbMovieSearchResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<TmdbMovie>
)

data class TmdbMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,

    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("genre_ids") val genreIds: List<String>,

    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("popularity") val popularity: Float?,
)

data class TmdbMovieDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("imdb_id") val imdbId: String?,

    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("vote_count") val voteCount: Int?,
    @SerializedName("genres") val genres: List<TmdbGenre>,
    @SerializedName("runtime") val runtime: Int,

    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    @SerializedName("origin_country") val originCountry: List<String>,

    @SerializedName("revenue") val revenue: Long,
    @SerializedName("budget") val budget: Int,
    @SerializedName("popularity") val popularity: Float?,

    @SerializedName("credits") val credits: TmdbCredits?,
)


data class TmdbPersonDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("imdb_id") val imdbId: String?,

    @SerializedName("name") val name: String,
    @SerializedName("biography") val biography: String?,
    @SerializedName("profile_path") val profilePath: String?,

    @SerializedName("birthday") val birthday: String?,
    @SerializedName("place_of_birth") val placeOfBirth: String?,
    @SerializedName("deathday") val deathday: String?,

    @SerializedName("known_for_department") val knownForDepartment: String?,
    @SerializedName("popularity") val popularity: Float?,

    @SerializedName("images") val images: TmdbPersonImageResponse?,
    @SerializedName("combined_credits") val combinedCredits: TmdbPersonCreditsResponse?,
)

data class TmdbPersonImageResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("profiles") val profiles: List<TmdbImage>,
)

data class TmdbPersonCreditsResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("cast") val cast: List<TmdbCastDetail>,
)

data class TmdbCastDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("credit_id") val creditId: String,
    @SerializedName("media_type") val mediaType: String,

    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("original_name") val originalName: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Float?,
    @SerializedName("character") val character: String?,

    @SerializedName("popularity") val popularity: Float?,
)

data class TmdbReviews(
    @SerializedName("id") val id: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("results") val results: List<TmdbAuthor>,
)

data class TmdbAuthor(
    @SerializedName("id") val id: String,
    @SerializedName("author") val author: String,
    @SerializedName("author_details") val authorDetails: TmdbAuthorDetail?,
    @SerializedName("content") val content: String,

    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("created_at") val createdAt: String,
)
data class TmdbAuthorDetail(
    @SerializedName("name") val name: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("avatar_path") val avatarPath: String?,
    @SerializedName("rating") val rating: Float?,
)


data class TmdbListResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("results") val results: List<TmdbList>,
)

data class TmdbList(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("iso_639_1") val languageIso: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("item_count") val itemCount: Int?,
    @SerializedName("favorite_count") val favoriteCount: Int?,
)

data class TmdbListDetail(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("iso_639_1") val languageIso: String?,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("poster_path") val posterPath: String?,

    @SerializedName("item_count") val itemCount: Int?,
    @SerializedName("favorite_count") val favoriteCount: Int?,

    @SerializedName("items") val items: List<TmdbMovie>,
)


@Serializable
data class TmdbShowMetadata(
    val showSpoilers: Boolean = true,
    val showSpoilersName: Boolean = true,
)

@Serializable
data class TmdbEpisodeMetadata(
    val showId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val seasonEpisodeCount: Int? = null,

    val runtime: Int? = null,
    val imdbId: String? = null, // Deprecated
)

fun TmdbEpisodeGroupEpisode.toEpisodeSummary() = TmdbEpisodeSummary(
    id = id,
    name = name,
    overview = overview,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    runtime = runtime,
    stillPath = stillPath,
    airDate = airDate,
    voteAverage = voteAverage,
    voteCount = voteCount,
)


fun TmdbShow.toRateItem(categoryId: Long = 0, weight: Float = 1f, subtitleOverride: DiscoverSortBy? = null) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = name,
    subtitle = when (subtitleOverride) {
        DiscoverSortBy.RELEASE_DATE -> formatDate(firstAirDate)
        DiscoverSortBy.POPULARITY -> popularity?.let { "%.1f".format(Locale.US, it) } ?: "N/A"
        else -> firstAirDate?.take(4)
    },
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    backdropImageUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
    backdropImageLowUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w300$it" },
    rating = voteAverage?.div(10f),
    ratingWeight = weight,
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_SHOWS,
)
fun TmdbShowDetail.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = name,
    subtitle = firstAirDate?.take(4),
    length = numberOfEpisodes.toFloat(),
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    backdropImageUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
    backdropImageLowUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w300$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_SHOWS,
)
fun TmdbSeasonDetail.toRateItem(categoryId: Long = 0, parentId: Long) = RateItem(
    id = 0,
    categoryId = categoryId,
    parentId = parentId,
    title = if (seasonNumber > 0) "Season $seasonNumber" else "Specials",
    subtitle = "${episodes.size} episodes",
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    length = episodes.size.toFloat(),
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_SEASONS,
)
fun TmdbEpisodeDetail.toRateItem(categoryId: Long = 0, showId: Int, parentId: Long, seasonEpisodeCount: Int? = null) = RateItem(
    id = 0,
    categoryId = categoryId,
    parentId = parentId,
    title = name,
    subtitle = "$showId $seasonNumber $episodeNumber",
    length = runtime.toFloat(),
    coverImageUrl = stillPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = stillPath?.let { "https://image.tmdb.org/t/p/w300$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_EPISODES,
    metadataJSON = Json.encodeToString(TmdbEpisodeMetadata(
        showId = showId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        seasonEpisodeCount = seasonEpisodeCount,
        imdbId = externalIds?.imdbId,
    ))
)

fun TmdbEpisodeSummary.toRateItem(categoryId: Long = 0, showId: Int, parentId: Long, seasonEpisodeCount: Int? = null) = RateItem(
    id = 0,
    categoryId = categoryId,
    parentId = parentId,
    title = name,
    subtitle = "$showId $seasonNumber $episodeNumber",
    length = runtime.toFloat(),
    coverImageUrl = stillPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = stillPath?.let { "https://image.tmdb.org/t/p/w300$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_EPISODES,
    metadataJSON = Json.encodeToString(TmdbEpisodeMetadata(
        showId = showId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        seasonEpisodeCount = seasonEpisodeCount,
    ))
)


fun TmdbMovie.toRateItem(categoryId: Long = 0, weight: Float = 1f, subtitleOverride: DiscoverSortBy? = null) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = title ?: "N/A",
    subtitle = when (subtitleOverride) {
        DiscoverSortBy.RELEASE_DATE -> formatDate(releaseDate)
        DiscoverSortBy.POPULARITY -> popularity?.let { "%.1f".format(Locale.US, it) } ?: "N/A"
        else -> releaseDate?.take(4)
    },
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    backdropImageUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
    backdropImageLowUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w300$it" },
    rating = voteAverage?.div(10f),
    ratingWeight = weight,
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_MOVIES,
)
fun TmdbMovieDetail.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = title,
    subtitle = releaseDate?.take(4),
    length = runtime.toFloat(),
    coverImageUrl = posterPath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
    backdropImageUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
    backdropImageLowUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w300$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_MOVIES,
)

fun TmdbImage.toCarouselImage() = CarouselImage(
    filePath = filePath,
    aspectRatio = aspectRatio,
)

fun TmdbPersonDetail.toRateItem(categoryId: Long = 0) = RateItem(
    id = 0,
    categoryId = categoryId,
    title = name,
    subtitle = knownForDepartment,
    coverImageUrl = profilePath?.let { "https://image.tmdb.org/t/p/original$it" },
    coverImageLowUrl = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
    externalId = id.toString(),
    externalSource = CategoryType.TMDB_PEOPLE,
)