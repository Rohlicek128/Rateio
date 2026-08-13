package com.rohlicek.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.ScreenScaffold


@Composable
fun SettingsAboutScreen(
    onBackClick: () -> Unit,
) {
    ScreenScaffold(
        title = "About",
        onBackClick = onBackClick,
    ) { padding, listState ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            state = listState,
        ) {
            item { SettingsListHeader("Testing") }
            item {
                SettingListItem(
                    title = "Gradient",
                    description = "Decides if every step has it's own interpolated color, that is between the two closest buckets.",
                    position = ListItemPosition.SINGLE,
                    trailingContent = {
                        SettingsSwitch(
                            checked = false,
                            onCheckedChange = {}
                        )
                    }
                )
            }

        }
    }
}