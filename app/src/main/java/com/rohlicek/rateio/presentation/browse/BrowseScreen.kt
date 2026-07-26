package com.rohlicek.rateio.presentation.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.browse.sections.gamesBrowseSection
import com.rohlicek.rateio.presentation.browse.sections.movieBrowseSection
import com.rohlicek.rateio.presentation.browse.sections.showBrowseSection
import com.rohlicek.rateio.presentation.category.CategoryItemListScreen
import com.rohlicek.rateio.presentation.components.ConnectedItemSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


@Composable
fun BrowseScreen(
    onItemClick: (externalId: String, type: CategoryType) -> Unit,
    onTopRatedClick: (category: CategoryType) -> Unit,
    contentPadding: PaddingValues,
    viewModel: BrowseViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(
                top = contentPadding.calculateTopPadding(),
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            ConnectedItemSelector(
                options = state.availableCategories.map { it.name },
                selectedIndex = state.availableCategories.indexOf(state.selectedCategory),
                onSelectionChanged = {
                    viewModel.onCategorySelected(state.availableCategories[it])
                },
            )
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        SearchBarExpandable(
            placeholder = "Search${" " + state.selectedCategory?.name?.lowercase()}...",
            onQueryChange = viewModel::onQueryChange,
        ) { collapse ->
            CategoryItemListScreen(
                title = "",
                category = state.selectedCategory,
                ratingColorOverride = true,
                items = state.results,
                isLoading = state.isLoading,
                onItemClick = { item ->
                    collapse()
                    coroutineScope.launch {
                        delay(165.milliseconds)
                        item.externalId?.let { id ->
                            item.externalSource?.let { type ->
                                onItemClick(id, type)
                            }
                        }
                    }
                },
                emptyContent = {
                    Text(
                        if (state.query.isBlank()) "Search for something"
                        else "No results for \"${state.error}\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = CornerSize(44.dp),
                        topEnd = CornerSize(44.dp),
                        bottomStart = CornerSize(0.dp),
                        bottomEnd = CornerSize(0.dp)
                    )
                )
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (state.selectedCategory?.type) {
                CategoryType.TMDB_SHOWS -> {
                    showBrowseSection(
                        onTopRatedClick = onTopRatedClick,
                        onItemClick = onItemClick,
                    )
                }
                CategoryType.TMDB_MOVIES -> {
                    movieBrowseSection(
                        onTopRatedClick = onTopRatedClick,
                        onItemClick = onItemClick,
                    )
                }
                CategoryType.STEAM_GAMES -> {
                    gamesBrowseSection(
                        onItemClick = onItemClick,
                    )
                }
                else -> {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                            Text(
                                "Nothing here yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(200.dp)) }
        }
    }
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
                trailingIcon = {
                    if (textFieldState.text.isNotEmpty()) {
                        IconButton(onClick = { textFieldState.clearText() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
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
        colors = SearchBarDefaults.colors().copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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