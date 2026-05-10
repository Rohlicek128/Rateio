package com.example.rateio.data.remote.steam

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface SteamApiService {
    @GET("ISteamChartsService/GetMostPlayedGames/v1/")
    suspend fun getMostPlayedGames(
        @Header("accept") accept: String = "application/json",
    ): SteamMostPlayed
}