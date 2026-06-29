package com.example.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.ScreenScaffold
import com.example.rateio.presentation.components.RateBox
import com.example.rateio.presentation.components.RatingBottomSheet
import com.example.rateio.presentation.components.SelectionList
import com.example.rateio.presentation.rating.display.RatingTransformationsConstants
import com.example.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.example.rateio.presentation.rating.display.getMaxValue
import com.example.rateio.presentation.rating.display.getMinValue
import com.example.rateio.utils.formatCompact


@Composable
fun SettingsRatingScreen(
    onBackClick: () -> Unit,
) {
    val items = listOf("IMDB", "PERCENTAGE", "TEN STARS", "FIVE STARS", "ELEVEN", "THOUSAND", "FIVE", "RECOMMEND", "FLOAT")
    var currentTransformation by remember {
        mutableStateOf(getCurrentRatingTransformations())
    }

    var showRatingSheet by remember { mutableStateOf(false) }

    val initialRating: Float? = 0.9f
    var ratingPer by remember(RatingTransformationsConstants.currentTransformation) {
        mutableStateOf(initialRating)
    }
    val rtf = getCurrentRatingTransformations()

    ScreenScaffold(
        title = "Rating Visualization",
        onBackClick = onBackClick,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        roundedCorners = 18.dp,
                        width = 24.dp,
                        minWidth = 42.dp,
                        height = 4.dp,
                        textStyle = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        loadingSize = 38.dp,
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

            item {
                SelectionList(
                    selectedIndex = -1,
                    listNames = items,
                    onSelect = {
                        currentTransformation = when (it) {
                            1 -> RatingTransformationsConstants.TF_PERCENTAGE
                            2 -> RatingTransformationsConstants.TF_TEN_STARS
                            3 -> RatingTransformationsConstants.TF_FIVE_STARS
                            4 -> RatingTransformationsConstants.TF_ELEVEN
                            5 -> RatingTransformationsConstants.TF_THOUSAND
                            6 -> RatingTransformationsConstants.TF_FIVE
                            7 -> RatingTransformationsConstants.TF_RECOMMEND
                            8 -> RatingTransformationsConstants.TF_FLOAT
                            else -> RatingTransformationsConstants.TF_IMDB
                        }
                        RatingTransformationsConstants.currentTransformation = currentTransformation
                        ratingPer = ratingPer?.plus(0.0000001f)
                    },
                )
            }
        }
    }
}