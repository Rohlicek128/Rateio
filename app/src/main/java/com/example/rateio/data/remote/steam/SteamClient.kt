package com.example.rateio.data.remote.steam

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object SteamClient {
    private const val STEAM_BASE_URL = "https://steamcommunity.com/"
    private const val STEAM_STORE_URL = "https://store.steampowered.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val steam: SteamService = Retrofit.Builder()
        .baseUrl(STEAM_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SteamService::class.java)

    val steamStore: SteamService = Retrofit.Builder()
        .baseUrl(STEAM_STORE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SteamService::class.java)
}