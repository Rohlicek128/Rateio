package com.example.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Transform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.ScreenScaffold


@Composable
fun SettingsScreen(
    onRatingClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    ScreenScaffold(
        title = "Settings",
        onBackClick = onBackClick,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
        ) {
            item {
                SettingListItem(
                    title = "Appearance",
                    description = "Theme, style",
                    icon = Icons.Default.Palette,
                    position = ListItemPosition.START,
                )
            }
            item {
                SettingListItem(
                    title = "Rating Visualization",
                    description = "Colors, Rating transformations",
                    icon = Icons.Default.Transform,
                    onClick = onRatingClick,
                    position = ListItemPosition.MIDDLE,
                )
            }
            item {
                SettingListItem(
                    title = "Categories",
                    description = "Shows, Movies, Games...",
                    icon = Icons.Default.Category,
                    onClick = onCategoriesClick,
                    position = ListItemPosition.MIDDLE,
                )
            }
            item {
                SettingListItem(
                    title = "About",
                    description = "App info",
                    icon = Icons.Default.Info,
                    position = ListItemPosition.END,
                )
            }
        }
    }
}