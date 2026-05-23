package com.example.rateio.data.remote.imdb

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


interface ImdbService {
    @GET("titles/{imdbId}")
    suspend fun getTitle(
        @Path("imdbId") imdbId: String,
        @Header("accept") accept: String = "application/json",
    ): ImdbTitleDetails

    @GET
    suspend fun batchGetTitles(
        @Url url: String,
    ): ImdbBatchResponse

    @GET("titles/{titleId}/episodes")
    suspend fun getEpisodes(
        @Path("titleId") titleId: String,
        @Query("season") season: String? = null,
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null,
    ): ImdbEpisodesResponse
}