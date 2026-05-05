package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.presentation.rating.display.getRoundedRating
import com.example.rateio.presentation.rating.display.getTransformedRating
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
    loadingSize: Dp = 28.dp,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = getRatingColor(getRoundedRating(rating))
    val display = getTransformedRating(rating)

    Surface(
        color = colors.backgroundColor,
        contentColor = colors.foregroundColor,
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
            if (isLoading) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(loadingSize),
                    color = MaterialTheme.colorScheme.secondaryFixedDim,
                    trackColor = MaterialTheme.colorScheme.surfaceBright,
                    wavelength = 12.dp,
                )
            }
            else {
                Text(
                    text = display,
                    style = textStyle,
                    fontWeight = fontWeight,
                    maxLines = 1,
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        }
    }
}