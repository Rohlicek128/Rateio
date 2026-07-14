package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow


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
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
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