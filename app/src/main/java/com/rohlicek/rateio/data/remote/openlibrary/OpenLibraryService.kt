package com.rohlicek.rateio.data.remote.openlibrary

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


private const val USER_AGENT = "Rateio (adam@tsv.cz)"

interface OpenLibraryService {
    @GET("/search.json")
    suspend fun searchWorks(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Header("accept") accept: String = "application/json",
        @Header("User-Agent") userAgent: String = USER_AGENT,
    ): OLWorksSearchResponse

    @GET("/works/{id}.json")
    suspend fun getWork(
        @Path("id") id: String,
        @Header("accept") accept: String = "application/json",
        @Header("User-Agent") userAgent: String = USER_AGENT,
    ): OLWorkDetail

    @GET("/works/{id}/editions.json")
    suspend fun getWorkEditions(
        @Path("id") id: String,
        @Header("accept") accept: String = "application/json",
        @Header("User-Agent") userAgent: String = USER_AGENT,
    ): OLWorksEditionsResponse

    @GET("/authors/{id}.json")
    suspend fun getAuthors(
        @Path("id") id: String,
        @Header("accept") accept: String = "application/json",
        @Header("User-Agent") userAgent: String = USER_AGENT,
    ): OLAuthorDetail
}