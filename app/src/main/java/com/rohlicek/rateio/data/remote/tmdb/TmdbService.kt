package com.rohlicek.rateio.data.remote.tmdb

import com.rohlicek.rateio.model.HasDisplayName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


enum class TmdbTimeWindow(override val displayName: String): HasDisplayName {
    DAY("Day"),
    WEEK("Week")
}

interface TmdbService {
    // Shows
    @GET("search/tv")
    suspend fun searchShows(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        //@Query("append_to_response") append: String = "external_ids",
    ): TmdbShowSearchResponse

    @GET("discover/tv")
    suspend fun discoverShows(
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",

        @Query("air_date.gte") airDateGte: String? = null,
        @Query("air_date.lte") airDateLte: String? = null,

        @Query("vote_count.gte") minVoteCount: Int = 100,
        @Query("vote_average.gte") minVoteAverage: Double = 6.0,

        @Query("without_genres") withoutGenres: String = "99,10763,10764,10767,10766",
        //@Query("with_status") withStatus: String? = "0,2,3",
    ): TmdbShowSearchResponse

    @GET("discover/tv")
    suspend fun topRatedShows(
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_count.gte") voteCountGte: Float = 200f,
        @Query("without_genres") withoutGenres: String = "99,10755",
    ): TmdbShowSearchResponse

    @GET("trending/tv/{time_window}")
    suspend fun trendingShows(
        @Path("time_window") timeWindow: String,
        @Query("language") language: String = "en-US",
    ): TmdbShowSearchResponse

    @GET("tv/{id}")
    suspend fun getShow(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits,external_ids",
    ): TmdbShowDetail


    @GET("tv/{showId}/season/{seasonNumber}")
    suspend fun getSeason(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Query("language") language: String = "en-US",
    ): TmdbSeasonDetail


    @GET("tv/{showId}/season/{seasonNumber}/episode/{episodeNumber}")
    suspend fun getEpisode(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Path("episodeNumber") episodeNumber: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits,external_ids",
    ): TmdbEpisodeDetail


    @GET("tv/{showId}/images")
    suspend fun getShowImages(
        @Path("showId") showId: Int,
        @Query("include_image_language") language: String = "en-US,null",
    ): TmdbImageResponse

    @GET("tv/{showId}/season/{seasonNumber}/episode/{episodeNumber}/images")
    suspend fun getEpisodeImages(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Path("episodeNumber") episodeNumber: Int,
    ): TmdbEpisodeImageResponse

    @GET("tv/{id}/reviews")
    suspend fun getShowReviews(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbReviews

    @GET("tv/{id}/external_ids")
    suspend fun getShowExternalIds(
        @Path("id") id: Int,
    ): TmdbExternalIds

    @GET("tv/{showId}/season/{seasonNumber}/episode/{episodeNumber}/external_ids")
    suspend fun getEpisodeExternalIds(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Path("episodeNumber") episodeNumber: Int,
    ): TmdbExternalIds



    // Movies
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        //@Query("append_to_response") append: String = "external_ids",
    ): TmdbMovieSearchResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",

        @Query("primary_release_date.gte") releaseDateGte: String? = null,
        @Query("primary_release_date.lte") releaseDateLte: String? = null,

        @Query("vote_count.gte") minVoteCount: Int = 150,
        @Query("vote_average.gte") minVoteAverage: Double = 5.5,

        @Query("with_release_type") withReleaseType: String = "2|3|4",
        @Query("without_genres") withoutGenres: String = "10770",
    ): TmdbMovieSearchResponse

    @GET("discover/movie")
    suspend fun topRatedMovies(
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_count.gte") voteCountGte: Float = 200f,
        @Query("without_genres") withoutGenres: String = "99,10755",
    ): TmdbMovieSearchResponse

    @GET("trending/movie/{time_window}")
    suspend fun trendingMovies(
        @Path("time_window") timeWindow: String,
        @Query("language") language: String = "en-US",
    ): TmdbMovieSearchResponse

    @GET("movie/{id}")
    suspend fun getMovie(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits",
    ): TmdbMovieDetail

    @GET("movie/{id}/images")
    suspend fun getMovieImages(
        @Path("id") id: Int,
        @Query("include_image_language") language: String = "en-US,null",
    ): TmdbImageResponse

    @GET("movie/{id}/reviews")
    suspend fun getMovieReviews(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbReviews

    @GET("movie/{id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
    ): TmdbMovieSearchResponse

    @GET("movie/{id}/external_ids")
    suspend fun getMovieExternalIds(
        @Path("id") id: Int,
    ): TmdbExternalIds



    // People
    @GET("person/{id}")
    suspend fun getPerson(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "images,combined_credits",
    ): TmdbPersonDetail


    @GET("tv/{id}/external_ids")
    suspend fun getTvExternalIds(
        @Path("id") id: Int,
    ): TmdbExternalIds
}