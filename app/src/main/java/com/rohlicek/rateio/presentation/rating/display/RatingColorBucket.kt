package com.rohlicek.rateio.presentation.rating.display

import androidx.compose.ui.graphics.Color
import com.rohlicek.rateio.utils.lerpPerceptual


data class RatingColorBucket(
    val equalOrGreaterThen: Float? = null,
    val backgroundColor: Color,
    val foregroundColor: Color,
    val label: String? = null,
) /*{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RatingColorBucket) return false

        return this.label == other.label && this.equalOrGreaterThen == other.equalOrGreaterThen
    }

    override fun hashCode(): Int {
        var result = label?.hashCode() ?: 0
        result = 31 * result + (equalOrGreaterThen?.hashCode() ?: 0)
        return result
    }
}*/

data class RatingColorBuckets(
    val name: String,
    val buckets: List<RatingColorBucket>,
    val nullBucket: RatingColorBucket,
    val gradient: Boolean = false,
)

fun getRatingColor(
    rating: Float?,
    buckets: RatingColorBuckets = getCurrentRatingColorBuckets(),
    rtf: RatingTransformation = getCurrentRatingTransformations()
): RatingColorBucket {
    if (rating == null) return buckets.nullBucket
    val transformedRating = getRoundedRating(rating, rtf = rtf)!!

    val lowestBucket = buckets.buckets.last()
    if (transformedRating < lowestBucket.equalOrGreaterThen!!) {
        return lowestBucket
    }

    if (!buckets.gradient) {
        buckets.buckets.forEach { if (it.equalOrGreaterThen != null && transformedRating >= it.equalOrGreaterThen) return it }
        return buckets.nullBucket
    }

    for (i in buckets.buckets.indices) {
        val upperBucket = buckets.buckets[if (i == 0) 0 else i - 1]
        val lowerBucket = buckets.buckets[i]

        val upperThreshold = if (i == 0) 1f else upperBucket.equalOrGreaterThen!!
        val lowerThreshold = lowerBucket.equalOrGreaterThen!!

        if (transformedRating in lowerThreshold..upperThreshold) {
            val range = upperThreshold - lowerThreshold

            val fraction = if (range > 0f) {
                ((transformedRating - lowerThreshold) / range).coerceIn(0f, 1f)
            } else 0f

            return RatingColorBucket(
                equalOrGreaterThen = transformedRating,
                backgroundColor = lerpPerceptual(lowerBucket.backgroundColor, upperBucket.backgroundColor, fraction),
                foregroundColor = if (fraction < 0.5f) lowerBucket.foregroundColor else upperBucket.foregroundColor,
                label = lowerBucket.label
            )
        }
    }
    return buckets.nullBucket
}

fun getBucketDisplayText(bucket: RatingColorBucket?): String {
    return bucket?.equalOrGreaterThen?.let { egt -> "≥${getTransformedRating(egt)}" } ?: "Null"
}

fun getCurrentRatingColorBuckets(): RatingColorBuckets {
    return RatingColorBucketConstants.currentBuckets
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

    val RC_CUSTOM_MOVIE = RatingColorBuckets(
        name = "Custom Movie",
        buckets = listOf(
            RatingColorBucket(0.9f, Color(0xFF1DA1F2), whiteText, "Masterpiece"),
            RatingColorBucket(0.85f, Color(0xFF186A3B), whiteText, "Awesome"),
            RatingColorBucket(0.75f, Color(0xFF28B463), blackText, "Great"),
            RatingColorBucket(0.65f, Color(0xFFF4D03F), blackText, "Good"),
            RatingColorBucket(0.5f, Color(0xFFF39C12), blackText, "Average"),
            RatingColorBucket(0.4f, Color(0xFFE74C3C), whiteText, "Bad"),
            RatingColorBucket(0.0f, Color(0xFF633974), whiteText, "Garbage"),
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

    var currentBuckets = RC_IMDB_EPISODES
}