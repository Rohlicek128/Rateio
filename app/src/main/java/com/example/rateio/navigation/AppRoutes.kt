package com.example.rateio.navigation

import kotlinx.serialization.Serializable


sealed interface Route {
    @Serializable sealed class TopLevel : Route {
        @Serializable data object Home : TopLevel()
        @Serializable data object Browse : TopLevel()
        @Serializable data object Profile : TopLevel()
    }

    @Serializable data class CategoryDetail(val categoryId: Long) : Route

    @Serializable data class RateItemDetail(val itemId: Long) : Route

    @Serializable data class TmdbMovieDetail(val movieId: Int) : Route

    @Serializable data class TmdbShowDetail(val showId: Int) : Route
    @Serializable data class TmdbEpisodeDetail(val showId: Int, val season: Int, val episode: Int) : Route

    @Serializable data class TmdbPersonDetail(val personId: Int) : Route

    @Serializable data class SteamGameDetail(val appId: String) : Route
}
