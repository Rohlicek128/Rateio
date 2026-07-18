package com.rohlicek.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DatasetLinked
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Transform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.rating.display.getRatingColor


@Composable
fun SettingsScreen(
    onAppearanceClick: () -> Unit,
    onRatingClick: () -> Unit,
    onDatabaseClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val alpha = 0.5f
    val step = 0.1f
    var settingCount = 1f

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
                val colors = getRatingColor(settingCount)
                settingCount -= step
                SettingListItem(
                    title = "Appearance",
                    description = "Theme, style",
                    icon = Icons.Default.Palette,
                    //iconContentColor = colors.foregroundColor,
                    iconContainerColor = colors.backgroundColor.copy(alpha = alpha),
                    onClick = onAppearanceClick,
                    position = ListItemPosition.START,
                )

            }
            item {
                val colors = getRatingColor(settingCount)
                settingCount -= step
                SettingListItem(
                    title = "Rating Visualization",
                    description = "Colors, Rating transformations",
                    icon = Icons.Default.Transform,
                    //iconContentColor = colors.foregroundColor,
                    iconContainerColor = colors.backgroundColor.copy(alpha = alpha),
                    onClick = onRatingClick,
                    position = ListItemPosition.MIDDLE,
                )
            }
            item {
                val colors = getRatingColor(settingCount)
                settingCount -= step
                SettingListItem(
                    title = "Categories",
                    description = "Shows, Movies, Games...",
                    icon = Icons.Default.Category,
                    //iconContentColor = colors.foregroundColor,
                    iconContainerColor = colors.backgroundColor.copy(alpha = alpha),
                    onClick = onCategoriesClick,
                    position = ListItemPosition.MIDDLE,
                )
            }
            item {
                val colors = getRatingColor(settingCount)
                settingCount -= step
                SettingListItem(
                    title = "Database",
                    description = "IMDb ratings",
                    icon = Icons.Default.DatasetLinked,
                    //iconContentColor = colors.foregroundColor,
                    iconContainerColor = colors.backgroundColor.copy(alpha = alpha),
                    onClick = onDatabaseClick,
                    position = ListItemPosition.MIDDLE,
                )
            }
            item {
                val colors = getRatingColor(settingCount)
                settingCount -= step
                SettingListItem(
                    title = "About",
                    description = "App info",
                    //iconContentColor = colors.foregroundColor,
                    iconContainerColor = colors.backgroundColor.copy(alpha = alpha),
                    icon = Icons.Default.Info,
                    position = ListItemPosition.END,
                )
            }
        }
    }
}