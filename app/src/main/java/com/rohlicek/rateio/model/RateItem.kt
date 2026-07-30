package com.rohlicek.rateio.model

import kotlin.math.log10

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


fun computeWeightedRating(aggregateRating: Float?, length: Int?): Float? {
    if (aggregateRating == null || length == null || length <= 0) return null

    val ratingWeight = 0.9f
    val lengthWeight = 1f - ratingWeight
    val maxLength = 150

    val ratingWeighted = aggregateRating * ratingWeight
    val lengthWeighted = (log10(length.toFloat()) / log10(maxLength.toFloat()))
        .coerceAtMost(1f) * lengthWeight

    return ratingWeighted + lengthWeighted
}

fun computeAggregateRatingWeighted(ratings: List<Float>): Float? {
    return computeWeightedRating(computeAggregateRating(ratings), ratings.size)
}


fun computeAggregateRating(ratings: List<Float>): Float? {
    return if (ratings.isNotEmpty())
        ratings.average().toFloat()
    else null
}

fun computeAggregateChildrenRating(children: List<RateItem>): Float? {
    val flatRatings = children.mapNotNull { it.rating }
    return computeAggregateRating(flatRatings)
}