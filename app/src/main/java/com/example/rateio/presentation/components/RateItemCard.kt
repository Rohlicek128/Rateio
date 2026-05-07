package com.example.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.view.HapticFeedbackConstantsCompat


@Composable
fun RateItemCard(
    title: String,
    subtitle: String?,
    coverImagePath: String?,
    rating: Float?,
    onClick: () -> Unit,
    placeholderRatio: Float = 2f / 3f,
    padding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    isLoading: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val offset = (-6).dp

    ListItem(
        headlineContent = { Text(
            title,
            modifier = Modifier.offset(x = offset),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            lineHeight = 1.em,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        ) },
        supportingContent = {
            subtitle?.let { Text(
                it,
                modifier = Modifier.offset(x = offset),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            ) }
        },
        leadingContent = {
            if (coverImagePath != null) {
                Card(
                    modifier = Modifier.offset(x = offset),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    AdaptiveAsyncImage(
                        model = coverImagePath,
                        contentDescription = "Cover image",
                        maxWidth = 120.dp,
                        maxHeight = 120.dp,
                        placeholderRatio = placeholderRatio
                    )
                }
            }
        },
        trailingContent = {
            RateBox(
                rating = rating,
                roundedCorners = 12.dp,
                width = 12.dp,
                minWidth = 36.dp,
                height = 4.dp,
                textStyle = MaterialTheme.typography.headlineSmall,
                isLoading = isLoading,
            )
        },
        modifier = Modifier
            .padding(padding)
            .clip(MaterialTheme.shapes.largeIncreased)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onClick()
            }),
        tonalElevation = 1.dp
    )
}

@Composable
fun IndexedItem(
    modifier: Modifier,
    index: Int,
    item: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$index.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.padding(horizontal = 4.dp))

        item()
    }
}