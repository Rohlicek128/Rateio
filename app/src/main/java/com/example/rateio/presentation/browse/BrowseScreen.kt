package com.example.rateio.presentation.browse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.presentation.components.RateItemCard
import kotlinx.coroutines.launch


@Composable
fun BrowseScreen(
    contentPadding: PaddingValues,
    onShowClick: (Int) -> Unit,
    viewModel: BrowseViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {

        SearchBar(
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
        )

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularWavyProgressIndicator()
                }
            }
            state.error != null -> {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            state.results.isEmpty() && state.query.isNotBlank() -> {
                Text("No results", modifier = Modifier.padding(16.dp))
            }
            else -> {
                LazyColumn {
                    items(state.results, key = { it.id }) { show ->
                        var ratingTest: Float? = null
                        if (show.voteAverage != null) {
                            if (show.voteAverage > 0f) ratingTest = show.voteAverage.div(10f).plus(0.1f)
                        }
                        RateItemCard(
                            title = show.name,
                            subtitle = "${show.firstAirDate?.take(4)}  ·  ${if (show.originCountry.isNotEmpty()) show.originCountry[0] else "N/A"}",
                            coverImagePath = "https://image.tmdb.org/t/p/w185${show.posterPath}",
                            rating = ratingTest,
                            onClick = { onShowClick(show.id) }
                        )
                    }

                    /*items(state.results, key = { it.id }) { movie ->
                        var ratingTest: Float? = null
                        if (movie.voteAverage != null) {
                            if (movie.voteAverage > 0f) ratingTest = movie.voteAverage.div(10f)
                        }
                        RateItemCard(
                            title = movie.title,
                            subtitle = "${movie.releaseDate?.take(4)}  ·  ${movie.originalLanguage?.uppercase() ?: "N/A"}",
                            coverImagePath = "https://image.tmdb.org/t/p/w185${movie.posterPath}",
                            rating = ratingTest,
                            onClick = { onShowClick(movie.id) }
                        )
                    }*/
                }
            }
        }
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