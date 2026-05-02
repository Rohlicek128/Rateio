package com.example.rateio.data.remote

import com.example.rateio.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface TmdbService {
    @GET("search/tv")
    suspend fun searchShows(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        //@Query("append_to_response") append: String = "external_ids",
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbSearchResponse

    @GET("tv/{id}")
    suspend fun getShow(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits,external_ids",
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbShowDetail


    @GET("tv/{showId}/season/{seasonNumber}")
    suspend fun getSeason(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Query("language") language: String = "en-US",
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbSeasonDetail


    @GET("tv/{showId}/season/{seasonNumber}/episode/{episodeNumber}")
    suspend fun getEpisode(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Path("episodeNumber") episodeNumber: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits,external_ids",
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbEpisodeDetail


    @GET("tv/{showId}/images")
    suspend fun getShowImages(
        @Path("showId") showId: Int,
        @Query("include_image_language") language: String = "en-US,null",
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbShowImageResponse

    @GET("tv/{showId}/season/{seasonNumber}/episode/{episodeNumber}/images")
    suspend fun getEpisodeImages(
        @Path("showId")       showId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Path("episodeNumber") episodeNumber: Int,
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbEpisodeImageResponse
}