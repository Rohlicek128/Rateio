package com.example.rateio.presentation.rating.display

import java.util.Locale
import kotlin.Int
import kotlin.math.pow
import kotlin.math.round


data class RatingTransformation(
    val name: String,

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

fun RatingTransformation.getMaxCharWidth(rating: Float? = 1f): Int {
    return getTransformedRating(rating, rtf = this)
        .replace(".", "")
        .replace(",", "")
        .length
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

fun getRoundedRating(rating: Float?, rtf: RatingTransformation = getCurrentRatingTransformations()): Float? {
    if (rating == null) return null
    return round(rating * rtf.stepCount.toInt()) / rtf.stepCount.toFloat()
}

fun getCurrentRatingTransformations(): RatingTransformation {
    return RatingTransformationsConstants.currentTransformation
}

object RatingTransformationsConstants {
    val TF_IMDB_PRECISE = RatingTransformation(
        name = "IMDb Precise",
        stepCount = 1000u,
        divider = 100f,
        decimalPlaces = 2u,
        majorTickFrequency = 10,
    )
    val TF_IMDB = RatingTransformation(
        name = "IMDb",
        stepCount = 100u,
        divider = 10f,
        decimalPlaces = 1u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f
    )
    val TF_PERCENTAGE = RatingTransformation(
        name = "Percentage",
        stepCount = 100u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val TF_PERCENTAGE_PRECISE = RatingTransformation(
        name = "Percentage Precise",
        stepCount = 1000u,
        divider = 10f,
        decimalPlaces = 1u,
        trailingString = "%",
        majorTickFrequency = 10,
    )
    val TF_TEN_STARS = RatingTransformation(
        name = "Ten Stars",
        stepCount = 20u,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_FIVE_STARS = RatingTransformation(
        name = "Five Stars",
        stepCount = 8u,
        offset = 2f,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_FIVE_STARS_ZERO = RatingTransformation(
        name = "Five Stars (From Zero)",
        stepCount = 10u,
        divider = 2f,
        decimalPlaces = 1u,
        majorTickFrequency = 2,
    )
    val TF_THOUSAND = RatingTransformation(
        name = "Thousand",
        stepCount = 1000u,
        majorTickFrequency = 10,
    )
    val TF_TEN_ZERO = RatingTransformation(
        name = "Ten (From Zero)",
        stepCount = 10u,
        majorTickFrequency = 1,
    )
    val TF_TEN = RatingTransformation(
        name = "Ten",
        stepCount = 9u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    val TF_FIVE = RatingTransformation(
        name = "Five",
        stepCount = 4u,
        offset = 1f,
        majorTickFrequency = 1,
    )
    val TF_RECOMMEND = RatingTransformation(
        name = "Recommend",
        stepCount = 1u,
        majorTickFrequency = 1,
    )
    val TF_FLOAT = RatingTransformation(
        name = "Float",
        stepCount = 100u,
        divider = 100f,
        decimalPlaces = 2u,
        majorTickFrequency = 5,
        legendaryPart = 0.96f,
        trailingString = "",
    )
    var currentTransformation = TF_IMDB
}