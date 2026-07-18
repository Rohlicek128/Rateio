package com.rohlicek.rateio.data.remote.steam

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


interface SteamCommunityService {
    @GET("actions/SearchApps/{query}")
    suspend fun searchGames(
        @Path("query") query: String,
        @Header("accept") accept: String = "application/json",
    ): List<SteamGameSummary>
}