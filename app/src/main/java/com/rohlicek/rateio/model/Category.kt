package com.rohlicek.rateio.model

interface HasDisplayName {
    val displayName: String
}

enum class CategoryType(override val displayName: String): HasDisplayName {
    TMDB_SHOWS("Shows"),
    TMDB_SEASONS("Seasons"),
    TMDB_EPISODES("Episodes"),
    TMDB_MOVIES("Movies"),
    TMDB_PEOPLE("People"),
    STEAM_GAMES("Games"),
    OPEN_LIBRARY_BOOKS("Books"),
    OPEN_LIBRARY_PART("Parts"),
    OPEN_LIBRARY_CHAPTER("Chapters"),
    CUSTOM("Custom"),
}

data class Category(
    val id: Long = 0,
    val type: CategoryType,
    val name: String,
    val sortOrder: Int = 0,
)