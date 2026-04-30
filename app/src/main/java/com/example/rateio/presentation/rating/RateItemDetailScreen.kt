package com.example.rateio.presentation.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rateio.presentation.components.AdaptiveAsyncImage
import com.example.rateio.presentation.components.RateBox


@Composable
fun RateItemDetailScreen(
    title: String,
    subtitle: String?,
    description: String?,
    coverImageUrl: String?,
    rating: Float?,
    ratingLabel: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraContent: LazyListScope.() -> Unit = {},
) {
    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 56.dp,
                bottom = innerPadding.calculateBottomPadding(),
            )
        ) {
            // Cover + title header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PosterWithRating(
                        imageUrl = coverImageUrl,
                        rating = rating
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            title,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                /*DetailHeader(
                    title = title,
                    subtitle = subtitle,
                    coverImageUrl = coverImageUrl,
                )*/
            }

            // Rating row
            /*item {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    RateBox(rating)
                }
                RatingRow(
                    rating = rating,
                    ratingLabel = ratingLabel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }*/

            // Description
            if (!description.isNullOrBlank()) {
                item {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Divider before extra content
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Type-specific content injected here
            extraContent()
        }


        FilledTonalIconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    start = 12.dp,
                )
                .zIndex(1f),
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}

@Composable
fun PosterWithRating(
    imageUrl: String?,
    rating: Float?,
    modifier: Modifier = Modifier
) {
    val rateBoxOverhang = 42.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(unbounded = true),
    ) {
        Card(
            onClick = { },
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.align(Alignment.Center)
        ) {
            AdaptiveAsyncImage(
                model = imageUrl,
                maxHeight = 500.dp
            )
        }
        RateBox(
            rating = rating,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = rateBoxOverhang),
            scale = 10.dp
        )
    }
}

@Composable
private fun DetailHeader(title: String, subtitle: String?, coverImageUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = coverImageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RatingRow(rating: Float?, ratingLabel: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (rating != null) {
            // Show as x/10 for readability
            Text(
                text = "%.1f".format(rating * 10),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = ratingLabel ?: "/10",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = "Not yet rated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}