package com.rohlicek.rateio.presentation.rating.display

import androidx.compose.ui.graphics.Color
import kotlin.collections.forEach


data class RatingColorBucket(
    val equalOrGreaterThen: Float? = null,
    val backgroundColor: Color,
    val foregroundColor: Color,
    val label: String? = null,
)

data class RatingColorBuckets(
    val name: String,
    val buckets: List<RatingColorBucket>,
    val nullBucket: RatingColorBucket,
)

fun getRatingColor(
    rating: Float?,
    buckets: RatingColorBuckets = getCurrentRatingColorBuckets(),
    rtf: RatingTransformation = getCurrentRatingTransformations()
): RatingColorBucket {
    if (rating == null) return buckets.nullBucket
    val transformedRating = getRoundedRating(rating, rtf = rtf)!!

    buckets.buckets.forEach { if (it.equalOrGreaterThen != null && transformedRating >= it.equalOrGreaterThen) return it }
    return buckets.nullBucket
}

fun getCurrentRatingColorBuckets(): RatingColorBuckets {
    return RatingColorBucketConstants.RC_IMDB_EPISODES
}

object RatingColorBucketConstants {
    private val whiteText = Color(0xFFFFFFFF)
    private val blackText = Color(0xFF181818)
    private val nullBucket = RatingColorBucket(
        null, Color(0xFF4A4A4A), Color(0xFFD2D2D2), "N/A"
    )

    val RC_IMDB_MOVIES = RatingColorBuckets(
        name = "IMDb Movies",
        buckets = listOf(
            RatingColorBucket(0.80f, Color(0xFF1DA1F2), whiteText, "Masterpiece"),
            RatingColorBucket(0.75f, Color(0xFF186A3B), whiteText, "Awesome"),
            RatingColorBucket(0.70f, Color(0xFF28B463), blackText, "Great"),
            RatingColorBucket(0.65f, Color(0xFFF4D03F), blackText, "Good"),
            RatingColorBucket(0.55f, Color(0xFFF39C12), blackText, "Average"),
            RatingColorBucket(0.40f, Color(0xFFE74C3C), whiteText, "Bad"),
            RatingColorBucket(0.00f, Color(0xFF633974), whiteText, "Garbage"),
        ),
        nullBucket = nullBucket
    )
    val RC_IMDB_SHOWS = RatingColorBuckets(
        name = "IMDb Shows",
        buckets = listOf(
            RatingColorBucket(0.87f, Color(0xFF1DA1F2), whiteText, "Masterpiece"),
            RatingColorBucket(0.82f, Color(0xFF186A3B), whiteText, "Awesome"),
            RatingColorBucket(0.78f, Color(0xFF28B463), blackText, "Great"),
            RatingColorBucket(0.75f, Color(0xFFF4D03F), blackText, "Good"),
            RatingColorBucket(0.65f, Color(0xFFF39C12), blackText, "Average"),
            RatingColorBucket(0.45f, Color(0xFFE74C3C), whiteText, "Bad"),
            RatingColorBucket(0.00f, Color(0xFF633974), whiteText, "Garbage"),
        ),
        nullBucket = nullBucket
    )
    val RC_IMDB_EPISODES = RatingColorBuckets(
        name = "IMDb Episodes",
        buckets = listOf(
            RatingColorBucket(0.96f, Color(0xFF1DA1F2), whiteText, "Masterpiece"),
            RatingColorBucket(0.90f, Color(0xFF186A3B), whiteText, "Awesome"),
            RatingColorBucket(0.80f, Color(0xFF28B463), blackText, "Great"),
            RatingColorBucket(0.70f, Color(0xFFF4D03F), blackText, "Good"),
            RatingColorBucket(0.60f, Color(0xFFF39C12), blackText, "Average"),
            RatingColorBucket(0.41f, Color(0xFFE74C3C), whiteText, "Bad"),
            RatingColorBucket(0.00f, Color(0xFF633974), whiteText, "Garbage"),
        ),
        nullBucket = nullBucket
    )

    val RC_DECADIC = RatingColorBuckets(
        name = "Decadic",
        buckets = listOf(
            RatingColorBucket(0.9f, Color(0xFF1DA1F2), whiteText, "Masterpiece"),
            RatingColorBucket(0.8f, Color(0xFF186A3B), whiteText, "Awesome"),
            RatingColorBucket(0.7f, Color(0xFF28B463), blackText, "Great"),
            RatingColorBucket(0.6f, Color(0xFFF4D03F), blackText, "Good"),
            RatingColorBucket(0.5f, Color(0xFFF39C12), blackText, "Average"),
            RatingColorBucket(0.4f, Color(0xFFE74C3C), whiteText, "Bad"),
            RatingColorBucket(0.0f, Color(0xFF633974), whiteText, "Garbage"),
        ),
        nullBucket = nullBucket
    )
    val RC_STEAM = RatingColorBuckets(
        name = "Steam",
        buckets = listOf(
            RatingColorBucket(0.95f, Color(0xFF5eb1e2), whiteText, "Overwhelmingly Positive"),
            RatingColorBucket(0.80f, Color(0xFF5eb1e2), whiteText, "Very Positive"),
            RatingColorBucket(0.70f, Color(0xFF5eb1e2), whiteText, "Mostly Positive"),
            RatingColorBucket(0.40f, Color(0xFFb39c72), whiteText, "Mixed"),
            RatingColorBucket(0.20f, Color(0xFFc85e2d), whiteText, "Mostly Negative"),
            RatingColorBucket(0.0f, Color(0xFFc85e2d), whiteText, "Very Negative"),
        ),
        nullBucket = nullBucket
    )
    val RC_CSFD = RatingColorBuckets(
        name = "ČSFD",
        buckets = listOf(
            RatingColorBucket(0.70f, Color(0xFFC81613), whiteText, "Nejlepší"),
            RatingColorBucket(0.31f, Color(0xFF658db4), whiteText, "Průměrný"),
            RatingColorBucket(0.00f, Color(0xFF494949), whiteText, "Nejslabší"),
        ),
        nullBucket = nullBucket
    )
}