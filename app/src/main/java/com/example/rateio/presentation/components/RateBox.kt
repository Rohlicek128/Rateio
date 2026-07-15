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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rateio.presentation.rating.display.RatingColorBuckets
import com.example.rateio.presentation.rating.display.RatingTransformation
import com.example.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.example.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.example.rateio.presentation.rating.display.getMaxCharWidth
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.presentation.rating.display.getRoundedRating
import com.example.rateio.presentation.rating.display.getTransformedRating
import com.example.rateio.ui.theme.GoogleSansRounded


@Composable
fun RateBox(
    rating: Float?,
    modifier: Modifier = Modifier,
    size: RateBoxSize = RateBoxSizeDefaults.REGULAR,
    widthConstrained: Boolean = false,
    decimalOffset: UInt = 0u,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    transformationOverride: RatingTransformation = getCurrentRatingTransformations(),
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    val colors = getRatingColor(getRoundedRating(rating), colorBucketsOverride)
    val display = getTransformedRating(rating, decimalOffset, transformationOverride)

    val maxLength = transformationOverride.getMaxCharWidth()
    val sizeAddition = ((maxLength - 3) * 8).coerceAtLeast(-7)
    val modifiedWidth = size.width + sizeAddition.dp

    Surface(
        color = colors.backgroundColor,
        contentColor = colors.foregroundColor,
        shape = RoundedCornerShape(size.rounding),
        border = null,
        modifier = if (onClick != null) modifier.clickable(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        }) else modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = size.paddingWidth, vertical = size.height)
                .widthIn(
                    min = if (widthConstrained) modifiedWidth else size.width,
                    max = if (widthConstrained) modifiedWidth else Dp.Unspecified
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.secondaryFixedDim,
                    trackColor = MaterialTheme.colorScheme.surfaceBright,
                    wavelength = 12.dp,
                )
            }
            else {
                Text(
                    text = display,
                    fontSize = size.textSize,
                    fontWeight = FontWeight.Black,
                    fontFamily = GoogleSansRounded,
                    letterSpacing = 0.sp,
                    maxLines = 1,
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        }
    }
}


data class RateBoxSize(
    val textSize: TextUnit,
    val paddingWidth: Dp,
    val width: Dp,
    val height: Dp,
    val rounding: Dp,
)

object RateBoxSizeDefaults {
    val smallRoundingSize: Dp = 7.dp
    val regularRoundingSize: Dp = 12.dp
    val largeRoundingSize: Dp = 18.dp

    val REGULAR = RateBoxSize(
        textSize = 23.sp,
        paddingWidth = 12.dp,
        width = 36.dp,
        height = 5.dp,
        rounding = regularRoundingSize,
    )

    val REGULAR_GRID = REGULAR.copy(
        height = 4.dp,
        rounding = smallRoundingSize,
    )

    val DISPLAY = RateBoxSize(
        textSize = 44.sp,
        paddingWidth = 24.dp,
        width = 42.dp,
        height = 5.dp,
        rounding = largeRoundingSize,
    )
}