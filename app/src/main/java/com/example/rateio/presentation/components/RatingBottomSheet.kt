package com.example.rateio.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun RatingBottomSheet(
    rating: Float = 0f,
    onDismiss: () -> Unit,
    onValueChange: (Float) -> Unit,
) {
    var sliderPosition by rememberSaveable { mutableFloatStateOf(rating) }

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
                rating = sliderPosition,
                roundedCorners = 18.dp,
                width = 24.dp,
                minWidth = 42.dp,
                height = 6.dp,
                textStyle = MaterialTheme.typography.displayMedium,
                loadingSize = 38.dp,
            )

            RackRatingSlider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                indicatorWidth = 4.dp,
                majorTickWidth = 3.5.dp,
                minorTickWidth = 2.5.dp,
                tickSpacing = 10.dp,
            )

            Spacer(modifier = Modifier.height(64.dp))

            /*Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = "%.1f".format(sliderPosition * 10))
                Slider(value = sliderPosition, onValueChange = {
                    sliderPosition = it
                    onValueChange(it)
                })
            }*/

            FilledTonalButton(
                onClick = {
                    onValueChange(sliderPosition)
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