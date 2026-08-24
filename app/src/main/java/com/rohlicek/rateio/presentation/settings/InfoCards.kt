package com.rohlicek.rateio.presentation.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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
        colors = ListItemDefaults.colors().copy(
            containerColor = if (darker) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            supportingContentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
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
        colors = ListItemDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            supportingContentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    )
}

@Composable
fun InfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    SettingListItem(
        modifier = modifier.padding(bottom = 4.dp),
        title = title,
        description = description,
        titleStyle = MaterialTheme.typography.titleSmall,
        descriptionStyle = MaterialTheme.typography.bodySmall,
        icon = Icons.Outlined.Info,
        colors = ListItemDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.primaryFixed,
            contentColor = MaterialTheme.colorScheme.onPrimaryFixed,
            supportingContentColor = MaterialTheme.colorScheme.onPrimaryFixed,
            leadingContentColor = MaterialTheme.colorScheme.onPrimaryFixed,
        ),
    )
}