package com.example.rateio.presentation.rating

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository


@Composable
fun SavedRateItemScreen(
    itemId: Long,
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }
    val categoryRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        CategoryRepository(db.categoryDao())
    }
    val viewModel: SavedRateItemViewModel = viewModel(
        factory = SavedRateItemViewModel.factory(itemId, itemRepository, categoryRepository)
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
        state.item != null -> {
            val item = state.item!!
            val category = state.category

            RateItemDetailScreen(
                title = item.title,
                subtitle = item.subtitle,
                categoryName = category?.name,
                description = "${item.externalId}, ${item.externalSource}, ${item.updatedAt}, ${item.createdAt}",
                coverImageUrl = item.coverImageUrl,
                backdropImageUrl = null,
                placeholderRatio = 2f / 3f,
                rating = item.rating,
                onBackClick = onBackClick,
                extraContent = { },
                onRatingSaved = { rating ->
                    viewModel.saveRating(rating)
                }
            )

        }
    }

}