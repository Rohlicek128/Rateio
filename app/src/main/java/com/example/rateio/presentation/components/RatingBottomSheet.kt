package com.example.rateio.presentation.components

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.rating.display.RatingColorBuckets
import com.example.rateio.presentation.rating.display.RatingTransformation
import com.example.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.example.rateio.presentation.rating.display.getCurrentRatingTransformations


@Composable
fun RatingBottomSheet(
    rating: Float? = 0f,
    onDismiss: () -> Unit,
    onValueChange: (Float?) -> Unit,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    transformationOverride: RatingTransformation = getCurrentRatingTransformations(),
) {
    val haptic = LocalHapticFeedback.current
    var rackPosition by rememberSaveable { mutableStateOf(rating) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                roundedCorners = 18.dp,
                width = 24.dp,
                minWidth = 42.dp,
                height = 6.dp,
                textStyle = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                loadingSize = 38.dp,
                decimalOffset = 1u,
                colorBucketsOverride = colorBucketsOverride,
                transformationOverride = transformationOverride,
            )

            RackRatingSlider(
                rating = rackPosition ?: 0.7f,
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
                modifier = Modifier.width(250.dp),
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("RATE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}