package com.example.rateio.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "rate_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RateItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("categoryId"), Index("parentId"), Index("externalId")],
)
data class RateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val parentId: Long?,
    val title: String,
    val subtitle: String?,
    val coverImageUrl: String?,
    val coverImageLowUrl: String?,
    val externalId: String?,
    val externalSource: String?,
    val rating: Float?,
    val ratingWeight: Float,
    val status: String,
    val metadataJSON: String?,
    val createdAt: Long,
    val updatedAt: Long,
)