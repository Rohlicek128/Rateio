package com.example.rateio.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.max

fun <T> List<T>.getWrapped(index: Int): T {
    if (isEmpty()) throw NoSuchElementException("List is empty.")

    val safeIndex = Math.floorMod(index, size)
    return this[safeIndex]
}

fun Color.dim(dimAmount: Float = 0.1f, alpha: Float = 1f): Color {
    val amount = dimAmount.coerceIn(0f, 1f)
    return this.copy(
        red = max(0f, this.red - amount),
        green = max(0f, this.green - amount),
        blue = max(0f, this.blue - amount),
        alpha = alpha
    )
}