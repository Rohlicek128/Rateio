package com.rohlicek.rateio.presentation.settings.rating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.HsvColorPickerDialog
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsListHeader


@Composable
fun RatingColorSettingsScreen(
    onBackClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    var showColorPicker by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(Color(255, 0 , 0)) }

    ScreenScaffold(
        title = "Color Buckets",
        onBackClick = onBackClick,
        actions = {
            FilledTonalButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onBackClick()
                },
                shapes = ButtonDefaults.shapes(),
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save",
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(
                    "Save",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
        ) {
            item { SettingsListHeader("Testing") }
            item {
                SettingListItem(
                    title = "Color Test",
                    description = "Click on the rating to change it",
                    position = ListItemPosition.SINGLE,
                    trailingContent = {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(color)
                                .clickable(onClick = { showColorPicker = true })
                        )
                    }
                )

                if (showColorPicker) {
                    HsvColorPickerDialog(
                        initialColor = color,
                        onDismissRequest = { showColorPicker = false },
                        onColorSelected = { color = it }
                    )
                }
            }

        }
    }
}