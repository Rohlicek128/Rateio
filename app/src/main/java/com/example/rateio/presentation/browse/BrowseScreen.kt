package com.example.rateio.presentation.browse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.category.CategoryItemListScreen


@Composable
fun BrowseScreen(
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
    contentPadding: PaddingValues,
    viewModel: BrowseViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()

    LaunchedEffect(textFieldState.text) {
        viewModel.onQueryChange(textFieldState.text.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ScrollableTabRow(
            selectedTabIndex = state.availableCategories.indexOf(state.selectedCategory),
        ) {
            state.availableCategories.forEach { category ->
                Tab(
                    selected = state.selectedCategory == category,
                    onClick = { viewModel.onCategorySelected(category) },
                    text = { Text(category.name) },
                )
            }
        }

        SearchBar(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
        )

        CategoryItemListScreen(
            title = "",
            items = state.results,
            isLoading = state.isLoading,
            onItemClick = { item ->
                item.externalId?.let { id ->
                    item.externalSource?.let { type ->
                        onItemClick(id, type)
                    }
                }
            },
            itemTrailingContent = { item ->
                // Show a checkmark if already in the user's library
                Icon(
                    Icons.Default.Add,
                    contentDescription = "View",
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            emptyContent = {
                Text(
                    if (state.query.isBlank()) "Search for something"
                    else "No results for \"${state.query}\"",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search movies...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
    )
}

@Composable
private fun SearchBar2(onQueryChange: (String) -> Unit) {
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { onQueryChange(textFieldState.text.toString()) },
                placeholder = { Text("Search shows...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            )
        }

    LaunchedEffect(textFieldState.text) {
        onQueryChange(textFieldState.text.toString())
    }
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        state = searchBarState,
        inputField = inputField
    )
    ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {

    }
}