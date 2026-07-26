package com.rohlicek.rateio.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.settings.SettingsValueText


@Composable
fun FloatingToast(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraExtraLarge,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = BorderStroke(4.dp, MaterialTheme.colorScheme.surfaceContainerHighest),
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit),
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }),
        modifier = modifier
    ) {
        Surface(
            shape = shape,
            color = color,
            contentColor = contentColor,
            border = border,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 54.dp, vertical = 16.dp)
                .clip(shape)
                .then(
                    if (onClick != null) Modifier.clickable(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onClick()
                    }) else Modifier
                ),
            content = content,
        )
    }
}


@Composable
fun RatingsSyncToast(
    isVisible: Boolean,
    progress: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    FloatingToast(
        modifier = modifier,
        isVisible = isVisible,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Syncing IMDb Ratings...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (progress >= 0) {
                    Spacer(modifier = Modifier.weight(1f))
                    SettingsValueText("$progress%")
                }
            }

            if (progress >= 0) {
                LinearWavyProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = { WavyProgressIndicatorDefaults.indicatorAmplitude(it) * 0.75f },
                )
            } else {
                LinearWavyProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 0.5f,
                )
            }
        }
    }
}