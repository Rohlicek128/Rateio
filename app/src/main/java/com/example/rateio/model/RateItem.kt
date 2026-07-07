package com.example.rateio.model

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

fun computeAggregateRating(children: List<RateItem>): Float? {
    val flatRatings = children.mapNotNull { it.rating }
    return if (flatRatings.isNotEmpty()) flatRatings.average().toFloat() else null
}