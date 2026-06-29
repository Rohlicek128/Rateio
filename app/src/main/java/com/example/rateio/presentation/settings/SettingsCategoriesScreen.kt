package com.example.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.ScreenScaffold


@Composable
fun SettingsCategoriesScreen(
    onBackClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    var checked by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(4f) }

    ScreenScaffold(
        title = "Categories",
        onBackClick = onBackClick,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
        ) {
            item { SettingsListHeader("Testing") }
            item {
                SettingListItem(
                    title = "Switch",
                    description = "Theme, style",
                    //icon = Icons.Default.Palette,
                    position = ListItemPosition.START,
                    trailingContent = {
                        Switch(
                            checked = checked,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                checked = it
                            },
                            thumbContent = if (checked) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Slider",
                    description = "With value of $sliderValue",
                    //icon = Icons.Default.Transform,
                    position = ListItemPosition.END,
                    supportingContent = {
                        Slider(
                            sliderValue,
                            onValueChange = { sliderValue = it },
                            steps = 9,
                            valueRange = 1f..10f
                        )
                    },
                )
            }

            item { SettingsListHeader("Colors") }
            item {
                SettingListItem(
                    title = "Switch",
                    description = "Theme, style",
                    icon = Icons.Default.Android,
                    position = ListItemPosition.SINGLE,
                    trailingContent = {
                        Switch(
                            checked = checked,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                checked = it
                            },
                            thumbContent = if (checked) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                )
            }

            item { SettingsListHeader("More Options") }
            item {
                SettingListItem(
                    title = "Next Screen",
                    //description = "Something, Something important. And is quite long for a setting label/description",
                    description = "Something, Something important",
                    icon = Icons.Default.AccountCircle,
                    position = ListItemPosition.SINGLE,
                    onClick = {},
                )
            }
        }
    }
}