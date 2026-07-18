package com.rohlicek.rateio.data.remote.steam

import retrofit2.http.GET
import retrofit2.http.Header


interface SteamApiService {
    @GET("ISteamChartsService/GetMostPlayedGames/v1/")
    suspend fun getMostPlayedGames(
        @Header("accept") accept: String = "application/json",
    ): SteamMostPlayed
}