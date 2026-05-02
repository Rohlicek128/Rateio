package com.example.rateio.data.remote.imdb

import com.example.rateio.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


interface ImdbService {
    @GET("titles/{imdbId}")
    suspend fun getTitle(
        @Path("imdbId") imdbId: String,
        @Header("accept") accept: String = "application/json",
    ): ImdbTitleDetails
}