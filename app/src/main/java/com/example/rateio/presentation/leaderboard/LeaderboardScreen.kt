package com.example.rateio.presentation.leaderboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Text
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.category.CategoryItemListScreen
import com.example.rateio.presentation.components.ModalEnumMultiSelector
import com.example.rateio.presentation.components.ModalEnumSelector
import com.example.rateio.presentation.components.ScreenError
import com.example.rateio.presentation.components.ScreenLoading
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import com.example.rateio.presentation.settings.SettingsSelectedEnum
import com.example.rateio.presentation.settings.SettingsSelectedEnums


@Composable
fun LeaderboardScreen(
    contentPadding: PaddingValues,
    onItemClick: (RateItem) -> Unit,
) {
    val context = LocalContext.current
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }
    val viewModel: LeaderboardViewModel = viewModel(
        factory = LeaderboardViewModel.factory(itemRepository)
    )
    val state by viewModel.state.collectAsState()

    var showEnumModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        when {
            state.isLoading -> {
                ScreenLoading()
            }
            state.error != null -> {
                ScreenError(state.error)
            }
            else -> {
                CategoryItemListScreen(
                    title = "",
                    items = state.items
                        .filter { it.rating != null }
                        .sortedWith(compareBy({ -(it.rating ?: -1f) }, { it.title })),
                    placeholderRatio = if (CategoryType.TMDB_EPISODES in state.types || CategoryType.OPEN_LIBRARY_CHAPTER in state.types)
                        16f / 9f else 2f / 3f,
                    isLoading = false,
                    onItemClick = onItemClick,
                    showRanking = true,
                    headerContent = {
                        SettingListItem(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            title = "Category",
                            description = "Select category to be filtered",
                            icon = Icons.Default.Category,
                            position = ListItemPosition.SINGLE,
                            supportingContent = {
                                SettingsSelectedEnums(
                                    modifier = Modifier.padding(top = 8.dp),
                                    names = state.types.map { it.displayName },
                                )
                            },
                            showNavigateIconOnClick = false,
                            onClick = { showEnumModal = true }
                        )
                        if (showEnumModal) {
                            ModalEnumMultiSelector(
                                title = "Category",
                                selectedOptions = state.types,
                                onOptionSelected = {
                                    viewModel.updateType(it)
                                },
                                separatedOptions = listOf(CategoryType.CUSTOM),
                                onDismiss = { showEnumModal = false },
                            )
                        }
                    }
                )
            }
        }
    }

}