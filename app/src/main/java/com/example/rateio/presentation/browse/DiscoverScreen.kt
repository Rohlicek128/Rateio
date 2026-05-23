package com.example.rateio.presentation.browse

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.category.ItemListRow


@Composable
fun DiscoverScreen(
    category: Category,
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    sortBy: String = "popularity.desc",
    showNullRatings: Boolean = false,
) {
    val viewModel: DiscoverViewModel = viewModel(
        key = category.type.name + sortBy,
        factory = DiscoverViewModel.factory(category, sortBy),
    )
    val state by viewModel.state.collectAsState()

    ItemListRow(
        modifier = modifier,
        title = title,
        items = state.results,
        isLoading = state.isLoading,
        showRanking = true,
        showNullRatings = showNullRatings,
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