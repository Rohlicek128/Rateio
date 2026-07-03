package com.example.rateio.model

enum class CategoryType {
    TMDB_SHOWS,
    TMDB_SEASONS,
    TMDB_EPISODES,
    TMDB_MOVIES,
    TMDB_PEOPLE,
    STEAM_GAMES,
    OPEN_LIBRARY_BOOKS,
    OPEN_LIBRARY_CHAPTER,
    CUSTOM,
}
fun CategoryType.label() = when (this) {
    CategoryType.TMDB_SHOWS -> "Shows"
    CategoryType.TMDB_SEASONS -> "Seasons"
    CategoryType.TMDB_EPISODES -> "Episodes"
    CategoryType.TMDB_MOVIES -> "Movies"
    CategoryType.TMDB_PEOPLE -> "People"
    CategoryType.STEAM_GAMES -> "Games"
    CategoryType.OPEN_LIBRARY_BOOKS -> "Books"
    CategoryType.OPEN_LIBRARY_CHAPTER -> "Chapter"
    CategoryType.CUSTOM -> "Custom"
    else -> "Unknown"
}

data class Category(
    val id: Long = 0,
    val type: CategoryType,
    val name: String,
    val sortOrder: Int = 0,
)