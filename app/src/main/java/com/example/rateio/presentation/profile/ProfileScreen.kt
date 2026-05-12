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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.rateio.presentation.components.AdaptiveAsyncImage


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
                                val itemCount = (state.itemCount ?: "N/A").toString()
                                Text(
                                    text = "Rated $itemCount items",
                                    //modifier = Modifier.padding(horizontal = 4.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }


                    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) }

                    items(20) { id ->
                        Card(
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Text("Something #$id", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleMedium)
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