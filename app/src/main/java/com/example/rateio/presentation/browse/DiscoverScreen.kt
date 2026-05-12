package com.example.rateio.presentation.browse

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.category.CategoryItemListScreen


@Composable
fun DiscoverScreen(
    category: Category,
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DiscoverViewModel = viewModel(
        key = category.type.name,
        factory = DiscoverViewModel.factory(category),
    )
    val state by viewModel.state.collectAsState()

    CategoryItemListScreen(
        modifier = modifier,
        title = "Popular",
        items = state.results,
        isLoading = state.isLoading,
        showRanking = true,
        onItemClick = { item ->
            item.externalId?.let { id ->
                item.externalSource?.let { type ->
                    onItemClick(id, type)
                }
            }
        },
        emptyContent = {
            Text(
                "Nothing to discover :(",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}