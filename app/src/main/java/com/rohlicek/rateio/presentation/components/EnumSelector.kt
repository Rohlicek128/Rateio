package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.model.HasDisplayName
import com.rohlicek.rateio.model.ItemStatus


enum class SortOrder(override val displayName: String) : HasDisplayName {
    ASCENDING("Ascending"),
    DESCENDING("Descending"),
}


@Composable
fun ModalSelector(
    modifier: Modifier = Modifier,
    title: String,
    onDismiss: () -> Unit,
    maxHeightFraction: Float = 0.55f,
    skipPartiallyExpanded: Boolean = false,
    headerContent: (@Composable (ColumnScope.() -> Unit))? = null,
    footerContent: (LazyListScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(maxHeightFraction)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MajorSectionHeader(title)

            if (headerContent != null) {
                headerContent()
                Spacer(modifier = Modifier.height(12.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large.copy(
                        bottomStart = MaterialTheme.shapes.extraLarge.bottomStart,
                        bottomEnd = MaterialTheme.shapes.extraLarge.bottomEnd,
                    )),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                content()
                if (footerContent != null) {
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    footerContent()
                }
                item { Spacer(modifier = Modifier.height(50.dp)) }
            }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> ModalEnumMultiSelector(
    modifier: Modifier = Modifier,
    title: String,
    selectedOptions: List<T>,
    crossinline onOptionSelected: (T) -> Unit,
    noinline onDismiss: () -> Unit,
    separatedOptions: List<T> = emptyList(),
    onClickDismiss: Boolean = false,
    maxHeightFraction: Float = 0.55f,
    skipPartiallyExpanded: Boolean = false,
    noinline headerContent: (@Composable (ColumnScope.() -> Unit))? = null,
    noinline footerContent: (LazyListScope.() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    val options = enumValues<T>()

    ModalSelector(
        modifier = modifier,
        title = title,
        onDismiss = onDismiss,
        maxHeightFraction = maxHeightFraction,
        skipPartiallyExpanded = skipPartiallyExpanded,
        headerContent = headerContent,
        footerContent = footerContent,
    ) {
        items(options.filter { it !in separatedOptions }, key = { it }) { option ->
            EnumListItem(
                option = option,
                selected = option in selectedOptions,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onOptionSelected(option)
                    if (onClickDismiss) onDismiss()
                },
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        items(options.filter { it in separatedOptions }, key = { it }) { option ->
            EnumListItem(
                option = option,
                selected = option in selectedOptions,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onOptionSelected(option)
                    if (onClickDismiss) onDismiss()
                },
                paddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> ModalEnumSelector(
    modifier: Modifier = Modifier,
    title: String,
    selectedOption: T,
    crossinline onOptionSelected: (T) -> Unit,
    noinline onDismiss: () -> Unit,
    separatedOptions: List<T> = emptyList(),
    maxHeightFraction: Float = 0.55f,
    skipPartiallyExpanded: Boolean = false,
    noinline headerContent: (@Composable (ColumnScope.() -> Unit))? = null,
    noinline footerContent: (LazyListScope.() -> Unit)? = null,
) {
    ModalEnumMultiSelector(
        modifier = modifier,
        title = title,
        selectedOptions = listOf(selectedOption),
        onOptionSelected = onOptionSelected,
        onDismiss = onDismiss,
        separatedOptions = separatedOptions,
        maxHeightFraction = maxHeightFraction,
        skipPartiallyExpanded = skipPartiallyExpanded,
        headerContent = headerContent,
        footerContent = footerContent,
        onClickDismiss = true,
    )
}


@Composable
inline fun <reified T : Enum<T>> EnumListItem(
    option: T,
    selected: Boolean,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
) {
    ListItem(
        headlineContent = {
            Text(
                modifier = Modifier.padding(paddingValues),
                text = (option as? HasDisplayName)?.displayName ?: option.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        },
        colors = if (selected) {
            ListItemDefaults.colors().copy(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else ListItemDefaults.colors(),
        tonalElevation = if (selected) ListItemDefaults.Elevation else 1.dp,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
    )
}



@Composable
inline fun <reified T : Enum<T>> ModalSortableEnumSelector(
    modifier: Modifier = Modifier,
    title: String = "Sort By",
    selectedOption: T,
    crossinline onOptionSelected: (T) -> Unit,
    selectedOrder: SortOrder,
    crossinline onOrderChange: (SortOrder) -> Unit,
    noinline onDismiss: () -> Unit,
    separatedOptions: List<T> = emptyList(),
    noinline footerContent: (LazyListScope.() -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    ModalEnumSelector(
        modifier = modifier,
        title = title,
        selectedOption = selectedOption,
        onOptionSelected = onOptionSelected,
        onDismiss = onDismiss,
        separatedOptions = separatedOptions,
        maxHeightFraction = 0.7f,
        skipPartiallyExpanded = true,
        headerContent = {
            OrderListItem(
                order = selectedOrder,
                onChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    onOrderChange(it)
                }
            )
        },
        footerContent = footerContent,
    )
}

@Composable
fun OrderListItem(
    modifier: Modifier = Modifier,
    order: SortOrder,
    onChange: (SortOrder) -> Unit,
) {
    val alpha = 0.65f

    ListItem(
        headlineContent = {
            Text(
                modifier = Modifier.padding(bottom = 14.dp),
                text = order.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        overlineContent = {
            Text(
                modifier = Modifier.padding(top = 14.dp),
                text = "Order",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        leadingContent = {
            Card(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (order == SortOrder.ASCENDING) Icons.Default.ArrowUpward
                        else Icons.Default.ArrowDownward,
                        null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        //modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            }
        },
        colors = ListItemDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
            contentColor = MaterialTheme.colorScheme.onSecondary,
            overlineContentColor = MaterialTheme.colorScheme.onSecondary,
            supportingContentColor = MaterialTheme.colorScheme.onSecondary,
        ),
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                width = 6.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.extraLarge,
            )
            .clickable(onClick = {
                onChange(when (order) {
                    SortOrder.ASCENDING -> SortOrder.DESCENDING
                    SortOrder.DESCENDING -> SortOrder.ASCENDING
                })
            })
    )
}


@Composable
inline fun <reified T : Enum<T>> RowButtonEnumSelector(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    selectedOption: T?,
    excludedOptions: List<T> = emptyList(),
    crossinline onOptionSelected: (T?) -> Unit,
    nullIsAll: Boolean = false,
) {
    RowButtonEnumMultiSelector(
        modifier = modifier,
        contentPadding = contentPadding,
        selectedOptions = selectedOption?.let { listOf(it) } ?: emptyList(),
        excludedOptions = excludedOptions,
        onOptionSelected = onOptionSelected,
        emptyIsAll = nullIsAll,
    )
}

@Composable
inline fun <reified T : Enum<T>> RowButtonEnumMultiSelector(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    selectedOptions: List<T>,
    excludedOptions: List<T> = emptyList(),
    crossinline onOptionSelected: (T?) -> Unit,
    emptyIsAll: Boolean = false,
) {
    val options = enumValues<T>()
    val interactionSources = remember { List(options.size + (if (emptyIsAll) 1 else 0)) { MutableInteractionSource() } }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        ButtonGroup(
            modifier = Modifier.padding(contentPadding),
            overflowIndicator = {},
            expandedRatio = 0.2f,
        ) {
            if (emptyIsAll) {
                customItem(
                    buttonGroupContent = {
                        RowToggleButton(
                            label = "All",
                            selected = selectedOptions.isEmpty(),
                            onSelect = { onOptionSelected(null) },
                            interactionSource = interactionSources[0],
                        )
                    },
                    menuContent = {},
                )
            }

            options.filter { it !in excludedOptions }.forEachIndexed { index, option ->
                customItem(
                    buttonGroupContent = {
                        RowToggleButton(
                            label = (option as? HasDisplayName)?.displayName ?: option.name,
                            selected = option in selectedOptions,
                            onSelect = { onOptionSelected(option) },
                            interactionSource = interactionSources[index + (if (emptyIsAll) 1 else 0)],
                        )
                    },
                    menuContent = {},
                )
            }
        }
    }
}

@Composable
fun ButtonGroupScope.RowToggleButton(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    val haptic = LocalHapticFeedback.current

    ToggleButton(
        checked = selected,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onSelect()
        },
        shapes = ToggleButtonDefaults.shapes(),
        contentPadding = ButtonDefaults.SmallContentPadding,
        interactionSource = interactionSource,
        colors = ToggleButtonDefaults.toggleButtonColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        ),
        modifier = Modifier.animateWidth(interactionSource = interactionSource),
    ) {
        Text(
            text = label,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.Center,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
        )
    }
}