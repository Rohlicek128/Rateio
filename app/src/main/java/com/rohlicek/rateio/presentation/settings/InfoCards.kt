package com.rohlicek.rateio.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun ErrorCard(
    description: String,
    modifier: Modifier = Modifier,
    title: String = "Error",
    darker: Boolean = false,
) {
    SettingListItem(
        modifier = modifier,
        title = title,
        boldTitle = true,
        description = description,
        icon = Icons.Default.Error,
        containerColor = if (darker) MaterialTheme.colorScheme.onError
            else MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        subtitleColor = MaterialTheme.colorScheme.onErrorContainer,
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
        boldTitle = true,
        description = description,
        icon = Icons.Default.Warning,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        subtitleColor = MaterialTheme.colorScheme.onErrorContainer,
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