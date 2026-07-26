package com.rohlicek.rateio.data.db

import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem


fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    type = CategoryType.valueOf(type),
    name = name,
    sortOrder = sortOrder,
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    type = type.name,
    name = name,
    sortOrder = sortOrder,
)

fun RateItemEntity.toDomain(): RateItem = RateItem(
    id = id,
    categoryId = categoryId,
    parentId = parentId,
    title = title,
    subtitle = subtitle,
    length = length,
    coverImageUrl = coverImageUrl,
    coverImageLowUrl = coverImageLowUrl,
    coverImageOverride = coverImageOverride,
    backdropImageUrl = backdropImageUrl,
    backdropImageLowUrl = backdropImageLowUrl,
    externalId = externalId,
    externalSource = externalSource?.let { CategoryType.valueOf(it) },
    rating = rating,
    ratingOverride = ratingOverride,
    ratingWeight = ratingWeight,
    review = review,
    status = ItemStatus.valueOf(status),
    metadataJSON = metadataJSON,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun RateItem.toEntity(): RateItemEntity = RateItemEntity(
    id = id,
    categoryId = categoryId,
    parentId = parentId,
    title = title,
    subtitle = subtitle,
    length = length,
    coverImageUrl = coverImageUrl,
    coverImageLowUrl = coverImageLowUrl,
    coverImageOverride = coverImageOverride,
    backdropImageUrl = backdropImageUrl,
    backdropImageLowUrl = backdropImageLowUrl,
    externalId = externalId,
    externalSource = externalSource?.name,
    rating = rating,
    ratingOverride = ratingOverride,
    ratingWeight = ratingWeight,
    review = review,
    status = status.name,
    metadataJSON = metadataJSON,
    createdAt = createdAt,
    updatedAt = updatedAt,
)