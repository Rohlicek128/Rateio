package com.rohlicek.rateio.model

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

data class RateItem(
    val id: Long = 0,
    val categoryId: Long,
    val parentId: Long? = null,

    val title: String,
    val subtitle: String? = null,
    val length: Float? = null,
    val coverImageUrl: String? = null,
    val coverImageLowUrl: String? = null,
    val coverImageOverride: String? = null,
    val backdropImageUrl: String? = null,
    val backdropImageLowUrl: String? = null,

    val externalId: String? = null,
    val externalSource: CategoryType? = null,

    val rating: Float? = null,
    val ratingOverride: Float? = null,
    val ratingWeight: Float = 1.0f,
    val review: String? = null,

    val status: ItemStatus = ItemStatus.WATCHLIST,

    val metadataJSON: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class ItemStatus(override val displayName: String): HasDisplayName {
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    WATCHLIST("Watchlist"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped"),
    NONE("None"),
}


fun computeWeightedRating(aggregateRating: Float?, length: Int?, maxLengthOverride: Int? = null): Float? {
    if (aggregateRating == null || length == null || length <= 0) return null

    val ratingWeight = 0.9f
    val lengthWeight = 1f - ratingWeight
    val maxLength = maxLengthOverride ?: 155

    val ratingWeighted = aggregateRating * ratingWeight
    val lengthWeighted = (log10(length.toFloat()) / log10(maxLength.toFloat()))
        .coerceAtMost(1f) * lengthWeight

    return ratingWeighted + lengthWeighted
}

fun computeAggregateRatingWeighted(ratings: List<Float>): Float? {
    return computeWeightedRating(computeAggregateRating(ratings), ratings.size)
}


fun computeAggregateRating(ratings: List<Float>): Float? {
    return computeAverageRating(ratings)
    //return computeBlendedRating(ratings)
}
private fun computeAverageRating(ratings: List<Float>): Float? {
    return if (ratings.isNotEmpty())
        ratings.average().toFloat()
    else null
}
private fun computeBlendedRating(ratings: List<Float>): Float? {
    if (ratings.isEmpty()) return null

    val sorted = ratings.sorted()
    val average = sorted.average().toFloat()

    // 1. Calculate the 70th percentile rank
    val p70Index = ((sorted.size - 1) * 0.70f).toInt()
    val p70 = sorted[p70Index]

    // 2. Determine interpolation factor (alpha) based on episode count
    // Short shows (<=5 ep) use alpha ~0.1; Long shows (>=50 ep) reach alpha ~0.5
    val alpha = (ratings.size.toFloat() / 50f).coerceIn(0.1f, 0.5f)
    return ((1f - alpha) * average) + (alpha * p70)
}

fun computeAggregateChildrenRating(children: List<RateItem>): Float? {
    val flatRatings = children.mapNotNull { it.rating }
    return computeAggregateRating(flatRatings)
}


fun calculateStandardDeviation(ratings: List<Float>, average: Float? = null): Float? {
    if (ratings.isEmpty()) return null
    val avg = average ?: ratings.average().toFloat()

    val top = ratings.sumOf { (it - avg).pow(2).toDouble() }

    return sqrt(top / (ratings.size - 1)).toFloat()
}