package com.example.rateio.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rateio.data.remote.TmdbShow


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
                        ShowCard(show = show, onClick = { onShowClick(show.id) })
                    }
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
        placeholder = { Text("Search shows...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
    )
}

@Composable
private fun ShowCard(show: TmdbShow, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(show.name, style = MaterialTheme.typography.titleMedium) },
        supportingContent = {
            show.firstAirDate?.take(4)?.let { Text(it) }
        },
        leadingContent = {
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(show.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" })
                        .crossfade(true)
                        .build(),
                    contentDescription = "Poster",
                    modifier = Modifier.height(120.dp)
                )
            }
        },
        trailingContent = {
            show.voteAverage?.let {
                Text("%.1f".format(it), style = MaterialTheme.typography.labelLarge)
            }
        },
        modifier = Modifier
            .padding(PaddingValues(horizontal = 16.dp, vertical = 4.dp))
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        tonalElevation = 1.dp
    )
}