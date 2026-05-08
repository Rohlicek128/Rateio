package com.example.rateio.data.db

import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.model.ItemStatus
import com.example.rateio.model.RateItem


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
    coverImageUrl = coverImageUrl,
    coverImageLowUrl = coverImageLowUrl,
    externalId = externalId,
    externalSource = externalSource?.let { CategoryType.valueOf(it) },
    rating = rating,
    ratingWeight = ratingWeight,
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
    coverImageUrl = coverImageUrl,
    coverImageLowUrl = coverImageLowUrl,
    externalId = externalId,
    externalSource = externalSource?.name,
    rating = rating,
    ratingWeight = ratingWeight,
    status = status.name,
    metadataJSON = metadataJSON,
    createdAt = createdAt,
    updatedAt = updatedAt,
)