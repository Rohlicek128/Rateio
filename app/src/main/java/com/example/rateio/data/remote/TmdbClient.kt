package com.example.rateio.data.remote

import com.example.rateio.data.remote.imdb.ImdbService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object TmdbClient {
    private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    private const val IMDB_BASE_URL = "https://api.imdbapi.dev/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val tmdb: TmdbService = Retrofit.Builder()
        .baseUrl(TMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TmdbService::class.java)

    val imdb: ImdbService = Retrofit.Builder()
        .baseUrl(IMDB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImdbService::class.java)
}