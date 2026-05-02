package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rateio.ui.theme.GoogleSans
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun RateBox(
    rating: Float?,
    modifier: Modifier = Modifier,
    roundedCorners: Dp = 8.dp,
    width: Dp = 10.dp,
    minWidth: Dp = Dp.Unspecified,
    maxWidth: Dp = Dp.Unspecified,
    height: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    onClick: (() -> Unit)? = null
) {
    val (backgroundColor, contentColor, label) = getColorSchemeImdbEpisodes(rating)

    val displayText = if (rating != null) "%.1f".format(Locale.US, (rating * 100f).roundToInt() / 10f) else "?"

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(roundedCorners),
        border = null,
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = width, vertical = height)
                .widthIn(min = minWidth, max = maxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = displayText,
                style = textStyle,
                fontWeight = fontWeight,
                maxLines = 1,
            )
        }
    }
}


@Composable
private fun getColorSchemeImdbEpisodes(rating: Float?): Triple<Color, Color, String> {
    val whiteText = Color(0xFFFFFFFF)
    val blackText = Color(0xFF181818)
    return when {
        rating == null  -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "N/A")
        rating >= 0.96f -> Triple(Color(0xFF1DA1F2), whiteText, "Masterpiece")
        rating >= 0.90f -> Triple(Color(0xFF186A3B), whiteText, "Awesome")
        rating >= 0.80f -> Triple(Color(0xFF28B463), blackText, "Great")
        rating >= 0.70f -> Triple(Color(0xFFF4D03F), blackText, "Good")
        rating >= 0.60f -> Triple(Color(0xFFF39C12), blackText, "Average")
        rating >= 0.41f -> Triple(Color(0xFFE74C3C), whiteText, "Bad")
        else -> Triple(Color(0xFF633974), whiteText, "Garbage")
    }
}

@Composable
private fun getColorSchemeImdbMovies(rating: Float?): Triple<Color, Color, String> {
    val whiteText = Color(0xFFFFFFFF)
    val blackText = Color(0xFF181818)
    return when {
        rating == null  -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "N/A")
        rating >= 0.80f -> Triple(Color(0xFF1DA1F2), whiteText, "Masterpiece")
        rating >= 0.75f -> Triple(Color(0xFF186A3B), whiteText, "Awesome")
        rating >= 0.70f -> Triple(Color(0xFF28B463), blackText, "Great")
        rating >= 0.65f -> Triple(Color(0xFFF4D03F), blackText, "Good")
        rating >= 0.55f -> Triple(Color(0xFFF39C12), blackText, "Average")
        rating >= 0.40f -> Triple(Color(0xFFE74C3C), whiteText, "Bad")
        else -> Triple(Color(0xFF633974), whiteText, "Garbage")
    }
}