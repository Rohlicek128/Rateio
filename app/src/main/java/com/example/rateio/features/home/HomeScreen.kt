package com.example.rateio.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.Category
import com.example.rateio.presentation.category.CategoryItemListScreen
import com.example.rateio.presentation.components.FloatingIconButton
import com.example.rateio.presentation.components.RateBox
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import com.example.rateio.ui.theme.GoogleSans


@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onItemClick: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val categoryRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        CategoryRepository(db.categoryDao())
    }
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(categoryRepository, itemRepository)
    )
    val state by viewModel.state.collectAsState()


    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
            }
        }
        state.categories.isNotEmpty() -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            "Categories",
                            style = MaterialTheme.typography.displayMedium,
                            lineHeight = 1.1.em,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) }

                itemsIndexed(state.categories.sortedBy { it.category.sortOrder }) { index, (category, icon, count) ->
                    SettingListItem(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 3.dp),
                        title = category.name,
                        titleStyle = MaterialTheme.typography.titleLarge,
                        icon = icon,
                        description = "$count items",
                        position = when {
                            state.categories.size == 1 -> ListItemPosition.SINGLE
                            index == 0 -> ListItemPosition.START
                            index >= state.categories.size - 1 -> ListItemPosition.END
                            state.categories.size > 1 -> ListItemPosition.MIDDLE
                            else -> ListItemPosition.SINGLE
                        },
                        onClick = { onItemClick(category.id) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FloatingIconButton(
                    modifier = Modifier.padding(
                        top = contentPadding.calculateTopPadding() + 12.dp,
                        end = 20.dp,
                    ),
                    icon = Icons.Filled.Settings,
                    onClick = onOpenSettings,
                )
            }
        }
        
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    onItemClick: (Long) -> Unit,
    count: Int? = null,
) {
    val haptic = LocalHapticFeedback.current

    ListItem(
        headlineContent = { Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            text = category.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            lineHeight = 1.em,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        ) },
        trailingContent = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = null,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .widthIn(min = 58.dp)
            ) {
                Text(
                    text = count?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = GoogleSans,
                    maxLines = 1,
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    overflow = TextOverflow.Visible,
                    softWrap = false,
                )
            }
        },
        modifier = Modifier
            .padding(PaddingValues(horizontal = 20.dp, vertical = 6.dp))
            .clip(MaterialTheme.shapes.largeIncreased)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onItemClick(category.id)
            }),
        tonalElevation = 1.dp
    )
}