package com.example.rateio.data.remote.steam

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface SteamStoreService {
    @GET("api/appdetails")
    suspend fun getGames(
        @Query("appids") appids: String,
        @Query("cc") cc: String = "eu",
        @Query("filters") filters: String = "",
        @Header("accept") accept: String = "application/json",
    ): Map<String, SteamGameDetailResponse>

    @GET("appreviews/{appid}")
    suspend fun getGameReviews(
        @Path("appid") appid: String,
        @Query("json") json: String = "1",
        @Query("num_per_page") numPerPage: Int = 0,
        @Query("language") language: String = "all",
        @Query("purchase_type") purchaseType: String = "all",
        @Header("accept") accept: String = "application/json",
    ): SteamGameReviews
}