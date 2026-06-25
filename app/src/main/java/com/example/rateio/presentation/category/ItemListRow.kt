package com.example.rateio.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.RateItemGridCard
import com.example.rateio.presentation.components.SectionHeader

@Composable
fun ItemListRow(
    title: String,
    items: List<RateItem>,
    isLoading: Boolean,
    onItemClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    showNullRatings: Boolean = true,
    showRanking: Boolean = false,
    placeholderRatio: Float = 2f / 3f,
    itemTrailingContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
) {
    Column {
        if (title.isNotBlank()) {
            SectionHeader(title)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularWavyProgressIndicator()
            }
            items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                emptyContent?.invoke() ?: Text(
                    "Nothing here yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> LazyRow (
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    RateItemGridCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        coverImagePath = item.coverImageLowUrl,
                        rating = item.rating,
                        //placeholderRatio = if (item.externalSource == CategoryType.TMDB_EPISODES) 16f / 9f else 2f / 3f,
                        padding = PaddingValues(0.dp),
                        rank = if (showRanking) index + 1 else null,
                        onClick = { onItemClick(item) },
                        leadingRateBoxContent = itemTrailingContent,
                        showNullRatings = showNullRatings,
                    )
                }
            }
        }
    }
}