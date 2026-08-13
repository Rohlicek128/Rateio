package com.rohlicek.rateio.presentation.settings

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.db.ImdbRatingEntity
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.preferences.SyncPreferences
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants


@Composable
fun SettingsDatabaseScreen(
    syncRunning: Boolean,
    syncProgress: Int?,
    onSyncRequest: () -> Unit,
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
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: SettingsDatabaseViewModel = viewModel(
        factory = SettingsDatabaseViewModel.factory( categoryRepository, itemRepository)
    )


    val haptic = LocalHapticFeedback.current

    val preferences = remember { SyncPreferences(context) }

    // 1. Observe Last Sync Time
    val lastSyncTimestamp by preferences.lastSyncTime.collectAsState(initial = null)


    var rating by remember(lastSyncTimestamp) { mutableStateOf<ImdbRatingEntity?>(null) }
    LaunchedEffect(key1 = lastSyncTimestamp) {
        rating = imdbRepository.getRatingByImdbId("tt12042730")
    }

    ScreenScaffold(
        title = "Database",
        onBackClick = onBackClick,
    ) { padding, listState ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            state = listState,
        ) {

            item { SettingsListHeader("IMDb") }
            item {
                SettingListItem(
                    title = "Testing Query",
                    description = "Project Hail Mary (tt12042730), ${rating?.averageRating}, ${rating?.numVotes}",
                    //icon = Icons.Default.Palette,
                    position = ListItemPosition.START,
                    trailingContent = {
                        RateBox(
                            rating = rating?.averageRating,
                            colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_MOVIES,
                        )
                    }
                )
            }
            item {
                SettingListItem(
                    title = "IMDb Ratings Database",
                    description = lastSyncTimestamp?.let {
                        "Last Updated: " + DateUtils.formatDateTime(
                            context, it,
                            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
                        )
                    } ?: "Never Synced",
                    //icon = Icons.Default.Palette,
                    position = ListItemPosition.END,
                    supportingContent = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (syncRunning) {
                                Text("Downloading and processing ratings...")
                                Spacer(modifier = Modifier.height(8.dp))
                                if (syncProgress != null) {
                                    LinearProgressIndicator(
                                        progress = { syncProgress / 100f },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text("$syncProgress%")
                                }
                                else {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                        onSyncRequest()
                                    }
                                ) {
                                    Text("Sync IMDb Ratings Now (Take ~20s)")
                                }
                            }
                        }

                    }
                )
            }

        }
    }
}