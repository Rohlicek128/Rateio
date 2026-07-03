package com.example.rateio.data.remote.openlibrary

import com.example.rateio.data.remote.tmdb.TmdbService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object OpenLibraryClient {
    private const val BASE_URL = "https://openlibrary.org"
    const val COVERS_BASE_URL = "https://covers.openlibrary.org/b"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val customGson = GsonBuilder()
        .registerTypeAdapter(OLTypeValue::class.java, OLTypeValueDeserializer())
        .create()

    val service: OpenLibraryService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(customGson))
        .build()
        .create(OpenLibraryService::class.java)
}