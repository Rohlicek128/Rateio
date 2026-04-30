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
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbSearchResponse

    @GET("tv/{id}")
    suspend fun getShow(
        @Path("id") id: Int,
        @Query("language") language: String = "en-US",
        @Query("append_to_response") append: String = "credits",
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") bearer: String = "Bearer " + BuildConfig.TMDB_API_KEY,
    ): TmdbShowDetail
}