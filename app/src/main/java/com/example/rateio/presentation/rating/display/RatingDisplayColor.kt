package com.example.rateio.presentation.rating.display

import androidx.compose.ui.graphics.Color


data class RatingDisplayColor(
    val backgroundColor: Color,
    val foregroundColor: Color,
    val label: String,
)

fun getRatingColor(rating: Float?): RatingDisplayColor {
    return getColorSchemeImdbEpisodes(rating)
}

private fun getColorSchemeImdbEpisodes(rating: Float?): RatingDisplayColor {
    val whiteText = Color(0xFFFFFFFF)
    val blackText = Color(0xFF181818)
    return when {
        rating == null -> RatingDisplayColor(Color(0xFF4A4A4A), Color(0xFFD2D2D2), "N/A")
        rating >= 0.96f -> RatingDisplayColor(Color(0xFF1DA1F2), whiteText, "Masterpiece")
        rating >= 0.90f -> RatingDisplayColor(Color(0xFF186A3B), whiteText, "Awesome")
        rating >= 0.80f -> RatingDisplayColor(Color(0xFF28B463), blackText, "Great")
        rating >= 0.70f -> RatingDisplayColor(Color(0xFFF4D03F), blackText, "Good")
        rating >= 0.60f -> RatingDisplayColor(Color(0xFFF39C12), blackText, "Average")
        rating >= 0.41f -> RatingDisplayColor(Color(0xFFE74C3C), whiteText, "Bad")
        else -> RatingDisplayColor(Color(0xFF633974), whiteText, "Garbage")
    }
}

private fun getColorSchemeImdbMovies(rating: Float?): RatingDisplayColor {
    val whiteText = Color(0xFFFFFFFF)
    val blackText = Color(0xFF181818)
    return when {
        rating == null -> RatingDisplayColor(Color(0xFF5F5F5F), Color(0xFFBABABA), "N/A")
        rating >= 0.80f -> RatingDisplayColor(Color(0xFF1DA1F2), whiteText, "Masterpiece")
        rating >= 0.75f -> RatingDisplayColor(Color(0xFF186A3B), whiteText, "Awesome")
        rating >= 0.70f -> RatingDisplayColor(Color(0xFF28B463), blackText, "Great")
        rating >= 0.65f -> RatingDisplayColor(Color(0xFFF4D03F), blackText, "Good")
        rating >= 0.55f -> RatingDisplayColor(Color(0xFFF39C12), blackText, "Average")
        rating >= 0.40f -> RatingDisplayColor(Color(0xFFE74C3C), whiteText, "Bad")
        else -> RatingDisplayColor(Color(0xFF633974), whiteText, "Garbage")
    }
}

private fun getColorSchemeDecadic(rating: Float?): RatingDisplayColor {
    val whiteText = Color(0xFFFFFFFF)
    val blackText = Color(0xFF181818)
    return when {
        rating == null -> RatingDisplayColor(Color(0xFF5F5F5F), Color(0xFFBABABA), "N/A")
        rating >= 0.9f -> RatingDisplayColor(Color(0xFF1DA1F2), whiteText, "Masterpiece")
        rating >= 0.8f -> RatingDisplayColor(Color(0xFF186A3B), whiteText, "Awesome")
        rating >= 0.7f -> RatingDisplayColor(Color(0xFF28B463), blackText, "Great")
        rating >= 0.6f -> RatingDisplayColor(Color(0xFFF4D03F), blackText, "Good")
        rating >= 0.5f -> RatingDisplayColor(Color(0xFFF39C12), blackText, "Average")
        rating >= 0.4f -> RatingDisplayColor(Color(0xFFE74C3C), whiteText, "Bad")
        else -> RatingDisplayColor(Color(0xFF633974), whiteText, "Garbage")
    }
}

private fun getColorSchemeSteam(rating: Float?): RatingDisplayColor {
    val whiteText = Color(0xFFFFFFFF)
    val blackText = Color(0xFF181818)
    return when {
        rating == null -> RatingDisplayColor(Color(0xFF4A4A4A), Color(0xFFD2D2D2), "N/A")
        rating >= 0.95f -> RatingDisplayColor(Color(0xFF5eb1e2), whiteText, "Overwhelmingly Positive")
        rating >= 0.80f -> RatingDisplayColor(Color(0xFF5eb1e2), whiteText, "Very Positive")
        rating >= 0.70f -> RatingDisplayColor(Color(0xFF5eb1e2), whiteText, "Mostly Positive")
        rating >= 0.40f -> RatingDisplayColor(Color(0xFFb39c72), whiteText, "Mixed")
        rating >= 0.20f -> RatingDisplayColor(Color(0xFFc85e2d), whiteText, "Mostly Negative")
        else -> RatingDisplayColor(Color(0xFFc85e2d), whiteText, "Very Negative")
    }
}

private fun getColorSchemeCsfd(rating: Float?): RatingDisplayColor {
    val whiteText = Color(0xFFFFFFFF)
    return when {
        rating == null -> RatingDisplayColor(Color(0xFF5F5F5F), Color(0xFFBABABA), "N/A")
        rating >= 0.7f -> RatingDisplayColor(Color(0xFFC81613), whiteText, "Nejlepší")
        rating >= 0.31f -> RatingDisplayColor(Color(0xFF658db4), whiteText, "Průměrný")
        else -> RatingDisplayColor(Color(0xFF494949), whiteText, "Nejslabší")
    }
}