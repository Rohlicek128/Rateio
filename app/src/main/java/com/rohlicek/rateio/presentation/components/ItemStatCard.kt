package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.RatingTransformation
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.rohlicek.rateio.utils.formatCompact
import com.rohlicek.rateio.utils.openExternalLink


@Composable
fun ItemStatCard(
    header: String,
    statistic: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            statistic,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            header,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ItemRatingStatCard(
    rating: Float?,
    votes: Int?,
    source: String,
    modifier: Modifier = Modifier,
    onClickUrl: String? = null,
    colorBucketsOverride: RatingColorBuckets = getCurrentRatingColorBuckets(),
    transformationOverride: RatingTransformation = getCurrentRatingTransformations(),
    showNullVotes: Boolean = true,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val correctedRating = if (votes != null && votes <= 0) null else rating
    val correctedVotes = if (votes != null && votes <= 0) null else votes

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClickUrl != null) {
                    Modifier.clickable(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        openExternalLink(
                            context,
                            url = onClickUrl
                        )
                    })
                }
                else Modifier
            ),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .height(85.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = source,
                style = MaterialTheme.typography.headlineSmall,
                //color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RateBox(
                    rating = correctedRating,
                    colorBucketsOverride = colorBucketsOverride,
                    transformationOverride = transformationOverride,
                )
                if (showNullVotes || correctedVotes != null)
                Text(
                    if (correctedVotes != null) formatCompact(correctedVotes.toLong()) else "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                )
            }

        }
    }
}