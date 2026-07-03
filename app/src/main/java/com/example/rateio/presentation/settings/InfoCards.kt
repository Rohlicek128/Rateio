package com.example.rateio.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun ErrorCard(
    description: String,
    modifier: Modifier = Modifier,
    title: String = "Error",
) {
    SettingListItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = Icons.Default.Error,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.onError,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    )
}

@Composable
fun WarningCard(
    description: String,
    modifier: Modifier = Modifier,
    title: String = "Warning",
) {
    SettingListItem(
        modifier = modifier,
        title = title,
        description = description,
        icon = Icons.Default.Warning,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    )
}

@Composable
fun InfoCard(
    //title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    SettingListItem(
        modifier = modifier,
        title = "Info",
        description = description,
        icon = Icons.Default.Info,
    )
}