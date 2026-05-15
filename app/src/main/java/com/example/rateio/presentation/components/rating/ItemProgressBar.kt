package com.example.rateio.presentation.components.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.model.ItemStatus
import com.example.rateio.presentation.components.label


@Composable
fun ItemProgressBar(
    modifier: Modifier = Modifier,
    startString: String? = null,
    startValue: Float = 0f,
    endString: String,
    endValue: Float,
    currentString: String? = null,
    currentValue: Float = 0f,
    finishString: String = "COMPLETED",
    status: ItemStatus = ItemStatus.IN_PROGRESS,
    onClick: (() -> Unit)? = null,
) {
    val progress = ((currentValue - startValue) / (endValue - startValue)).coerceIn(0f, 1f)

    val statusString = when {
        status == ItemStatus.COMPLETED || progress >= 1f -> ItemStatus.COMPLETED.label()
        status == ItemStatus.ON_HOLD -> ItemStatus.ON_HOLD.label()
        status == ItemStatus.DROPPED -> ItemStatus.DROPPED.label()
        else -> null
    }

    ListItem(
        headlineContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (startString != null) {
                        Text(
                            text = startString,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (currentString != null && statusString == null) {
                        Text(
                            text = currentString,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (statusString != null) {
                        Text(
                            text = statusString.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = endString,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                val isStopped = status == ItemStatus.COMPLETED || status == ItemStatus.DROPPED || status == ItemStatus.ON_HOLD
                LinearWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .padding(bottom = 2.dp),
                    waveSpeed = 10.dp,
                    wavelength = 22.dp,
                    amplitude = {
                        if (!isStopped) WavyProgressIndicatorDefaults.indicatorAmplitude(it)
                        else 0f
                    }
                )
            }
        },
        tonalElevation = 1.dp,
        modifier = modifier
            .clip(MaterialTheme.shapes.largeIncreased)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
    )
}