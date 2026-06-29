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

fun RatingTransformations.getMinValue(): String {
    val rtf = getCurrentRatingTransformations()

    val transformed = (rtf.offset) / (rtf.divider)
    val decimalPow = 10f.pow((rtf.decimalPlaces).toInt())
    val rounded = round(transformed * decimalPow) / decimalPow

    return rtf.leadingString + ("%.${rtf.decimalPlaces}f").format(rtf.locale, rounded) + rtf.trailingString
}
fun RatingTransformations.getMaxValue(): String {
    val rtf = getCurrentRatingTransformations()

    val transformed = (rtf.stepCount.toInt() + rtf.offset) / (rtf.divider)
    val decimalPow = 10f.pow((rtf.decimalPlaces).toInt())
    val rounded = round(transformed * decimalPow) / decimalPow

    return rtf.leadingString + ("%.${rtf.decimalPlaces}f").format(rtf.locale, rounded) + rtf.trailingString
}

fun getTransformedRating(rating: Float?, decimalOffset: UInt = 0u): String {
    val rtf = getCurrentRatingTransformations()
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

fun getCurrentRatingTransformations(): RatingTransformations {
    return RatingTransformationsConstants.currentTransformation
}

object RatingTransformationsConstants {
    val TF_IMDB_PRECISE = RatingTransformations(
        stepCount = 1000u,
        divider = 100f,
        decimalPlaces = 2u,
        majorTickFrequency = 10,
    )
    val TF_IMDB = RatingTransformations(
        stepCount = 100u,
        divider = 10f,
        decimalPlaces = 1u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f
    )
    val TF_PERCENTAGE = RatingTransformations(
        stepCount = 100u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val TF_PERCENTAGE_PRECISE = RatingTransformations(
        stepCount = 1000u,
        divider = 10f,
        decimalPlaces = 1u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val TF_TEN_STARS = RatingTransformations(
        stepCount = 20u,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_FIVE_STARS = RatingTransformations(
        stepCount = 8u,
        offset = 2f,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_THOUSAND = RatingTransformations(
        stepCount = 1000u,
        majorTickFrequency = 10,
    )
    val TF_ELEVEN = RatingTransformations(
        stepCount = 10u,
        majorTickFrequency = 1,
    )
    val TF_TEN = RatingTransformations(
        stepCount = 9u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    val TF_FIVE = RatingTransformations(
        stepCount = 4u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    val TF_RECOMMEND = RatingTransformations(
        stepCount = 1u,
        majorTickFrequency = 1,
    )
    val TF_FLOAT = RatingTransformations(
        stepCount = 100u,
        divider = 100f,
        decimalPlaces = 2u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f,
        trailingString = "",
    )
    var currentTransformation = TF_IMDB
}