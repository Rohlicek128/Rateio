package com.rohlicek.rateio.presentation.browse

import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.components.CarouselDotsIndicator
import com.rohlicek.rateio.presentation.components.RateBox
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.utils.formatCompact
import kotlinx.coroutines.delay
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@Composable
fun TmdbTrendingCarousel(
    modifier: Modifier = Modifier,
    category: CategoryType,
    padding: PaddingValues = PaddingValues(16.dp),
    onItemClick: (externalId: String, type: CategoryType) -> Unit
) {
    val context = LocalContext.current
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }
    val viewModel: TmdbTrendingCarouselViewModel = viewModel(
        key = category.name,
        factory = TmdbTrendingCarouselViewModel.factory(category, imdbRepository),
    )
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current

    val realPageCount = if (state.results.isNotEmpty()) state.results.size else 20
    val shape = MaterialTheme.shapes.extraLarge

    val pageCount = 2.0.pow(12.0).toInt()
    val initialPage = (pageCount / 2) - ((pageCount / 2) % realPageCount)

    val carouselState = rememberCarouselState(
        initialItem = initialPage
    ) { pageCount }

    LaunchedEffect(carouselState) {
        snapshotFlow { carouselState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    while (true) {
                        delay(7.seconds)
                        if (!carouselState.isScrollInProgress) {
                            carouselState.animateScrollToItem(
                                item = carouselState.currentItem + 1,
                                animationSpec = tween(
                                    durationMillis = 1250,
                                    easing = EaseInOut,
                                )
                            )
                        } else {
                            break
                        }
                    }
                }
            }
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(padding),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalMultiBrowseCarousel(
            state = carouselState,
            preferredItemWidth = 330.dp,
            itemSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) { index ->
            Box {
                val item = if (!state.isLoading) state.results[index % realPageCount] else null
                AsyncImage(
                    model = item?.coverImageUrl,
                    contentDescription = item?.title,
                    modifier = Modifier
                        .height(220.dp)
                        .maskClip(shape)
                        .clickable {
                            if (item?.externalId != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                onItemClick(item.externalId, category)
                            }
                        },
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                    error = ColorPainter(MaterialTheme.colorScheme.surfaceContainer),
                )

                if (!state.isLoading && item != null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .maskClip(shape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.8f),
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                lineHeight = 1.em,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (item.ratingWeight > 0f) {
                                Text(
                                    //text = item.subtitle,
                                    text = "${formatCompact(item.ratingWeight.toLong())} Votes",
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        RateBox(
                            rating = item.rating,
                            colorBucketsOverride = when (category) {
                                CategoryType.TMDB_SHOWS -> RatingColorBucketConstants.RC_IMDB_SHOWS
                                CategoryType.TMDB_MOVIES -> RatingColorBucketConstants.RC_IMDB_MOVIES
                                else -> getCurrentRatingColorBuckets()
                            }
                        )
                    }
                }
                else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .maskClip(shape)
                    ) {
                        ScreenLoading()
                    }
                }
            }
        }

        CarouselDotsIndicator(realPageCount, carouselState)
    }
}