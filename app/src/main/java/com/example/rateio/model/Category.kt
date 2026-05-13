package com.example.rateio.model

enum class CategoryType {
    TMDB_SHOWS,
    TMDB_SEASONS,
    TMDB_EPISODES,
    TMDB_MOVIES,
    STEAM_GAMES,
    CUSTOM,
}

data class Category(
    val id: Long = 0,
    val type: CategoryType,
    val name: String,
    val sortOrder: Int = 0,
)