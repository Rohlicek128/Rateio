package com.example.rateio.data.remote.steam

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object SteamClient {
    private const val STEAM_COMMUNITY_URL = "https://steamcommunity.com/"
    private const val STEAM_STORE_URL = "https://store.steampowered.com/"
    private const val STEAM_API_URL = "https://api.steampowered.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val steamCommunity: SteamCommunityService = Retrofit.Builder()
        .baseUrl(STEAM_COMMUNITY_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SteamCommunityService::class.java)

    val steamStore: SteamStoreService = Retrofit.Builder()
        .baseUrl(STEAM_STORE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SteamStoreService::class.java)

    val steamApi: SteamApiService = Retrofit.Builder()
        .baseUrl(STEAM_API_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SteamApiService::class.java)
}