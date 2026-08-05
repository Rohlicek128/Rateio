package com.rohlicek.rateio.presentation.settings.rating

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.HsvColorPickerDialog
import com.rohlicek.rateio.presentation.components.ItemCard
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.components.RatingBottomSheet
import com.rohlicek.rateio.presentation.components.SaveButton
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucket
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsListHeader
import com.rohlicek.rateio.presentation.settings.SettingsPlaceholderText
import com.rohlicek.rateio.presentation.settings.SettingsSwitch
import com.rohlicek.rateio.presentation.settings.SettingsTextField


@Composable
fun RatingColorSettingsScreen(
    defaultBuckets: RatingColorBuckets = getCurrentRatingColorBuckets(),
    onSave: (RatingColorBuckets) -> Unit,
    onBackClick: () -> Unit,
) {
    val viewModel: RatingColorSettingsViewModel = viewModel(
        factory = RatingColorSettingsViewModel.factory(defaultBuckets)
    )
    val state = viewModel.uiState

    var showRatingSheet by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Color Buckets",
        onBackClick = onBackClick,
        actions = {
            SaveButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = {
                    onSave(state)
                    onBackClick()
                }
            )
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
                                colorBucketsOverride = state,
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
                        colorBucketsOverride = state,
                    )
                }
            }

            item { SettingsListHeader("Details") }
            item {
                SettingListItem(
                    title = "Name",
                    description = "The name of the color buckets.",
                    position = ListItemPosition.START,
                    supportingContent = {
                        SettingsTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            value = state.name,
                            onValueChange = { value ->
                                viewModel.updateBuckets { it.copy(name = value) }
                            },
                            placeholder = { SettingsPlaceholderText("eg. My Colors") }
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Gradient",
                    description = "Decides if every step has it's own interpolated color, that is between the two closest buckets.",
                    position = ListItemPosition.END,
                    trailingContent = {
                        SettingsSwitch(
                            checked = state.gradient,
                            onCheckedChange = { value ->
                                viewModel.updateBuckets { it.copy(gradient = value) }
                            }
                        )
                    }
                )
            }

            item { SettingsListHeader("Buckets") }
            itemsIndexed(
                state.buckets,
                key = { _, bucket -> "${bucket.label}_${bucket.equalOrGreaterThen ?: "null"}" }
            ) { index, _ ->
                BucketEditorCard(
                    buckets = state,
                    index = index,
                    transform = { newBucket ->
                        viewModel.updateBucketAtIndex(index, newBucket)
                    }
                )
            }

        }
    }
}

private enum class ColorTarget { BACKGROUND, FOREGROUND }

@Composable
private fun BucketEditorCard(
    buckets: RatingColorBuckets,
    index: Int,
    transform: (RatingColorBucket) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val bucket = buckets.buckets[index]

    val interactionSources = remember(2) {
        List(2) { MutableInteractionSource() }
    }

    var editingTarget by remember { mutableStateOf<ColorTarget?>(null) }
    editingTarget?.let { target ->
        val initialColor = when (target) {
            ColorTarget.BACKGROUND -> bucket.backgroundColor
            ColorTarget.FOREGROUND -> bucket.foregroundColor
        }
        HsvColorPickerDialog(
            initialColor = initialColor,
            onDismissRequest = { editingTarget = null },
            onColorSelected = { color ->
                transform(
                    when (target) {
                        ColorTarget.BACKGROUND -> bucket.copy(backgroundColor = color)
                        ColorTarget.FOREGROUND -> bucket.copy(foregroundColor = color)
                    }
                )
                editingTarget = null
            }
        )
    }

    var showRangeSheet by remember { mutableStateOf(false) }
    if (showRangeSheet) {
        RatingBottomSheet(
            rating = bucket.equalOrGreaterThen,
            onDismiss = { showRangeSheet = false },
            onValueChange = { range ->
                transform(bucket.copy(equalOrGreaterThen = range))
            },
            colorBucketsOverride = buckets,
        )
    }

    ItemCard(
        modifier = Modifier.padding(vertical = 4.dp),
        headlineContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        showRangeSheet = true
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        text = bucket.equalOrGreaterThen?.let { egt -> "≥${getTransformedRating(egt)}" } ?: "Null",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                    )
                }


                SettingsTextField(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    value = bucket.label ?: "",
                    showIcon = false,
                    onValueChange = { value ->
                        transform(bucket.copy(label = value.ifBlank { null }))
                    },
                    placeholder = { SettingsPlaceholderText("eg. My Colors") }
                )
            }
        },
        supportingContent = {
            ButtonGroup(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                expandedRatio = 0.15f,
                overflowIndicator = {},
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                customItem(
                    buttonGroupContent = {
                        ColorEditCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .animateWidth(interactionSource = interactionSources[0]),
                            color = bucket.backgroundColor,
                            labelColor = bucket.foregroundColor,
                            label = "Background",
                            interactionSource = interactionSources[0],
                            onClick = { editingTarget = ColorTarget.BACKGROUND }
                        )
                    },
                    menuContent = {}
                )
                customItem(
                    buttonGroupContent = {
                        ColorEditCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .animateWidth(interactionSource = interactionSources[1]),
                            color = bucket.foregroundColor,
                            labelColor = bucket.backgroundColor,
                            label = "Foreground",
                            interactionSource = interactionSources[1],
                            onClick = { editingTarget = ColorTarget.FOREGROUND }
                        )
                    },
                    menuContent = {}
                )
            }
        }
    )
}

@Composable
private fun ColorEditCard(
    modifier: Modifier = Modifier,
    color: Color,
    label: String,
    labelColor: Color,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    /*Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
    }*/

    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors().copy(containerColor = color),
        shapes = ButtonDefaults.shapes(),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onClick()
        },
        interactionSource = interactionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier =  Modifier.align(Alignment.Center),
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = labelColor,
                softWrap = false,
                overflow = TextOverflow.Visible
            )
        }
    }
}