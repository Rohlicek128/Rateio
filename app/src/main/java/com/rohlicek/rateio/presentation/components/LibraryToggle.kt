package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ElevatedToggleButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow


@Composable
fun LibraryToggle(
    checked: Boolean,
    itemName: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    var openDeleteDialog by remember { mutableStateOf(false) }

    ElevatedToggleButton(
        modifier = modifier,
        checked = !checked,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            if (checked) openDeleteDialog = true
            else onCheckedChange(true)
        },
        colors = ToggleButtonDefaults.elevatedToggleButtonColors().copy(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
        ),
        shapes = ToggleButtonDefaults.shapes(),
    ) {
        if (checked) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Remove from library",
                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
            )
        } else {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add to library",
                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
            )
        }

        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))

        Text(
            if (checked) "Remove from library" else "Add to library",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (openDeleteDialog) {
        AlertDialogExample(
            onDismissRequest = {
                openDeleteDialog = false
            },
            onConfirmation = {
                openDeleteDialog = false
                onCheckedChange(false)
            },
            dialogTitle = "Delete $itemName from library?",
            dialogText = "You will permanently delete $itemName from your library.",
        )
    }
}