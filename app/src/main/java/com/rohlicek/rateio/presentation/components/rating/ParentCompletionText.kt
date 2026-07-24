package com.rohlicek.rateio.presentation.components.rating

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em


@Composable
fun ParentCompletionText(
    numberOfCompleted: Int,
    numberOfAll: Int,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(end = 6.dp)
) {
    val show = true
    val showNoCompletion = false
    val showNull = false

    if (show && (showNoCompletion || numberOfCompleted > 0) && (showNull || numberOfAll > 0)) {
        Text(
            modifier = modifier.padding(padding),
            text = "${numberOfCompleted}/${numberOfAll}",
            style = MaterialTheme.typography.titleMedium,
            //color = if (flatRatings.size >= season.episodeCount) MaterialTheme.colorScheme.secondary else Color.Unspecified,
            fontWeight = if (numberOfCompleted >= numberOfAll) FontWeight.ExtraBold else null,
            lineHeight = 1.em,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}