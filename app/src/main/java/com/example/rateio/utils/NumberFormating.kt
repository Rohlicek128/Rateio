package com.example.rateio.utils

import java.text.NumberFormat
import java.util.Locale


fun formatCompact(number: Int): String {
    return when {
        number >= 1_000_000 -> "%.1fM".format(Locale.US, number / 1_000_000.0).trimEnd('0').trimEnd('.')
        number >= 1_000 -> "%.0fK".format(Locale.US, number / 1_000.0).trimEnd('0').trimEnd('.')
        else -> number.toString()
    }
}

fun formatGrouped(number: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}