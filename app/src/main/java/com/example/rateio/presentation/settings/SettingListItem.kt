package com.example.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.components.ItemCard
import kotlin.math.max
import kotlin.math.min


enum class ListItemPosition {
    START,
    MIDDLE,
    END,
    SINGLE,
}

@Composable
fun SettingListItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    titleStyle: TextStyle = MaterialTheme.typography.titleMediumEmphasized,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    position: ListItemPosition = ListItemPosition.SINGLE,
    showNavigateIconOnClick: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current

    val largeCorner = MaterialTheme.shapes.extraLarge
    val smallCorner = MaterialTheme.shapes.medium

    val shape = when (position) {
        ListItemPosition.START -> largeCorner.copy(
            bottomStart = smallCorner.bottomStart,
            bottomEnd = smallCorner.bottomEnd,
        )
        ListItemPosition.END -> largeCorner.copy(
            topStart = smallCorner.topStart,
            topEnd = smallCorner.topEnd,
        )
        ListItemPosition.SINGLE -> largeCorner
        else -> smallCorner
    }

    ItemCard(
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        leadingPadding = if (icon != null) 4.dp else 0.dp,
        onClick = if (onClick != null) {
            {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                onClick()
            }
        } else null,
        headlineContent = {
            Text(
                title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
        },
        subtitleContent = if (description != null) {
            {
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                )
            }
        } else null,
        supportingContent = supportingContent,
        leadingContent = if (icon != null) {
            {
                Icon(icon, null, tint = contentColor)
                /*val colors = getRatingColor(Random.nextFloat() + 0.25f)
                Card(
                    modifier = Modifier.size(IconButtonDefaults.extraLargeIconSize),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors().copy(
                        containerColor = colors.backgroundColor,
                        contentColor = colors.foregroundColor
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            icon, null,
                            tint = colors.foregroundColor
                        )
                    }
                }*/
            }
        } else null,
        trailingContent = {
            if (onClick != null && showNavigateIconOnClick) {
                Icon(
                    modifier = Modifier.padding(end = 4.dp),
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                    tint = contentColor,
                )
            }
            else trailingContent?.invoke()
        },
    )

}


@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = modifier,
        singleLine = singleLine,
        shape = MaterialTheme.shapes.medium,
        value = value,
        colors = OutlinedTextFieldDefaults.colors().copy(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                Icons.Default.TextFields,
                null,
            )
        },
    )
}

@Composable
fun SettingsNumberField(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    asInt: Boolean = false,
    icon: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
) {
    val displayInt = if (value == 0f) "" else value.toInt().toString()

    OutlinedTextField(
        modifier = modifier,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        value = if (asInt) displayInt else value.toString(),
        colors = OutlinedTextFieldDefaults.colors().copy(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedIndicatorColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        onValueChange = { input ->
            if (asInt) {
                if (input.isEmpty()) {
                    onValueChange(0f)
                    return@OutlinedTextField
                }

                val cleanInput = input.filter { it.isDigit() }
                val parsedInt = cleanInput.toIntOrNull()
                if (parsedInt != null) {
                    onValueChange(parsedInt.toFloat())
                }
            }
            else {
                val value = input.toFloatOrNull()
                if (input.isEmpty() || value != null) {
                    onValueChange(value ?: 1f)
                }
            }
        },
        placeholder = placeholder,
        leadingIcon = if (icon) {
            {
                Icon(
                    Icons.Default.Numbers,
                    null,
                )
            }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun SettingsSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onCheckedChange(it)
        },
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
    )
}

@Composable
fun IntCounter(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = Int.MIN_VALUE,
    maxValue: Int = Int.MAX_VALUE,
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                onValueChange(max(minValue, value - 1))
            },
            shapes = IconButtonDefaults.shapes(),
            enabled = value > minValue,
        ) {
            Icon(Icons.Default.Remove, "Subtract")
        }

        SettingsNumberField(
            modifier = Modifier.width(70.dp),
            value = value.toFloat(),
            icon = false,
            asInt = true,
            onValueChange = { value ->
                onValueChange(max(minValue, min(maxValue, value.toInt())))
            },
        )

        FilledTonalIconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                onValueChange(min(maxValue, value + 1))
            },
            shapes = IconButtonDefaults.shapes(),
            enabled = value < maxValue,
        ) {
            Icon(Icons.Default.Add, "Add")
        }
    }
}

@Composable
fun SettingsSelectedEnum(
    modifier: Modifier = Modifier,
    name: String,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsSelectedEnums(
    modifier: Modifier = Modifier,
    names: List<String>,
) {
    FlowRow(
        modifier = modifier,
        //horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        names.forEach {
            Card(
                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}