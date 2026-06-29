package com.example.rateio.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.rating.display.getRatingColor
import kotlin.random.Random


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
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    position: ListItemPosition = ListItemPosition.MIDDLE,
    test: Boolean = false,
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
        trailingContent = if (onClick != null) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                )
            }
        } else trailingContent,
        tonalElevation = 1.dp,
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
}