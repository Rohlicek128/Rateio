package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp


private fun Color.toHexCode(): String {
    return String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
}

@Composable
fun HsvColorPickerDialog(
    initialColor: Color,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val initialHsv = remember(initialColor) {
        FloatArray(3).apply {
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(
                    (initialColor.alpha * 255).toInt(),
                    (initialColor.red * 255).toInt(),
                    (initialColor.green * 255).toInt(),
                    (initialColor.blue * 255).toInt()
                ),
                this
            )
        }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) } // 0f .. 360f
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) } // 0f .. 1f
    var value by remember { mutableFloatStateOf(initialHsv[2]) } // 0f .. 1f

    val currentRgbColor = remember(hue, saturation, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Pick Color") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SaturationValueBox(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onSelectionChanged = { sat, valPrice ->
                        saturation = sat
                        value = valPrice
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(currentRgbColor)
                    )
                    Text(
                        text = currentRgbColor.toHexCode(),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Hue", style = MaterialTheme.typography.bodySmall)
                    HueSlider(
                        hue = hue,
                        onHueChanged = { hue = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onColorSelected(currentRgbColor)
                onDismissRequest()
            }) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SaturationValueBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onSelectionChanged: (sat: Float, value: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pureHueColor = remember(hue) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    }

    val cornerRadius = 40f

    Box(
        modifier = modifier
            //.clip(MaterialTheme.shapes.large)
            .pointerInput(Unit) {
                // Catch sudden precise single tap events
                detectTapGestures { offset ->
                    val sat = (offset.x / size.width).coerceIn(0f, 1f)
                    val valPrice = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                    onSelectionChanged(sat, valPrice)
                }
            }
            .pointerInput(Unit) {
                // Track finger dragging across coordinates continuously
                detectDragGestures { change, _ ->
                    val sat = (change.position.x / size.width).coerceIn(0f, 1f)
                    val valPrice = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                    onSelectionChanged(sat, valPrice)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, pureHueColor)
                ),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                ),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )

            val cursorX = saturation * size.width
            val cursorY = (1f - value) * size.height

            // Indicator
            drawCircle(
                color = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))),
                radius = 14.dp.toPx(),
                center = Offset(cursorX, cursorY),
            )
            drawCircle(
                color = (if (value > 0.5f) Color.Black else Color.White).copy(alpha = 0.5f),
                radius = 12.dp.toPx(),
                center = Offset(cursorX, cursorY),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate full spectrum rainbow list sequence programmatically
    val rainbowColors = remember {
        (0..360 step 60).map { Color(android.graphics.Color.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))) }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Linear rainbow color background track strip layout block background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(MaterialTheme.shapes.large)
                .background(Brush.horizontalGradient(rainbowColors))
        )

        Slider(
            value = hue,
            onValueChange = onHueChanged,
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}