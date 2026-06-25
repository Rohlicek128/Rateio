package com.example.rateio.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.category.CategoryItemListScreen
import com.example.rateio.presentation.category.ItemListRow
import com.example.rateio.presentation.components.AdaptiveAsyncImage
import com.example.rateio.presentation.rating.display.getCurrentRatingTransformations
import com.example.rateio.presentation.rating.display.getMaxValue
import com.example.rateio.presentation.rating.display.getMinValue
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.presentation.rating.display.getTransformedRating


@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val categoryRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        CategoryRepository(db.categoryDao())
    }
    val itemRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        RateItemRepository(db.rateItemDao())
    }

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(itemRepository, categoryRepository)
    )
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current


    val rtf = getCurrentRatingTransformations()
    val barChartGroups = state.items.groupBy { if (it.rating != null) getTransformedRating(it.rating) else null }
    val barChartEntries = barChartGroups.mapValues {
            BarChartEntry(
                label = it.key ?: "Null",
                itemCount = it.value.size.coerceAtLeast(0),
                order = it.value.first().rating ?: 0.0f,
                color = getRatingColor(it.value.last().rating).backgroundColor
            )
        }.toMutableMap()
    for (i in 0..rtf.stepCount.toInt()) {
        val ratingGroup = getTransformedRating(i.toFloat() / rtf.stepCount.toFloat())
        barChartEntries.putIfAbsent(ratingGroup, BarChartEntry(
            label = ratingGroup,
            itemCount = 0,
            order = i.toFloat() / rtf.stepCount.toFloat(),
        ))
    }

    var selectedRatingGroup by remember { mutableStateOf("Null") }


    Scaffold { innerPadding ->
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 76.dp,
                        bottom = innerPadding.calculateBottomPadding(),
                    )
                ) {
                    // Header + Backdrop
                    item {
                        val backgroundColor = MaterialTheme.colorScheme.background
                        val offset = (-76).dp

                        Box(
                            modifier = Modifier.fillMaxWidth().offset(y = offset),
                        ) {
                            AdaptiveAsyncImage(
                                model = "https://image.tmdb.org/t/p/w1280/ignr9C0hv1iMLDUWrX90lM6Bkmk.jpg", //y8vQKqARUYPktoQTp1kLuHqBZkh.jpg
                                placeholderRatio = 16f / 9f,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth(unbounded = true),
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.0f))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                backgroundColor.copy(alpha = 0.4f),
                                                backgroundColor,
                                            ),
                                            startY = 0f,
                                            endY = Float.POSITIVE_INFINITY,
                                        )
                                    )
                            )

                            // Header
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .align(Alignment.BottomStart)
                                    .offset(y = -offset - 16.dp),
                                horizontalAlignment = Alignment.Start,
                            ) {
                                Text(
                                    "Profile",
                                    style = MaterialTheme.typography.displayMedium,
                                    lineHeight = 1.1.em,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center
                                )
                                /*val itemCount = state.items.size.toString()
                                Text(
                                    text = "Rated $itemCount items",
                                    //modifier = Modifier.padding(horizontal = 4.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )*/
                            }
                        }
                    }


                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) }

                    item {
                        StatCardRow(
                            itemCount = state.items.size,
                            categoryCount = 3,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        )
                    }

                    item {
                        RatingsBarChart(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            entries = barChartEntries.map { it.value }.sortedBy { it.order },
                            onSelect = {
                                selectedRatingGroup = it
                            }
                        )
                    }

                    if (selectedRatingGroup != "Null") {
                        item {
                            Card(
                                shape = RoundedCornerShape(28.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 0.dp, vertical = 20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 20.dp),
                                        text = selectedRatingGroup,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    val selectedItems = barChartGroups[selectedRatingGroup]
                                    if (selectedItems != null) {
                                        ItemListRow(
                                            title = "",
                                            items = selectedItems.sortedByDescending { it.rating ?: -1f },
                                            isLoading = false,
                                            showRanking = true,
                                            showNullRatings = true,
                                            //placeholderRatio = 16f / 9f,
                                            onItemClick = { item ->
                                                item.externalId?.let { id ->
                                                    item.externalSource?.let { type ->

                                                    }
                                                }
                                            },
                                            emptyContent = {
                                                Text(
                                                    "Nothing Selected",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(200.dp)) }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onOpenSettings()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ),
                        modifier = Modifier
                            .padding(
                                top = innerPadding.calculateTopPadding() + 12.dp,
                                end = 12.dp,
                            )
                            .zIndex(1f),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                }
            }

        }
    }
}