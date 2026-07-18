package com.rohlicek.rateio.data

import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.CategoryType


object CategoryRegistry {
    val all: List<Category> = listOf(
        Category(
            id = 0,
            type = CategoryType.TMDB_SHOWS,
            name = "Shows",
            sortOrder = 0,
        ),
        Category(
            id = 0,
            type = CategoryType.TMDB_MOVIES,
            name = "Movies",
            sortOrder = 1,
        ),
        Category(
            id = 0,
            type = CategoryType.STEAM_GAMES,
            name = "Games",
            sortOrder = 2,
        ),
        Category(
            id = 0,
            type = CategoryType.OPEN_LIBRARY_BOOKS,
            name = "Books",
            sortOrder = 3,
        ),
    )

    fun forType(type: CategoryType): Category? = all.firstOrNull { it.type == type }
}