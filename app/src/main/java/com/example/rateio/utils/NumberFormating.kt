package com.example.rateio.utils

import java.text.NumberFormat
import java.util.Locale


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