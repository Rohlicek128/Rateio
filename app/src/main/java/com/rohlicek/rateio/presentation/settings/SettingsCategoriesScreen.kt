package com.rohlicek.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Slider
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
import com.rohlicek.rateio.presentation.ScreenScaffold


@Composable
fun SettingsCategoriesScreen(
    onBackClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    var checked by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(4f) }
    var counterValue by remember { mutableIntStateOf(1) }

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
                        SettingsSwitch(
                            checked = checked,
                            onCheckedChange = { checked = it }
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Slider",
                    description = "With value of $sliderValue",
                    position = ListItemPosition.MIDDLE,
                    supportingContent = {
                        Slider(
                            sliderValue,
                            onValueChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                sliderValue = it
                            },
                            steps = 9,
                            valueRange = 0f..10f
                        )
                    },
                )
            }
            item {
                SettingListItem(
                    title = "Counter",
                    description = "With value of $counterValue",
                    position = ListItemPosition.END,
                    trailingContent = {
                        IntCounter(
                            value = counterValue,
                            onValueChange = { counterValue = it },
                            minValue = 1,
                            maxValue = 100,
                        )
                    }
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
                        SettingsSwitch(
                            checked = checked,
                            onCheckedChange = { checked = it }
                        )
                    }
                )
            }
        }
    }
}