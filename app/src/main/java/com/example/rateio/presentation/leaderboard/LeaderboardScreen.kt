package com.example.rateio.presentation.leaderboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.category.CategoryItemListScreen


@Composable
fun LeaderboardScreen(
    contentPadding: PaddingValues,
    onItemClick: (RateItem) -> Unit,
) {
    val type = CategoryType.TMDB_EPISODES

    val context = LocalContext.current
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }
    val viewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModel.factory(type, itemRepository)
    )
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
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
            else -> {
                CategoryItemListScreen(
                    title = "",
                    items = state.items
                        .filter { it.rating != null }
                        .sortedWith(compareBy({ -(it.rating ?: -1f) }, { it.title })),
                    placeholderRatio = if (type == CategoryType.TMDB_EPISODES) 16f / 9f else 2f / 3f,
                    isLoading = false,
                    onItemClick = onItemClick,
                    showRanking = true,
                )
            }
        }
    }

}