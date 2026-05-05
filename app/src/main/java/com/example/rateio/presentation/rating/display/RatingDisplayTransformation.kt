package com.example.rateio.presentation.rating.display

import java.util.Locale
import kotlin.Int
import kotlin.math.pow
import kotlin.math.round


data class RatingTransformations(
    val stepCount: UInt,
    val offset: Float = 0f,
    val divider: Float = 1f,
    val decimalPlaces: UInt = 0u,
    val locale: Locale = Locale.US,
    val leadingString: String = "",
    val trailingString: String = "",
    val nullString: String = "?",

    val majorTickFrequency: Int = 5,
    val legendaryPart: Float = 0.96f
)

fun getTransformedRating(rating: Float?): String {
    val rtf = getCurrentRatingTransformations()
    if (rating == null) return rtf.nullString


    val steppedRating = round(rating * rtf.stepCount.toInt())
    val transformed = (steppedRating + rtf.offset) / rtf.divider
    val decimalPow = 10f.pow(rtf.decimalPlaces.toInt())
    val rounded = round(transformed * decimalPow) / decimalPow

    return rtf.leadingString + ("%.${rtf.decimalPlaces}f").format(rtf.locale, rounded) + rtf.trailingString
}

fun getRoundedRating(rating: Float?): Float? {
    if (rating == null) return null
    val rtf = getCurrentRatingTransformations()
    return round(rating * rtf.stepCount.toInt()) / rtf.stepCount.toFloat()
}

fun getCurrentRatingTransformations(): RatingTransformations {
    val tfThousand = RatingTransformations(
        stepCount = 1000u,
        divider = 10f,
        decimalPlaces = 1u,
        majorTickFrequency = 100,
    )
    val tfHunTen = RatingTransformations(
        stepCount = 100u,
        divider = 10f,
        decimalPlaces = 1u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f
    )
    val tfPercentage = RatingTransformations(
        stepCount = 100u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val tfStars = RatingTransformations(
        stepCount = 8u,
        offset = 2f,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val tfTen = RatingTransformations(
        stepCount = 9u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    return tfHunTen
}