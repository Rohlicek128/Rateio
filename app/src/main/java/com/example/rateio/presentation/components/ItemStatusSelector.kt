package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rateio.model.ItemStatus


@Composable
fun ItemStatusSelector(
    selected: ItemStatus,
    onStatusSelected: (ItemStatus) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ((openSheet: () -> Unit) -> Unit)? = null,
) {
    var showSheet by remember { mutableStateOf(false) }
    val itemStatuses = ItemStatus.entries.filter { it != ItemStatus.NONE }
    val itemStatusList = itemStatuses.map { it.label() }
    val selectedIndex = itemStatusList.indexOf(selected.label())

    content?.invoke {
        showSheet = true
    }

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = modifier.padding(horizontal = 16.dp)
            ) {
                MajorSectionHeader("Status")
                Spacer(modifier = Modifier.height(4.dp))

                SelectionList(
                    selectedIndex = selectedIndex,
                    listNames = itemStatusList,
                    onSelect = {
                        onStatusSelected(itemStatuses[it])
                        showSheet = false
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                SelectionList(
                    selectedIndex = if (selected == ItemStatus.NONE) 0 else 1,
                    listNames = listOf(ItemStatus.NONE.label()),
                    onSelect = {
                        onStatusSelected(ItemStatus.NONE)
                        showSheet = false
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

fun ItemStatus.label() = when (this) {
    ItemStatus.NONE -> "None"
    ItemStatus.WATCHLIST -> "Watchlist"
    ItemStatus.IN_PROGRESS -> "In Progress"
    ItemStatus.COMPLETED -> "Completed"
    ItemStatus.DROPPED -> "Dropped"
    ItemStatus.ON_HOLD -> "On Hold"
}