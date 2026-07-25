package com.rohlicek.rateio.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.core.net.toUri
import kotlin.math.cbrt
import kotlin.math.max
import kotlin.math.pow

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

@Composable
fun Modifier.shimmerLoading(
    baseColor: Color = Color.Transparent,
    highlightColor: Color = Color.White.copy(alpha = 0.5f)
): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    return this.drawWithCache {
        val brush = Brush.linearGradient(
            colors = listOf(baseColor, highlightColor, baseColor),
            start = Offset(translateAnim - 500f, translateAnim - 500f),
            end = Offset(translateAnim, translateAnim)
        )
        onDrawWithContent {
            drawContent()
            drawRect(brush = brush)
        }
    }
}


fun lerpPerceptual(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)

    // Convert start from sRGB to Linear RGB
    val r1 = sRgbToLinear(start.red)
    val g1 = sRgbToLinear(start.green)
    val b1 = sRgbToLinear(start.blue)

    // Convert end from sRGB to Linear RGB
    val r2 = sRgbToLinear(end.red)
    val g2 = sRgbToLinear(end.green)
    val b2 = sRgbToLinear(end.blue)

    // Linear RGB -> Oklab (Start)
    val l1 = cbrt(0.4122214708f * r1 + 0.5363325363f * g1 + 0.0514459929f * b1)
    val m1 = cbrt(0.2119034982f * r1 + 0.6806995451f * g1 + 0.1073969566f * b1)
    val s1 = cbrt(0.0883024619f * r1 + 0.2817188376f * g1 + 0.6299787005f * b1)
    val okL1 = 0.2104542553f * l1 + 0.7936177850f * m1 - 0.0040720468f * s1
    val okA1 = 1.9779984951f * l1 - 2.4285922050f * m1 + 0.4505937099f * s1
    val okB1 = 0.0259040371f * l1 + 0.7827717662f * m1 - 0.8086757660f * s1

    // Linear RGB -> Oklab (End)
    val l2 = cbrt(0.4122214708f * r2 + 0.5363325363f * g2 + 0.0514459929f * b2)
    val m2 = cbrt(0.2119034982f * r2 + 0.6806995451f * g2 + 0.1073969566f * b2)
    val s2 = cbrt(0.0883024619f * r2 + 0.2817188376f * g2 + 0.6299787005f * b2)
    val okL2 = 0.2104542553f * l2 + 0.7936177850f * m2 - 0.0040720468f * s1
    val okA2 = 1.9779984951f * l2 - 2.4285922050f * m2 + 0.4505937099f * s2
    val okB2 = 0.0259040371f * l2 + 0.7827717662f * m2 - 0.8086757660f * s2

    // Interpolate in Oklab space
    val L = okL1 + f * (okL2 - okL1)
    val a = okA1 + f * (okA2 - okA1)
    val b = okB1 + f * (okB2 - okB1)

    // Oklab -> Linear RGB
    val l_ = L + 0.3963377774f * a + 0.2158037573f * b
    val m_ = L - 0.1055613458f * a - 0.0638541728f * b
    val s_ = L - 0.0894841775f * a - 1.2914855480f * b

    val l3 = l_ * l_ * l_
    val m3 = m_ * m_ * m_
    val s3 = s_ * s_ * s_

    val r3 = +4.0767416621f * l3 - 3.3077115913f * m3 + 0.2309699292f * s3
    val g3 = -1.2684380046f * l3 + 2.6097574011f * m3 - 0.3413193965f * s3
    val b3 = -0.0041960863f * l3 - 0.7034186147f * m3 + 1.7076147010f * s3

    // Convert back to sRGB
    val red = linearToSRgb(r3)
    val green = linearToSRgb(g3)
    val blue = linearToSRgb(b3)
    val alpha = start.alpha + f * (end.alpha - start.alpha)

    return Color(red, green, blue, alpha)
}

private fun sRgbToLinear(c: Float): Float {
    return if (c <= 0.04045f) c / 12.92f else ((c + 0.055) / 1.055).pow(2.4).toFloat()
}

private fun linearToSRgb(c: Float): Float {
    val clamped = max(0f, c)
    val srgb = if (clamped <= 0.0031308f) clamped * 12.92f else 1.055f * clamped.toDouble()
        .pow(1 / 2.4).toFloat() - 0.055f
    return srgb.coerceIn(0f, 1f)
}