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

    val externalId: String? = null,
    val externalSource: CategoryType? = null,

    val rating: Float? = null,
    val ratingWeight: Float = 1.0f,
    val review: String? = null,

    val status: ItemStatus = ItemStatus.WATCHLIST,

    val metadataJSON: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class ItemStatus(override val displayName: String): HasDisplayName {
    NONE("None"),
    WATCHLIST("Watchlist"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    DROPPED("Dropped"),
    ON_HOLD("On Hold"),
}


fun RateItem.computedRating(children: List<RateItem>): Float? {
    if (children.isEmpty()) return rating

    val rated = children.mapNotNull { it.rating }
    return if (!rated.isEmpty()) null
    else rated.average().toFloat()
}

fun computeAggregateRatingWeighted(ratings: List<Float>): Float? {
    val ratingWeight = 0.9f
    val lengthWeight = 1f - ratingWeight
    val maxLength = 150

    val ratingWeighted = if (ratings.isNotEmpty())
        ratings.average().toFloat() * ratingWeight
    else return null

    val lengthWeighted = (log10(ratings.size.toFloat()) / log10(maxLength.toFloat())) * lengthWeight

    return ratingWeighted + lengthWeighted
}

fun computeAggregateRatingAverage(children: List<RateItem>): Float? {
    val flatRatings = children.mapNotNull { it.rating }
    return if (flatRatings.isNotEmpty()) flatRatings.average().toFloat() else null
}