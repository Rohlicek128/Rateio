package com.rohlicek.rateio.presentation.rating

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.ModalSettings
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsTextField
import com.rohlicek.rateio.utils.formatDate
import com.rohlicek.rateio.utils.parseDate


@Composable
fun EditableRateItemDetailScreen(
    item: RateItem,
    categoryName: String?,
    onItemUpdate: (RateItem) -> Unit,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onBackClick: () -> Unit,
) {
    val viewModel: EditableRateItemViewModel = viewModel(
        factory = EditableRateItemViewModel.factory(item)
    )
    val state = viewModel.itemState

    val haptic = LocalHapticFeedback.current


    var showSettings by remember { mutableStateOf(false) }
    if (showSettings) {
        ModalSettings(
            title = "$categoryName Settings",
            onDismiss = { showSettings = false }
        ) {
            item {
                SettingListItem(
                    title = "Title",
                    description = "Name of the item",
                    position = ListItemPosition.START,
                    supportingContent = {
                        SettingsTextField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.title,
                            onValueChange = { value ->
                                viewModel.updateItem { it.copy(title = value) }
                            },
                            placeholder = { Text("eg. #") },
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Subtitle",
                    description = "Description or a subtitle of the item",
                    position = ListItemPosition.MIDDLE,
                    supportingContent = {
                        SettingsTextField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.subtitle ?: "",
                            onValueChange = { value ->
                                viewModel.updateItem { it.copy(subtitle = value) }
                            },
                            placeholder = { Text("eg. #") },
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "Cover Image",
                    description = "Image URL of the cover image",
                    position = ListItemPosition.END,
                    supportingContent = {
                        SettingsTextField(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            value = state.coverImageUrl ?: state.coverImageLowUrl ?: "",
                            onValueChange = { value ->
                                viewModel.updateItem { it.copy(coverImageUrl = value, coverImageLowUrl = value) }
                            },
                            singleLine = false,
                            placeholder = { Text("eg. https://example.org/image.jpg") },
                        )
                    }
                    ,
                    trailingContent = {
                        AnimatedVisibility(state.coverImageUrl != null || state.coverImageLowUrl != null) {
                            IconButton(
                                onClick = {
                                    viewModel.updateItem { it.copy(coverImageUrl = null, coverImageLowUrl = null) }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, null)
                            }
                        }
                    }
                )
            }
        }
    }

    RateItemDetailScreen(
        title = state.title,
        subtitle = state.subtitle,
        categoryName = categoryName,
        description = null,
        coverImageUrl = state.coverImageOverride ?: state.coverImageUrl,
        backdropImageUrl = null,
        placeholderRatio = 16f / 9f,
        rating = state.rating,
        onBackClick = onBackClick,
        onRatingSaved = onRatingSaved,
        onOpenSettings = { showSettings = true },
        debug = "${state.id}, ${state.parentId}, ${state.externalId}, ${state.externalSource}," +
                " ${formatDate(parseDate(state.updatedAt))}," +
                " ${formatDate(parseDate(state.createdAt))}, ${state.metadataJSON}",
        extraContent = {

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    FilledTonalButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            onItemUpdate(state)
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
            }

        },
    )
}