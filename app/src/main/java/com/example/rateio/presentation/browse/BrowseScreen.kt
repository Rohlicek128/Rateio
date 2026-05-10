package com.example.rateio.presentation.browse

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.SecondaryScrollableTabRow
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
import com.example.rateio.presentation.components.DisplaySelector
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun BrowseScreen(
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
    contentPadding: PaddingValues,
    viewModel: BrowseViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    //LaunchedEffect(textFieldState.text) {
    //    viewModel.onQueryChange(textFieldState.text.toString())
    //}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            DisplaySelector(
                options = state.availableCategories.map { it.name },
                selectedIndex = state.availableCategories.indexOf(state.selectedCategory),
                onSelectionChanged = {
                    viewModel.onCategorySelected(state.availableCategories[it])
                },
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        SearchBarExpandable(
            //query = state.query,
            placeholder = "Search${" " + state.selectedCategory?.name?.lowercase()}...",
            onQueryChange = viewModel::onQueryChange,
        ) { collapse ->
            CategoryItemListScreen(
                title = "",
                items = state.results,
                isLoading = state.isLoading,
                onItemClick = { item ->
                    collapse()
                    coroutineScope.launch {
                        delay(165)
                        item.externalId?.let { id ->
                            item.externalSource?.let { type ->
                                onItemClick(id, type)
                            }
                        }
                    }
                },
                itemTrailingContent = { item ->
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
}

@Composable
private fun SearchBar(query: String, categoryName: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search ${categoryName}...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
    )
}

@Composable
private fun SearchBarExpandable(
    placeholder: String = "Search...",
    onQueryChange: (String) -> Unit,
    content: @Composable (collapse: () -> Unit) -> Unit,
) {
    val searchBarState = rememberSearchBarState()
    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { onQueryChange(textFieldState.text.toString()) },
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            )
        }

    val collapse: () -> Unit = {
        scope.launch { searchBarState.animateToCollapsed() }
    }


    LaunchedEffect(textFieldState.text) {
        onQueryChange(textFieldState.text.toString())
    }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 4.dp),
        state = searchBarState,
        inputField = inputField,
        tonalElevation = 0.dp,
    )
    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        tonalElevation = 0.dp,
    ) {
        content(collapse)
    }
}