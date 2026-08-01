package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider


@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    tonalElevation: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    leadingPadding: Dp = 0.dp,
    overlineContent: @Composable (ColumnScope.() -> Unit)? = null,
    headlineContent: @Composable (ColumnScope.() -> Unit)? = null,
    subtitleContent: @Composable (ColumnScope.() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 70.dp)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            ),
        shape = shape,
        color = colors.containerColor,
        tonalElevation = tonalElevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingContent != null) {
                CompositionLocalProvider(LocalContentColor provides colors.leadingIconColor) {
                    leadingContent()
                }
            }

            Column(
                modifier = Modifier.padding(start = leadingPadding),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (overlineContent != null) {
                            CompositionLocalProvider(LocalContentColor provides colors.overlineColor) {
                                overlineContent()
                            }
                        }
                        if (headlineContent != null) {
                            CompositionLocalProvider(LocalContentColor provides colors.headlineColor) {
                                headlineContent()
                            }
                        }
                        if (subtitleContent != null) {
                            CompositionLocalProvider(LocalContentColor provides colors.supportingTextColor) {
                                subtitleContent()
                            }
                        }
                    }

                    if (trailingContent != null) {
                        Spacer(modifier = Modifier.width(6.dp))

                        CompositionLocalProvider(LocalContentColor provides colors.trailingIconColor) {
                            trailingContent()
                        }
                    }
                }

                if (supportingContent != null) {
                    CompositionLocalProvider(LocalContentColor provides colors.supportingTextColor) {
                        supportingContent()
                    }
                }
            }
        }
    }
}