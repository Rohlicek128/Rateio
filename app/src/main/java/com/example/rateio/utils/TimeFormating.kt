package com.example.rateio.utils

import kotlin.math.floor
import kotlin.math.round


fun formatTime(totalMinutes: Int): String {
    val hours = floor(totalMinutes / 60f).toInt()
    val minutes = round(totalMinutes % 60f).toInt()
    return "${if (hours > 0) hours.toString() + "h " else ""}${
        when {
            minutes > 0 -> minutes.toString() + "m"
            hours <= 0 -> "?m"
            else -> ""
        }
    }"
}