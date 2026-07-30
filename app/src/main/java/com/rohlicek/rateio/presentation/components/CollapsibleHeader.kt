package com.rohlicek.rateio.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun CollapsibleHeader(
    title: String,
    isOpened: Boolean,
    onClick: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    var opened by remember { mutableStateOf(isOpened) }

    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        trailingContent = if (onClick != null) {
            {
                Icon(
                    imageVector = if (opened) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        } else null,
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 0.dp)
            .clip(MaterialTheme.shapes.largeIncreased)
            then(
                if (onClick != null) Modifier.clickable(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    opened = !opened
                    onClick(opened)
                }) else Modifier
            ),
        tonalElevation = if (opened) 0.dp else 2.dp,
    )

    AnimatedVisibility(
        visible = opened,
        enter = expandVertically(animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )),
        exit = shrinkVertically(animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )),
        modifier = Modifier.clip(MaterialTheme.shapes.large)
    ) {
        content?.invoke()
    }
}