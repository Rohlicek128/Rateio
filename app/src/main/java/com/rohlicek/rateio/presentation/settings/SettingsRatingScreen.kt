package com.rohlicek.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.components.RateBoxSizeDefaults
import com.rohlicek.rateio.presentation.components.RatingBottomSheet
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.RatingTransformationsConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.rohlicek.rateio.presentation.rating.display.getMaxValue
import com.rohlicek.rateio.presentation.rating.display.getMinValue


@Composable
fun SettingsRatingScreen(
    onRatingTransformationClick: () -> Unit,
    onRatingColorClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    val transformations = listOf(
        RatingTransformationsConstants.TF_IMDB,
        RatingTransformationsConstants.TF_IMDB_PRECISE,
        RatingTransformationsConstants.TF_PERCENTAGE,
        RatingTransformationsConstants.TF_PERCENTAGE_PRECISE,
        RatingTransformationsConstants.TF_TEN_STARS,
        RatingTransformationsConstants.TF_FIVE_STARS_ZERO,
        RatingTransformationsConstants.TF_TEN_ZERO,
        RatingTransformationsConstants.TF_FIVE,
        RatingTransformationsConstants.TF_THOUSAND,
        RatingTransformationsConstants.TF_RECOMMEND,
        RatingTransformationsConstants.TF_FLOAT,
    )
    var currentTransformation by remember {
        mutableStateOf(getCurrentRatingTransformations())
    }

    val buckets = listOf(
        RatingColorBucketConstants.RC_IMDB_MOVIES,
        RatingColorBucketConstants.RC_IMDB_SHOWS,
        RatingColorBucketConstants.RC_IMDB_EPISODES,
        RatingColorBucketConstants.RC_CUSTOM_MOVIE,
        RatingColorBucketConstants.RC_DECADIC,
        RatingColorBucketConstants.RC_STEAM,
        RatingColorBucketConstants.RC_CSFD,
    )
    var currentBucket by remember {
        mutableStateOf(getCurrentRatingColorBuckets())
    }

    var showRatingSheet by remember { mutableStateOf(false) }

    var ratingPer by remember(
        RatingTransformationsConstants.currentTransformation,
        RatingColorBucketConstants.currentBuckets
    ) {
        mutableStateOf<Float?>(0.87654f)
    }
    val rtf = getCurrentRatingTransformations()

    ScreenScaffold(
        title = "Rating Visualization",
        onBackClick = onBackClick,
    ) { padding, listState ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            state = listState,
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = rtf.getMinValue(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    RateBox(
                        rating = ratingPer,
                        size = RateBoxSizeDefaults.DISPLAY,
                        onClick = {
                            showRatingSheet = true
                        }
                    )

                    Text(
                        text = rtf.getMaxValue(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (showRatingSheet) {
                    RatingBottomSheet(
                        rating = ratingPer,
                        onDismiss = { showRatingSheet = false },
                        onValueChange = { ratingPer = it }
                    )
                }
            }

            item { SettingsListHeader("Transformations") }
            itemsIndexed(transformations) { index, tf ->
                SettingListItem(
                    title = tf.name,
                    description = "(rating * ${tf.stepCount} + ${tf.offset}) / ${tf.divider}",
                    position = when {
                        transformations.size == 1 -> ListItemPosition.SINGLE
                        index == 0 -> ListItemPosition.START
                        index == transformations.size - 1 -> ListItemPosition.END
                        else -> ListItemPosition.MIDDLE
                    },
                    onClick = {
                        currentTransformation = tf
                        RatingTransformationsConstants.currentTransformation = currentTransformation
                        ratingPer = ratingPer?.plus(0.0000001f)
                    },
                    showNavigateIconOnClick = false,
                    trailingContent = {
                        RateBox(
                            rating = ratingPer,
                            transformationOverride = tf,
                            colorBucketsOverride = currentBucket,
                        )
                    },
                    colors = if (currentTransformation == tf) {
                        ListItemDefaults.colors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                    } else ListItemDefaults.colors(),
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SettingListItem(
                    title = "Custom Transformation",
                    description = "Something, Something important. And is quite long for a setting label/description.",
                    icon = Icons.Default.Transform,
                    position = ListItemPosition.SINGLE,
                    onClick = onRatingTransformationClick,
                )
            }


            item { SettingsListHeader("Colors") }
            itemsIndexed(buckets) { index, bucket ->
                SettingListItem(
                    title = bucket.name,
                    description = "${bucket.buckets.size} buckets",
                    position = when {
                        buckets.size == 1 -> ListItemPosition.SINGLE
                        index == 0 -> ListItemPosition.START
                        index == buckets.size - 1 -> ListItemPosition.END
                        else -> ListItemPosition.MIDDLE
                    },
                    onClick = {
                        currentBucket = bucket
                        RatingColorBucketConstants.currentBuckets = currentBucket
                        ratingPer = ratingPer?.plus(0.0000001f)
                    },
                    showNavigateIconOnClick = false,
                    trailingContent = {
                        RateBox(
                            rating = ratingPer,
                            transformationOverride = currentTransformation,
                            colorBucketsOverride = bucket,
                        )
                    },
                    colors = if (currentBucket == bucket) {
                        ListItemDefaults.colors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                    } else ListItemDefaults.colors(),
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SettingListItem(
                    title = "Custom Color Buckets",
                    description = "Something, Something important.",
                    icon = Icons.Default.Palette,
                    position = ListItemPosition.SINGLE,
                    onClick = onRatingColorClick,
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}