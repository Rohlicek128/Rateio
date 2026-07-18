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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.rohlicek.rateio.data.db.ImdbRatingEntity
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.preferences.SyncPreferences
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.imdb.ManualSyncWorker
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants


@Composable
fun SettingsDatabaseScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val haptic = LocalHapticFeedback.current

    val workManager = remember { WorkManager.getInstance(context) }
    val preferences = remember { SyncPreferences(context) }

    // 1. Observe Last Sync Time
    val lastSyncTimestamp by preferences.lastSyncTime.collectAsState(initial = null)

    // 2. Observe active WorkManager tasks matching our sync name
    val workInfos by workManager.getWorkInfosForUniqueWorkFlow("manual_imdb_sync")
        .collectAsState(initial = emptyList())

    val activeWork = workInfos.firstOrNull()
    val isRunning = activeWork?.state == WorkInfo.State.RUNNING
    val progress = activeWork?.progress?.getInt("PROGRESS", 0) ?: 0


    var rating by remember(lastSyncTimestamp) { mutableStateOf<ImdbRatingEntity?>(null) }
    LaunchedEffect(key1 = lastSyncTimestamp) {
        rating = imdbRepository.getRatingByImdbId("tt12042730")
    }

    ScreenScaffold(
        title = "Database",
        onBackClick = onBackClick,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
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
                            if (isRunning) {
                                Text("Downloading and processing ratings...")
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text("$progress%")
                            } else {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                        // Trigger manual EXPEDITED sync
                                        val syncRequest = OneTimeWorkRequestBuilder<ManualSyncWorker>()
                                            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                            .build()

                                        workManager.enqueueUniqueWork(
                                            "manual_imdb_sync",
                                            ExistingWorkPolicy.REPLACE, // Replace if they press sync again
                                            syncRequest
                                        )
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