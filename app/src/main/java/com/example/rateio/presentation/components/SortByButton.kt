package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.rateio.presentation.rating.tmdb.SortMode

@Composable
fun SortBySelectionButton(
    selected: SortMode,
    onSelect: (SortMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    var showSheet by remember { mutableStateOf(false) }
    val itemStatusList = SortMode.entries.map { it.label() }
    val selectedIndex = itemStatusList.indexOf(selected.label())

    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            showSheet = true
        },
        modifier = modifier,
        shapes = ButtonDefaults.shapes(),
        /*colors = ButtonColors(
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )*/
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Sort,
            contentDescription = "Sort by",
            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            "Sort by",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                MajorSectionHeader("Sort by")
                Spacer(modifier = Modifier.height(4.dp))

                SelectionList(
                    selectedIndex = selectedIndex,
                    listNames = itemStatusList,
                    onSelect = {
                        onSelect(SortMode.entries[it])
                        showSheet = false
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

}

fun SortMode.label() = when (this) {
    SortMode.BY_SEASON -> "Season"
    SortMode.BY_RATING_BEST -> "Rating (Best)"
    SortMode.BY_RATING_WORST -> "Rating (Worst)"
    SortMode.BY_RUNTIME -> "Runtime"
    else -> "Unknown"
}