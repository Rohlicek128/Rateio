package com.example.rateio.data.db

import androidx.room3.Entity
import androidx.room3.PrimaryKey


@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconEmoji: String?,
    val apiSource: String,
    val isCustom: Boolean
)