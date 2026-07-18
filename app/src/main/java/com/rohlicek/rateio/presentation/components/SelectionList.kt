package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun SelectionList(
    selectedIndex: Int,
    listNames: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    val largeCorner = MaterialTheme.shapes.extraLarge
    val mediumCorner = MaterialTheme.shapes.large
    val smallCorner = MaterialTheme.shapes.small

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listNames.forEachIndexed { index, name ->
            val isSelected = selectedIndex == index
            val cornerShape = if (isSelected || listNames.size == 1) mediumCorner else smallCorner
            val shape = when (index) {
                0 if listNames.size > 1 -> largeCorner.copy(
                    bottomStart = cornerShape.bottomStart,
                    bottomEnd = cornerShape.bottomEnd,
                )
                listNames.lastIndex if listNames.size > 1 -> largeCorner.copy(
                    topStart = cornerShape.topStart,
                    topEnd = cornerShape.topEnd,
                )
                else -> cornerShape
            }

            ListItem(
                headlineContent = {
                    Text(
                        name,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                },
                colors = ListItemDefaults.colors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .clip(shape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        onSelect(index)
                    },
            )
        }
    }
}