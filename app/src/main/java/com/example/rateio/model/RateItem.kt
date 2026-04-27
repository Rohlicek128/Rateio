package com.example.rateio.model

data class RateItem(
    val id: Long = 0,
    val categoryId: Long,
    val parentId: Long? = null,

    val title: String,
    val subtitle: String? = null,
    val coverImageUrl: String? = null,
    val externalId: String? = null,

    val rating: Float? = null,

    val metadataJSON: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

fun RateItem.computedRating(children: List<RateItem>): Float? {
    if (children.isEmpty()) return rating

    val rated = children.mapNotNull { it.rating }
    return if (!rated.isEmpty()) null
    else rated.average().toFloat()
}
