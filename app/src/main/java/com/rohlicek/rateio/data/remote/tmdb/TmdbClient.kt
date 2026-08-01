package com.rohlicek.rateio.data.remote.tmdb

import com.rohlicek.rateio.BuildConfig
import com.rohlicek.rateio.data.preferences.SyncPreferences
import com.rohlicek.rateio.data.remote.imdb.depricated.ImdbService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class TmdbClient(private val preferences: SyncPreferences) {
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        val savedToken = runBlocking {
            preferences.tmdbApiToken.first()
        } //.ifBlank { BuildConfig.TMDB_API_KEY }

        val requestBuilder = originalRequest.newBuilder()
            .header("accept", "application/json")
            .header("Accept-Encoding", "identity")

        val finalRequest = if (savedToken.length < 50) {
            val newUrl = originalRequest.url.newBuilder()
                .addQueryParameter("api_key", savedToken)
                .build()
            requestBuilder.url(newUrl).build()
        } else {
            requestBuilder
                .header("Authorization", "Bearer $savedToken")
                .build()
        }

        chain.proceed(finalRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .build()

    val tmdb: TmdbService = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TmdbService::class.java)

    /*val imdb: ImdbService = Retrofit.Builder()
        .baseUrl("https://api.imdbapi.dev/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImdbService::class.java)*/
}