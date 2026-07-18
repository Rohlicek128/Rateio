package com.rohlicek.rateio.presentation.rating.tmdb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.tmdb.toCarouselImage
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.components.AdaptiveImageCarousel
import com.rohlicek.rateio.presentation.components.ImageSize
import com.rohlicek.rateio.presentation.components.LibraryToggle
import com.rohlicek.rateio.presentation.components.MajorSectionHeader
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.components.SectionHeader
import com.rohlicek.rateio.presentation.rating.RateItemDetailScreen
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.utils.formatDate

@Composable
fun TmdbPersonDetailScreen(
    personId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
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

    val viewModel: TmdbPersonDetailViewModel = viewModel(
        factory = TmdbPersonDetailViewModel.factory(personId, categoryRepository, itemRepository)
    )
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
        }
        state.person != null -> {
            val person = state.person!!

            RateItemDetailScreen(
                title = person.name,
                subtitle = buildString {
                    append(formatDate(person.birthday))
                    if (person.deathday != null) append(" - ${formatDate(person.deathday)}")
                    if (person.knownForDepartment != null) append("  |  ${person.knownForDepartment}")
                }.ifBlank { null },
                categoryName = CategoryRegistry.forType(CategoryType.TMDB_MOVIES)?.name,
                description = person.biography,
                coverImageUrl = person.profilePath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = null,
                rating = if (!isSaved) null else customRating,
                showNullRating = isSaved,
                onRatingSaved = onRatingSaved,
                onBackClick = onBackClick,
                canAddToLibrary = false,
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LibraryToggle(
                                checked = state.savedItemId != null,
                                onCheckedChange = {
                                    //viewModel.onToggleSaved(state.person!!)
                                },
                                itemName = person.name,
                            )
                        }
                    }


                    //Images
                    person.images?.profiles?.takeIf { it.isNotEmpty() }?.let { images ->
                        item { SectionHeader("Images") }
                        item {
                            AdaptiveImageCarousel(
                                urlBuilder = { size, path ->
                                    "https://image.tmdb.org/t/p/${when(size) {
                                        ImageSize.MEDIUM -> "h632"
                                        ImageSize.LARGE -> "original"
                                    }}${path}"
                                },
                                images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                itemWidth = 130.dp,
                                itemHeight = 200.dp,
                                shape = MaterialTheme.shapes.large,
                                maximizable = true,
                            )
                        }
                    }


                    // Movies
                    if (person.combinedCredits != null && person.combinedCredits.cast.isNotEmpty()) {
                        person.combinedCredits.cast
                            .groupBy { it.mediaType }
                            .forEach { (media, credits) ->
                                item { MajorSectionHeader(media.uppercase()) }

                                credits.sortedByDescending { it.releaseDate ?: it.firstAirDate }
                                    .groupBy { (it.releaseDate ?: it.firstAirDate)?.take(4) }
                                    .forEach { (year, credits) ->
                                        item { SectionHeader(if (year.isNullOrBlank()) "Unknown" else year) }
                                        items(credits, key = { it.creditId }) { credit ->
                                            if (credit.character == null || !credit.character.contains("Self")) {
                                                RateItemCard(
                                                    title = credit.originalTitle ?: credit.originalName ?: "N/A",
                                                    subtitle = credit.character,
                                                    coverImagePath = credit.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                                    rating = credit.voteAverage?.let { if (it > 0f) it.div(10f) else null },
                                                    placeholderRatio = 2f / 3f,
                                                    padding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                                    onClick = { },
                                                    colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_MOVIES,
                                                )
                                            }
                                        }
                                    }
                            }

                    }

                }
            )
        }
    }
}