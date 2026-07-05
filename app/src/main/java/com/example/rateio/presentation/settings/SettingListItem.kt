package com.example.rateio.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.rating.display.getRatingColor
import kotlin.random.Random


enum class ListItemPosition {
    START,
    MIDDLE,
    END,
    SINGLE,
}

/*@Composable
fun SettingListItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    position: ListItemPosition = ListItemPosition.SINGLE,
    showNavigateIconOnClick: Boolean = true,
    //test: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current

    val largeCorner = MaterialTheme.shapes.extraLarge
    val smallCorner = MaterialTheme.shapes.medium

    ListItem(
        leadingContent = if (icon != null) {
            {
                Icon(icon, null)
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
        }
        else {
            null
        },
        headlineContent = {
            Text(
                title,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Column {
                if (description != null) {
                    Text(description)
                }
                supportingContent?.invoke()
            }
        },
        trailingContent = if (onClick != null && showNavigateIconOnClick) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                )
            }
        } else trailingContent,
        tonalElevation = 1.dp,
        colors = colors,
        modifier = modifier
            .clip(when (position) {
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
            })
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        onClick()
                    })
                }
                else Modifier
            ),
    )
}*/

@Composable
fun SettingListItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
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

    Surface(
        modifier = modifier
            .heightIn(min = 70.dp)
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        onClick()
                    })
                }
                else Modifier
            ),
        shape = shape,
        color = containerColor,
        tonalElevation = 1.dp,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, null, tint = contentColor)
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                        )
                        if (description != null) {
                            Text(
                                description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtitleColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (onClick != null && showNavigateIconOnClick) {
                        Icon(
                            modifier = Modifier.padding(end = 4.dp),
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = null,
                            tint = contentColor,
                        )
                    }
                    else trailingContent?.invoke()
                }

                supportingContent?.invoke()
            }
        }

    }
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
    placeholder: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = modifier,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        value = value.toString(),
        colors = OutlinedTextFieldDefaults.colors().copy(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        onValueChange = {
            val value = it.toFloatOrNull()
            if (it.isEmpty() || value != null) {
                onValueChange(value ?: 1f)
            }
        },
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                Icons.Default.Numbers,
                null,
            )
        },
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