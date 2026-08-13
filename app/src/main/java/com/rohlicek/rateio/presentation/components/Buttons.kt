package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeGroup
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.presentation.settings.SettingsSelectedEnum
import com.rohlicek.rateio.utils.openExternalLink


@Composable
fun SortByButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onClick()
        },
        modifier = modifier,
        interactionSource = interactionSource,
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Sort,
            contentDescription = "Sort by",
            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            "Sort by",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
fun GroupByButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onClick()
        },
        modifier = modifier,
        interactionSource = interactionSource,
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(
            Icons.Default.Groups,
            contentDescription = "Group by",
            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            "Group by",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}


@Composable
fun SaveButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Button(
        modifier = modifier,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            onClick()
        },
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSource,
        enabled = enabled,
    ) {
        Icon(
            Icons.Default.Save,
            contentDescription = "Save",
            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            "Save",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
fun OpenButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    label: String,
    onClickUrl: String,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    FilledTonalButton(
        modifier = modifier,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            openExternalLink(
                context,
                url = onClickUrl
            )
        },
        shapes = ButtonDefaults.shapes(),
        interactionSource = interactionSource,
        enabled = enabled,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Go to $label",
            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Visible,
        )
    }
}


@Composable
fun FloatingIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    FilledTonalIconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        modifier = modifier.zIndex(1f),
        shapes = IconButtonDefaults.shapes()
    ) {
        Icon(
            icon,
            contentDescription = null,
        )
    }
}


@Composable
fun OrderButton(
    modifier: Modifier = Modifier,
    title: String? = null,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onClick()
        },
        modifier = modifier,
        shapes = ButtonDefaults.shapes(),
    ) {
        Icon(
            Icons.Default.Tv,
            contentDescription = title ?: "Order",
            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
        Text(
            "Order",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
fun ModalEpisodeGroupsSelector(
    modifier: Modifier = Modifier,
    episodeGroups: List<TmdbEpisodeGroup>,
    selectedGroupId: String?,
    onSelectGroupId: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val defaultGroup = remember {
        val type = EpisodeGroupTypes.ORIGINAL_AIR_DATE
        TmdbEpisodeGroup(
            id = "",
            name = type.displayName,
            description = "Ordered by the original air dates from TMDB.",
            type = 1,
            seasonCount = null,
            episodeCount = null,
            network = null,
        )
    }

    ModalSelector(
        modifier = modifier,
        title = "Episode Orders",
        maxHeightFraction = 0.8f,
        skipPartiallyExpanded = false,
        onDismiss = onDismiss,
    ) {
        item {
            GroupListItem(
                group = defaultGroup,
                selected = selectedGroupId == null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onSelectGroupId(null)
                    onDismiss()
                }
            )
        }

        items(episodeGroups, key = { it.id }) { group ->
            GroupListItem(
                group = group,
                selected = group.id == selectedGroupId,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onSelectGroupId(group.id)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun GroupListItem(
    group: TmdbEpisodeGroup,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
) {
    ItemCard(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick),
        contentPadding = paddingValues,
        tonalElevation = if (selected) ListItemDefaults.Elevation else 1.dp,
        colors = if (selected) {
            ListItemDefaults.colors().copy(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                supportingContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                trailingContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
        } else ListItemDefaults.colors(),
        headlineContent = {
            Text(
                text = group.name ?: "???",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        subtitleContent = {
            if (!group.description.isNullOrBlank()) {
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailingContent = {
            Text(
                text = "${group.seasonCount ?: "?"} / ${group.episodeCount ?: "?"}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        },
        supportingContent = if (group.type != null) {
            {
                val groupType = EpisodeGroupTypes.fromId(group.type)
                if (groupType != null) {
                    SettingsSelectedEnum(
                        modifier = Modifier.padding(top = 6.dp),
                        name = groupType.displayName
                    )
                }
            }
        } else null,
    )
}


enum class EpisodeGroupTypes(val id: Int, override val displayName: String) : HasDisplayName {
    ORIGINAL_AIR_DATE(1, "Original air date"),
    ABSOLUTE(2, "Absolute"),
    DVD(3, "DVD"),
    DIGITAL(4, "Digital"),
    STORY_ARC(5, "Story arc"),
    PRODUCTION(6, "Production"),
    TV(7, "TV");

    companion object {
        private val map = entries.associateBy { it.id }
        fun fromId(id: Int): EpisodeGroupTypes? = map[id]
        fun fromIdOrDefault(id: Int, default: EpisodeGroupTypes = ORIGINAL_AIR_DATE): EpisodeGroupTypes {
            return map[id] ?: default
        }
    }
}