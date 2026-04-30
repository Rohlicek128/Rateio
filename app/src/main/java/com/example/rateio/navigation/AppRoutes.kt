package com.example.rateio.navigation

import kotlinx.serialization.Serializable


sealed interface Route {
    @Serializable sealed class TopLevel : Route {
        @Serializable data object Home : TopLevel()
        @Serializable data object Browse : TopLevel()
        @Serializable data object Profile : TopLevel()
    }

    @Serializable data class RateItemDetail(val itemId: Long) : Route

    @Serializable data class TmdbShowDetail(val showId: Int) : Route

    @Serializable data class CategoryDetail(val categoryId: Long) : Route
}
