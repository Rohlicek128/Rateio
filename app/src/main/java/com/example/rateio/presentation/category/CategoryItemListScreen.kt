package com.example.rateio.presentation.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.SectionHeader


@Composable
fun CategoryItemListScreen(
    title: String,
    items: List<RateItem>,
    isLoading: Boolean,
    onItemClick: (RateItem) -> Unit,
    modifier: Modifier = Modifier,
    showRanking: Boolean = false,
    itemTrailingContent: (@Composable (RateItem) -> Unit)? = null,
    headerContent: (@Composable () -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
) {
    var displayCover by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        if (title.isNotBlank()) {
            SectionHeader(title)
        }

        headerContent?.invoke()

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
            else -> LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        OutlinedToggleButton(
                            checked = displayCover,
                            onCheckedChange = { displayCover = it },
                            shapes = ToggleButtonDefaults.shapes(),
                        ) {
                            Text(
                                "Cover Images",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                itemsIndexed(items) { index, item ->
                    RateItemCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        coverImagePath = if (displayCover) item.coverImageLowUrl else null,
                        rating = item.rating,
                        placeholderRatio = 2f / 3f,
                        padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        rank = if (showRanking) index + 1 else null,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}