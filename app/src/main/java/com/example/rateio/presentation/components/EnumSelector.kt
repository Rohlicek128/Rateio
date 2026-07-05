package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rateio.model.HasDisplayName


@Composable
inline fun <reified T : Enum<T>> ModalEnumSelector(
    modifier: Modifier = Modifier,
    title: String,
    selectedOption: T,
    crossinline onOptionSelected: (T) -> Unit,
    noinline onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val options = enumValues<T>()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MajorSectionHeader(title)

            LazyColumn(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large.copy(
                        bottomStart = MaterialTheme.shapes.extraLarge.bottomStart,
                        bottomEnd = MaterialTheme.shapes.extraLarge.bottomEnd,
                    )),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(options, key = { it }) { option ->
                    val selected = option == selectedOption
                    ListItem(
                        headlineContent = {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                text = (option as? HasDisplayName)?.displayName ?: option.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingContent = {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        },
                        colors = if (selected) {
                            ListItemDefaults.colors().copy(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else ListItemDefaults.colors(),
                        tonalElevation = if (selected) ListItemDefaults.Elevation else 1.dp,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                onOptionSelected(option)
                                onDismiss()
                            })
                    )
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}