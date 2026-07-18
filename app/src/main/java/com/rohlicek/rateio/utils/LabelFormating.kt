package com.rohlicek.rateio.utils

import com.rohlicek.rateio.model.CategoryType
import java.util.Locale


fun formatItemRankLabel(rank: Int, category: CategoryType): String {
    return "${formatOrderNumber(rank)} in ${category.displayName}"
}

fun formatIMDbRatingLabel(rating: Float?): String? {
    return rating?.let { "%.1f on IMDb".format(Locale.US, it * 10f) }
}