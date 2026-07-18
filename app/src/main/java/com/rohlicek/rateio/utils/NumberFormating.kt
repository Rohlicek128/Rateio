package com.rohlicek.rateio.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs


fun formatCompact(number: Long, decimalsKilo: Int = 0, decimalsMillion: Int = 1, decimalsBillion: Int = 1): String {
    return when {
        number >= 1_000_000_000 -> "%.${decimalsBillion}fB".format(Locale.US, number / 1_000_000_000.0).trimEnd('0').trimEnd('.')
        number >= 1_000_000 -> "%.${decimalsMillion}fM".format(Locale.US, number / 1_000_000.0).trimEnd('0').trimEnd('.')
        number >= 1_000 -> "%.${decimalsKilo}fK".format(Locale.US, number / 1_000.0).trimEnd('0').trimEnd('.')
        else -> number.toString()
    }
}

fun formatGrouped(number: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

fun formatOrderNumber(number: Int): String {
    val lastTwoDigits = abs(number) % 100
    val lastDigit = if (lastTwoDigits != 11 && lastTwoDigits != 12 && lastTwoDigits != 13)
        lastTwoDigits % 10
    else 0
    return number.toString() + when (lastDigit) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}