package com.example.rateio.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

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


fun openExternalLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No application found to open this link.", Toast.LENGTH_SHORT).show()
    }
}