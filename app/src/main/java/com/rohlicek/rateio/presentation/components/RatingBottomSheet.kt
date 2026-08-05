package com.rohlicek.rateio.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.RatingTransformation
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingTransformations


@Composable
fun RatingBottomSheet(
    rating: Float? = null,
    onDismiss: () -> Unit,
    onValueChange: (Float?) -> Unit,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    transformationOverride: RatingTransformation = getCurrentRatingTransformations(),
) {
    val haptic = LocalHapticFeedback.current
    var rackPosition by remember(rating) { mutableStateOf(rating) }

    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isPressed by buttonInteractionSource.collectIsPressedAsState()
    val animatedWidth by animateDpAsState(
        targetValue = if (isPressed) 40.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rate_button_width_anim"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RateBox(
                rating = rackPosition,
                size = RateBoxSizeDefaults.DISPLAY,
                decimalOffset = 1u,
                colorBucketsOverride = colorBucketsOverride,
                transformationOverride = transformationOverride,
            )

            RackRatingSlider(
                rating = rackPosition ?: 0.7f,
                colorBucketsOverride = colorBucketsOverride,
                onValueChange = {
                    rackPosition = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                indicatorWidth = 4.5.dp,
                majorTickWidth = 4.dp,
                minorTickWidth = 3.dp,
                minorTickHeightFraction = 0.5f,
                majorTickHeightFraction = 0.65f,
                tickSpacing = 11.dp,
                stepCount = transformationOverride.stepCount.toInt(),
                majorTickFrequency = transformationOverride.majorTickFrequency,
                hardPart = transformationOverride.legendaryPart,
            )

            RackRatingSlider(
                rating = rackPosition ?: 0.7f,
                colorBucketsOverride = colorBucketsOverride,
                onValueChange = {
                    rackPosition = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                indicatorWidth = 4.5.dp,
                majorTickWidth = 3.5.dp,
                minorTickWidth = 2.5.dp,
                minorTickHeightFraction = 0.4f,
                majorTickHeightFraction = 0.55f,
                tickSpacing = 8.dp,
                stepCount = transformationOverride.stepCount.toInt() * 10,
                majorTickFrequency = transformationOverride.majorTickFrequency * 5,
                hardPart = transformationOverride.legendaryPart,
            )

            Spacer(modifier = Modifier.height(32.dp))


            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    rackPosition = null
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("Set to Unrated", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onValueChange(rackPosition)
                    onDismiss()
                },
                interactionSource = buttonInteractionSource,
                modifier = Modifier.width(225.dp + animatedWidth),
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("RATE", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
            }
        }
    }
}