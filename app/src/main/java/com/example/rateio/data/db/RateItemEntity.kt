package com.example.rateio.data.db

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey


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
    indices = [
        Index("categoryId"),
        Index("parentId"),
    ]
)
data class RateItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val parentId: Long?,
    val title: String,
    val subtitle: String?,
    val coverImageUrl: String?,
    val externalId: String?,
    val rating: Float?,
    val metadata: String?,
    val createdAt: Long,
    val updatedAt: Long
)