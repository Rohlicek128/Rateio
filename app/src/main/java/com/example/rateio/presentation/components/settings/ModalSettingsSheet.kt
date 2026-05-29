package com.example.rateio.presentation.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rateio.data.remote.TmdbShowMetadata
import com.example.rateio.presentation.components.MajorSectionHeader


@Composable
fun ModalShowsSettingsSheet(
    metadata: TmdbShowMetadata,
    onValueChange: (TmdbShowMetadata) -> Unit,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

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
            MajorSectionHeader("Shows Settings")
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedToggleButton(
                checked = metadata.showSpoilers,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    onValueChange(metadata.copy(showSpoilers = it))
                },
                shapes = ToggleButtonDefaults.shapes(),
            ) {
                Text(
                    "Show Spoilers",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}