package com.example.rateio.presentation.rating.display

import java.util.Locale
import kotlin.Int
import kotlin.math.pow
import kotlin.math.round


data class RatingTransformation(
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

fun RatingTransformation.getMinValue(): String {
    val rtf = getCurrentRatingTransformations()

    val transformed = (rtf.offset) / (rtf.divider)
    val decimalPow = 10f.pow((rtf.decimalPlaces).toInt())
    val rounded = round(transformed * decimalPow) / decimalPow

    return rtf.leadingString + ("%.${rtf.decimalPlaces}f").format(rtf.locale, rounded) + rtf.trailingString
}
fun RatingTransformation.getMaxValue(): String {
    val rtf = getCurrentRatingTransformations()

    val transformed = (rtf.stepCount.toInt() + rtf.offset) / (rtf.divider)
    val decimalPow = 10f.pow((rtf.decimalPlaces).toInt())
    val rounded = round(transformed * decimalPow) / decimalPow

    return rtf.leadingString + ("%.${rtf.decimalPlaces}f").format(rtf.locale, rounded) + rtf.trailingString
}

fun getTransformedRating(rating: Float?, decimalOffset: UInt = 0u, rtf: RatingTransformation = getCurrentRatingTransformations()): String {
    if (rating == null) return rtf.nullString
    val decimalOffsetPow = 10f.pow(decimalOffset.toInt())

    val steppedRating = round(rating * rtf.stepCount.toInt() * decimalOffsetPow)
    val transformed = (steppedRating + rtf.offset * decimalOffsetPow) / (rtf.divider * decimalOffsetPow)
    val decimalPow = 10f.pow((rtf.decimalPlaces + decimalOffset).toInt())
    val rounded = round(transformed * decimalPow) / decimalPow

    return rtf.leadingString + ("%.${rtf.decimalPlaces + decimalOffset}f").format(rtf.locale, rounded) + rtf.trailingString
}

fun getRoundedRating(rating: Float?): Float? {
    if (rating == null) return null
    val rtf = getCurrentRatingTransformations()
    return round(rating * rtf.stepCount.toInt()) / rtf.stepCount.toFloat()
}

fun getCurrentRatingTransformations(): RatingTransformation {
    return RatingTransformationsConstants.currentTransformation
}

object RatingTransformationsConstants {
    val TF_IMDB_PRECISE = RatingTransformation(
        stepCount = 1000u,
        divider = 100f,
        decimalPlaces = 2u,
        majorTickFrequency = 10,
    )
    val TF_IMDB = RatingTransformation(
        stepCount = 100u,
        divider = 10f,
        decimalPlaces = 1u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f
    )
    val TF_PERCENTAGE = RatingTransformation(
        stepCount = 100u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val TF_PERCENTAGE_PRECISE = RatingTransformation(
        stepCount = 1000u,
        divider = 10f,
        decimalPlaces = 1u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val TF_TEN_STARS = RatingTransformation(
        stepCount = 20u,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_FIVE_STARS = RatingTransformation(
        stepCount = 8u,
        offset = 2f,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_FIVE_STARS_ZERO = RatingTransformation(
        stepCount = 10u,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_THOUSAND = RatingTransformation(
        stepCount = 1000u,
        majorTickFrequency = 10,
    )
    val TF_ELEVEN = RatingTransformation(
        stepCount = 10u,
        majorTickFrequency = 1,
    )
    val TF_TEN = RatingTransformation(
        stepCount = 9u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    val TF_FIVE = RatingTransformation(
        stepCount = 4u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    val TF_RECOMMEND = RatingTransformation(
        stepCount = 1u,
        majorTickFrequency = 1,
    )
    val TF_FLOAT = RatingTransformation(
        stepCount = 100u,
        divider = 100f,
        decimalPlaces = 2u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f,
        trailingString = "",
    )
    var currentTransformation = TF_IMDB
}