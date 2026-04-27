package com.example.rateio.model

data class Category(
    val id: Long = 0,
    val name: String,
    val iconEmoji: String? = null,
    val isCustom: Boolean = true
)