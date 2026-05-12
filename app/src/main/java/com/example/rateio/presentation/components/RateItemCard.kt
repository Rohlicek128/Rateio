package com.example.rateio.presentation.components

import android.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
    modifier: Modifier = Modifier,
    placeholderRatio: Float = 2f / 3f,
    padding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
    isLoading: Boolean = false,
    rank: Int? = null,
    rankWidth: Dp = 36.dp,
    bubbleText: String? = null,
) {
    val haptic = LocalHapticFeedback.current
    val offset = (-6).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(padding),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(visible = rank != null) {
            Text(
                text = "${rank}.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(rankWidth).offset(y = 12.dp),
                textAlign = TextAlign.End,
            )
        }


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
                    Box (
                        modifier = Modifier.offset(x = offset),
                    ) {
                        Card(
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

                        if (bubbleText != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                border = null,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .align(Alignment.BottomStart),
                            ) {
                                Text(
                                    bubbleText,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                )
                            }
                        }

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
                    //decimalOffset = if (rank != null) 1u else 0u
                )
            },
            modifier = Modifier
                .clip(MaterialTheme.shapes.largeIncreased)
                .clickable(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                }),
            tonalElevation = 1.dp
        )
    }
}

@Composable
fun IndexedItem(
    index: Int,
    modifier: Modifier = Modifier,
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