package com.example.rateio.presentation.settings.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.presentation.ScreenScaffold
import com.example.rateio.presentation.components.RateBox
import com.example.rateio.presentation.components.RatingBottomSheet
import com.example.rateio.presentation.rating.display.RatingTransformation
import com.example.rateio.presentation.rating.display.RatingTransformationsConstants
import com.example.rateio.presentation.rating.display.getTransformedRating
import com.example.rateio.presentation.settings.ErrorCard
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import com.example.rateio.presentation.settings.SettingsListHeader
import com.example.rateio.presentation.settings.SettingsNumberField
import com.example.rateio.presentation.settings.SettingsTextField
import com.example.rateio.presentation.settings.WarningCard


@Composable
fun RatingTransformationSettingsScreen(
    defaultTransformations: RatingTransformation = RatingTransformationsConstants.TF_IMDB,
    onSave: (RatingTransformation) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: RatingTransformationSettingsViewModel = viewModel(
        factory = RatingTransformationSettingsViewModel.factory(defaultTransformations)
    )
    val state = viewModel.uiState

    val haptic = LocalHapticFeedback.current

    var showRatingSheet by remember { mutableStateOf(false) }

    val canSave = {
        state.divider != 0.0f
    }

    ScreenScaffold(
        title = "Transformation",
        onBackClick = onBackClick,
        actions = {
            FilledTonalButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onSave(state)
                    onBackClick()
                },
                shapes = ButtonDefaults.shapes(),
                colors = ButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                enabled = canSave(),
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
            item {
                AnimatedVisibility(
                    visible = state.divider == 0.0f,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    ErrorCard(
                        title = "Division by zero",
                        description = "The divider is set to 0.0. Can't divide by zero!",
                        darker = true,
                    )
                }
            }
            item {
                AnimatedVisibility(
                    visible = getTransformedRating(1f, rtf = state)
                        .replace(".", "")
                        .replace(",", "")
                        .length >= 5,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    WarningCard(
                        title = "Getting too long",
                        description = "Transformed rating is getting too long. This could pose rendering issues, mainly in episode grids!"
                    )
                }
            }


            item { SettingsListHeader("Testing") }
            item {
                SettingListItem(
                    title = "Rating Showcase",
                    description = "Click on the rating to change it",
                    position = ListItemPosition.SINGLE,
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = viewModel.testRating?.toString() ?: "Null",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            RateBox(
                                rating = viewModel.testRating,
                                roundedCorners = 12.dp,
                                width = 12.dp,
                                minWidth = 36.dp,
                                height = 4.dp,
                                textStyle = MaterialTheme.typography.headlineSmall,
                                transformationOverride = state,
                                onClick = {
                                    showRatingSheet = true
                                },
                            )
                        }
                    }
                )

                if (showRatingSheet) {
                    RatingBottomSheet(
                        rating = viewModel.testRating,
                        onDismiss = { showRatingSheet = false },
                        onValueChange = { viewModel.testRating = it },
                        transformationOverride = state,
                    )
                }
            }

            item { SettingsListHeader("Arithmetic") }
            item {
                SettingListItem(
                    title = "Step Count",
                    description = "How many rating option to choose from",
                    position = ListItemPosition.START,
                    supportingContent = {
                        Slider(
                            state.stepCount.toFloat(),
                            onValueChange = { value ->
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                viewModel.updateTransformations { it.copy(stepCount = value.toUInt()) }
                            },
                            steps = 49,
                            valueRange = 0f..500f
                        )
                    },
                    trailingContent = {
                        Text(
                            modifier = Modifier.padding(end = 6.dp),
                            text = state.stepCount.toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Offset",
                    description = "With value of ${state.offset}",
                    position = ListItemPosition.MIDDLE,
                    supportingContent = {
                        SettingsNumberField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.offset,
                            onValueChange = { value ->
                                viewModel.updateTransformations { it.copy(offset = value) }
                            },
                            placeholder = { Text("eg. 0.0") }
                        )
                    },
                )
            }
            item {
                SettingListItem(
                    title = "Divider",
                    description = "With value of ${state.divider}",
                    position = ListItemPosition.END,
                    supportingContent = {
                        SettingsNumberField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.divider,
                            onValueChange = { value ->
                                viewModel.updateTransformations { it.copy(divider = value) }
                            },
                            placeholder = { Text("eg. 1.0") }
                        )
                    },
                )
            }

            item { SettingsListHeader("Formating") }
            item {
                SettingListItem(
                    title = "Decimal Places",
                    description = "With value of ${state.decimalPlaces}",
                    position = ListItemPosition.START,
                    supportingContent = {
                        Slider(
                            state.decimalPlaces.toFloat(),
                            onValueChange = { value ->
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                viewModel.updateTransformations { it.copy(decimalPlaces = value.toUInt()) }
                            },
                            steps = 4,
                            valueRange = 0f..5f,
                        )
                    },
                )
            }
            item {
                SettingListItem(
                    title = "Leading String",
                    description = "How many rating option to choose from. Value: \"${state.leadingString}\"",
                    position = ListItemPosition.MIDDLE,
                    supportingContent = {
                        SettingsTextField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.leadingString,
                            onValueChange = { value ->
                                viewModel.updateTransformations { it.copy(leadingString = value) }
                            },
                            placeholder = { Text("eg. #") },
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Trailing String",
                    description = "How many rating option to choose from. Value: \"${state.trailingString}\"",
                    position = ListItemPosition.MIDDLE,
                    supportingContent = {
                        SettingsTextField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.trailingString,
                            onValueChange = { value ->
                                viewModel.updateTransformations { it.copy(trailingString = value) }
                            },
                            placeholder = { Text("eg. %") }
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Locale",
                    description = "Value: \"${state.locale.language}\"",
                    position = ListItemPosition.END,
                )
            }


            item { Spacer(modifier = Modifier.height(150.dp)) }

        }
    }
}