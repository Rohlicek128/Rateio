package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ConnectedItemSelector(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    unCheckedIcons: List<ImageVector> = emptyList(),
    checkedIcons: List<ImageVector> = emptyList(),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val haptic = LocalHapticFeedback.current

    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, label ->
            OutlinedToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    onSelectionChanged(index)
                },
                modifier = Modifier.semantics { role = Role.RadioButton },
                shapes = getConnectedButtonShapes(index, options.size),
                colors = ToggleButtonDefaults.toggleButtonColors().copy(
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            ) {
                if (unCheckedIcons.isNotEmpty() || checkedIcons.isNotEmpty()) {
                    val icon = if (unCheckedIcons.isNotEmpty() && checkedIcons.isNotEmpty()) {
                        if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index]
                    } else {
                        if (unCheckedIcons.isEmpty()) checkedIcons[index] else unCheckedIcons[index]
                    }

                    Icon(
                        icon,
                        contentDescription = "Localized description",
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
                Text(
                    label,
                    style = textStyle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


@Composable
fun OutlinedConnectedButtonsExpressive(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    unCheckedIcons: List<ImageVector> = emptyList(),
    checkedIcons: List<ImageVector> = emptyList(),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val haptic = LocalHapticFeedback.current

    ButtonsExpressive(
        modifier = modifier,
        buttonCount = options.size,
        itemSpacing = itemSpacing,
    ) { index, interactionSource ->
        OutlinedToggleButton(
            checked = selectedIndex == index,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                onSelectionChanged(index)
            },
            interactionSource = interactionSource,
            modifier = Modifier
                .semantics { role = Role.RadioButton }
                .animateWidth(interactionSource = interactionSource),
            shapes = getConnectedButtonShapes(index, options.size),
            colors = ToggleButtonDefaults.toggleButtonColors().copy(
                checkedContainerColor = MaterialTheme.colorScheme.primary,
                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            //contentPadding = PaddingValues(horizontal = 24.dp, 4.dp),
        ) {
            if (unCheckedIcons.isNotEmpty() || checkedIcons.isNotEmpty()) {
                val icon = if (unCheckedIcons.isNotEmpty() && checkedIcons.isNotEmpty()) {
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index]
                } else {
                    if (unCheckedIcons.isEmpty()) checkedIcons[index] else unCheckedIcons[index]
                }

                Icon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
            }
            Text(
                text = options[index],
                style = textStyle,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center,
                fontWeight = if (selectedIndex == index) FontWeight.ExtraBold else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun ConnectedButtonsExpressive(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    unCheckedIcons: List<ImageVector> = emptyList(),
    checkedIcons: List<ImageVector> = emptyList(),
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val haptic = LocalHapticFeedback.current

    ButtonsExpressive(
        modifier = modifier,
        buttonCount = options.size,
        itemSpacing = itemSpacing,
        expandedRatio = 0.2f
    ) { index, interactionSource ->
        ToggleButton(
            checked = selectedIndex == index,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                onSelectionChanged(index)
            },
            interactionSource = interactionSource,
            modifier = Modifier
                .semantics { role = Role.RadioButton }
                .animateWidth(interactionSource = interactionSource),
            shapes = getConnectedButtonShapes(index, options.size),
            colors = ToggleButtonDefaults.toggleButtonColors().copy(
                checkedContainerColor = MaterialTheme.colorScheme.primary,
                checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, 2.dp)
        ) {
            if (unCheckedIcons.isNotEmpty() || checkedIcons.isNotEmpty()) {
                val icon = if (unCheckedIcons.isNotEmpty() && checkedIcons.isNotEmpty()) {
                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index]
                } else {
                    if (unCheckedIcons.isEmpty()) checkedIcons[index] else unCheckedIcons[index]
                }

                Icon(
                    icon,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
            }
            Text(
                text = options[index],
                style = textStyle,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                textAlign = TextAlign.Center,
                fontWeight = if (selectedIndex == index) FontWeight.ExtraBold else FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun ButtonsExpressive(
    buttonCount: Int,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = ButtonGroupDefaults.ConnectedSpaceBetween,
    expandedRatio: Float = 0.15f,
    buttonContent: @Composable ButtonGroupScope.(index: Int, interactionSource: MutableInteractionSource) -> Unit,
) {
    val interactionSources = remember(buttonCount) {
        List(buttonCount) { MutableInteractionSource() }
    }

    ButtonGroup(
        modifier = modifier,
        expandedRatio = expandedRatio,
        overflowIndicator = {},
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
        for (i in 0 until buttonCount) {
            val interactionSource = interactionSources[i]

            customItem(
                buttonGroupContent = {
                    buttonContent(i, interactionSource)
                },
                menuContent = {}
            )
        }
    }
}

@Composable
fun getConnectedButtonShapes(index: Int, count: Int) = when (index) {
    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
    count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
}