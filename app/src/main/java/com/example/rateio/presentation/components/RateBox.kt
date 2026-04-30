package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale


@Composable
fun RateBox(rating: Float?, modifier: Modifier = Modifier, scale: Dp = 1.dp) {
    var ratingTest: Float? = null
    if (rating != null) {
        if (rating > 0f) ratingTest = rating.plus(0.1f)
    }

    val (backgroundColor, contentColor, label) = when {
        ratingTest == null  -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, null)
        ratingTest >= 0.96f -> Triple(Color(0xFF1DA1F2), Color(0xFFFFFFFF), "Masterpiece")
        ratingTest >= 0.90f -> Triple(Color(0xFF186A3B), Color(0xFFFFFFFF), "Awesome")
        ratingTest >= 0.80f -> Triple(Color(0xFF28B463), Color(0xFF161616), "Great")
        ratingTest >= 0.70f -> Triple(Color(0xFFF4D03F), Color(0xFF161616), "Good")
        ratingTest >= 0.60f -> Triple(Color(0xFFF39C12), Color(0xFF161616), "Average")
        ratingTest >= 0.41f -> Triple(Color(0xFFF39C12), Color(0xFF161616), "Average")
        ratingTest >= 0.40f -> Triple(Color(0xFFE74C3C), Color(0xFFFFFFFF), "Bad")
        else            -> Triple(Color(0xFF633974), Color(0xFFFFFFFF), "Garbage")
    }

    val displayText = if (ratingTest != null) "%.1f".format(Locale.US, ratingTest * 10) else "?"

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp + scale),
        border = null,
        modifier = modifier.clickable(onClick = {}),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp + scale, vertical = 4.dp + (scale / 2)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}