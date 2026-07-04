package com.example.rateio.presentation.rating.tmdb

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.WrapText
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.WrapText
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.RateioDatabase
import com.example.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.example.rateio.data.remote.tmdb.TmdbShowMetadata
import com.example.rateio.data.remote.tmdb.toCarouselImage
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.ItemStatus
import com.example.rateio.model.RateItem
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CollapsibleHeader
import com.example.rateio.presentation.components.DateProgressBar
import com.example.rateio.presentation.components.DisplaySelector
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.ItemStatCard
import com.example.rateio.presentation.components.ItemStatusSelector
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.ReviewCard
import com.example.rateio.presentation.components.SectionHeader
import com.example.rateio.presentation.components.SortBySelectionButton
import com.example.rateio.presentation.components.rating.ChildrenGrid
import com.example.rateio.presentation.components.rating.ChildrenList
import com.example.rateio.presentation.components.rating.ChildrenTimeline
import com.example.rateio.presentation.components.rating.ChildrenWrapped
import com.example.rateio.presentation.components.rating.ItemProgressBar
import com.example.rateio.presentation.components.rating.RateItemList
import com.example.rateio.presentation.components.settings.ModalShowsSettingsSheet
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.presentation.rating.display.RatingColorBucketConstants
import com.example.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.SettingListItem
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.formatTime
import kotlinx.serialization.json.Json
import java.util.Locale
import kotlin.math.round


@Composable
fun TmdbShowDetailScreen(
    showId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onStatusSaved: ((ItemStatus) -> Unit)? = null,
    onMetadataSaved: ((String?) -> Unit)? = null,
    onBackClick: () -> Unit,
    onEpisodeClick: (showId: Int, seasonNumber: Int, episodeNumber: Int) -> Unit,
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

    val viewModel: TmdbShowDetailViewModel = viewModel(
        factory = TmdbShowDetailViewModel.factory(showId, categoryRepository, itemRepository)
    )
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current

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
        state.show != null -> {
            val show = state.show!!
            val metadata = remember(state.savedItem?.metadataJSON) {
                state.savedItem?.metadataJSON?.let {
                    runCatching { Json.decodeFromString<TmdbShowMetadata>(it) }.getOrNull()
                } ?: TmdbShowMetadata()
            }

            val seasons = show.seasons.filter { it.seasonNumber > 0 }.sortedBy { it.seasonNumber }
            val episodesViewModel: TmdbEpisodesViewModel = viewModel(
                factory = TmdbEpisodesViewModel.factory(
                    showId = showId,
                    seasonNumbers = seasons.map { it.seasonNumber },
                    imdbId = show.externalIds?.imdbId,
                    fetchRatings = !isSaved
                )
            )
            val episodesState by episodesViewModel.state.collectAsState()

            var selectedRatings by remember { mutableIntStateOf(if (!isSaved) 0 else 2) }
            var invertedGrid by remember { mutableStateOf(false) }
            var columnsWrapped by remember { mutableFloatStateOf(4f) }

            val userRatings = viewModel.userRatingsState.collectAsStateWithLifecycle()
            val listOfRatings = userRatings.value.values.flatMap { it.values }.filterNotNull()

            val ratings: Map<Int, Map<Int, Float?>> = when (selectedRatings) {
                0 -> episodesState.imdbRatings
                1 -> episodesState.seasonEpisodes.mapValues { (_, episodes) ->
                    episodes.associate { it -> it.episodeNumber to it.voteAverage?.div(10f).takeIf { it != 0f } }
                }
                2 -> userRatings.value
                else -> episodesState.imdbRatings
            }

            val childrenGroups: Map<RateItem?, List<RateItem>> = episodesState.seasonEpisodes.entries
                .associate { (seasonNumber, episodes) ->
                    val items = episodes.sortedBy { it.episodeNumber }.map { episode ->
                        RateItem(
                            id = 0,
                            categoryId = 0,
                            title = episode.name,
                            subtitle = "Episode ${episode.episodeNumber}",
                            rating = ratings[episode.seasonNumber]?.get(episode.episodeNumber),
                            coverImageUrl = episode.stillPath?.let { "https://image.tmdb.org/t/p/original$it" },
                            coverImageLowUrl = episode.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" },
                            externalId = episode.id.toString(),
                            externalSource = CategoryType.TMDB_EPISODES,
                            metadataJSON = Json.encodeToString(TmdbEpisodeMetadata(
                                showId = showId,
                                seasonNumber = episode.seasonNumber,
                                episodeNumber = episode.episodeNumber,
                                runtime = episode.runtime,
                            ))
                        )
                    }
                    val season = seasons[seasonNumber - 1]
                    val seasonItem = RateItem(
                        id = seasonNumber.toLong(),
                        categoryId = 0,
                        title = "Season $seasonNumber",
                        subtitle = "${(season.airDate ?: "N/A").take(4)} | ${season.episodeCount} episodes",
                        rating = null,
                        coverImageUrl = season.posterPath.let { "https://image.tmdb.org/t/p/original$it" },
                        coverImageLowUrl = season.posterPath.let { "https://image.tmdb.org/t/p/w342$it" },
                        externalId = season.id.toString(),
                    )
                    seasonItem to items
                }
            val onChildClick = { child: RateItem ->
                val metadata = child.metadataJSON?.let {
                    runCatching {
                        Json.decodeFromString<TmdbEpisodeMetadata>(it)
                    }.getOrNull()
                }
                if (metadata != null) {
                    onEpisodeClick(metadata.showId,
                        metadata.seasonNumber,
                        metadata.episodeNumber
                    )
                }
            }



            if (listOfRatings.size >= show.numberOfEpisodes)
                onStatusSaved?.invoke(ItemStatus.COMPLETED)
            var status by remember(state.savedItem?.status) { mutableStateOf(
                when {
                    listOfRatings.size >= show.numberOfEpisodes -> ItemStatus.COMPLETED
                    state.savedItem == null -> ItemStatus.IN_PROGRESS
                    else -> state.savedItem!!.status
                }
            ) }


            val unwatchedEpisodes = episodesState.seasonEpisodes.mapValues { (season, episodes) ->
                episodes.filter { episode ->
                    userRatings.value[season]?.get(episode.episodeNumber) == null
                }
            }.flatMap { it.value }
            val nextToWatchEpisode = if (unwatchedEpisodes.isNotEmpty() && status != ItemStatus.COMPLETED)
                unwatchedEpisodes.first() else null

            val expandWatchedSeason = true
            if (show.numberOfSeasons == 1) state.expandedSeasons.add("Season 1")
            else if (isSaved && expandWatchedSeason && nextToWatchEpisode != null) {
                state.expandedSeasons.add("Season ${nextToWatchEpisode.seasonNumber}") // TODO: get real season name
            }


            val sortedBestEpisodes = remember(childrenGroups) {
                childrenGroups
                    .flatMap { (_, episodes) -> episodes }
                    .sortedByDescending { episode ->
                        episode.rating ?: -1f
                    }
            }
            val sortedEpisodes = remember(childrenGroups, state.sortMode) {
                childrenGroups
                    .flatMap { (_, episodes) -> episodes }
                    .let { episodes ->
                        when (state.sortMode) {
                            SortMode.BY_RATING_WORST -> episodes.sortedBy { episode ->
                                episode.rating ?: 2f
                            }
                            SortMode.BY_RUNTIME -> episodes.sortedByDescending { episode ->
                                val metadata = episode.metadataJSON?.let {
                                    runCatching {
                                        Json.decodeFromString<TmdbEpisodeMetadata>(it)
                                    }.getOrNull()
                                }
                                metadata?.runtime
                            }
                            SortMode.BY_NAME -> episodes.sortedBy { episode ->
                                episode.title
                            }
                            else -> sortedBestEpisodes
                        }
                    }
            }
            val episodesCount = if (selectedRatings == 2) listOfRatings.size else sortedBestEpisodes.size
            val sortedEpisodesTop = when {
                episodesCount >= 10 -> {
                    sortedBestEpisodes.take(round(episodesCount * 0.1f).toInt().coerceIn(3, 10))
                }
                episodesCount in 5..<10 -> {
                    sortedBestEpisodes.take(1)
                }
                else -> {
                    emptyList()
                }
            }

            val spoilers = metadata.showSpoilers || !isSaved
            val spoilName = true
            val spoilEpisode = { seasonNumber: Int, episodeNumber: Int ->
                spoilers || (selectedRatings == 2 && ratings[seasonNumber]?.get(episodeNumber) != null) || selectedRatings != 2
            }


            // Settings
            var showSettings by remember { mutableStateOf(false) }
            if (showSettings) {
                ModalShowsSettingsSheet(
                    metadata = metadata,
                    onValueChange = { newMetadata ->
                        onMetadataSaved?.invoke(Json.encodeToString(newMetadata))
                        viewModel.updateSavedItem()
                    },
                    onDismiss = { showSettings = false }
                )
            }

            RateItemDetailScreen(
                title = show.name,
                subtitle = buildString {
                    show.firstAirDate?.take(4)?.let { append(it) }
                    show.lastAirDate?.take(4)?.let { append(" - $it") }
                    if (show.status != null && show.status != "Ended") append("  |  ${show.status}")
                }.ifBlank { null },
                categoryName = CategoryRegistry.forType(CategoryType.TMDB_SHOWS)?.name,
                description = show.overview,
                coverImageUrl = show.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = show.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = if (!isSaved) state.imdbRating?.normalizedRating else customRating,
                ratingVotes = if (!isSaved) state.imdbRating?.voteCount else null,
                ratingLabel = state.imdbRating?.normalizedRating?.let { "%.1f/10 on IMDb".format(it * 10f) },
                ratingColorBucketsOverride = if (!isSaved) RatingColorBucketConstants.RC_IMDB_SHOWS else getCurrentRatingColorBuckets(),
                onRatingSaved = onRatingSaved,
                onBackClick = onBackClick,
                canAddToLibrary = false,
                onOpenSettings = { showSettings = true },
                headerExtraContent = {
                    //Stats
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            ItemStatCard(
                                header = "Seasons",
                                statistic = show.numberOfSeasons.toString(),
                            )
                            ItemStatCard(
                                header = "Episodes",
                                statistic = show.numberOfEpisodes.toString(),
                            )
                            ItemStatCard(
                                header = "Popularity",
                                statistic = "%.1f".format(Locale.US, show.popularity),
                            )
                        }
                    }
                },
                extraContent = {
                    // Library
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            LibraryToggle(
                                checked = state.savedItem != null,
                                onCheckedChange = {
                                    viewModel.onToggleSaved(state.show!!)
                                },
                                itemName = show.name,
                            )
                        }
                    }

                    // Genres
                    if (show.genres.isNotEmpty()) {
                        item {
                            GenreChips(
                                genres = show.genres.map { it.name },
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }


                    if (isSaved || userRatings.value.isNotEmpty()) {
                        // Progress Bar
                        item {
                            val headerName = "Progress"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                ItemStatusSelector(
                                    selected = status,
                                    onStatusSelected = {
                                        status = it
                                        onStatusSaved?.invoke(it)
                                    }
                                ) { openSheet ->
                                    val remaining = show.numberOfEpisodes - listOfRatings.size
                                    ItemProgressBar(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        endString = "${listOfRatings.size}/${show.numberOfEpisodes} episodes",
                                        endValue = show.numberOfEpisodes.toFloat(),
                                        currentString = "Remaining $remaining episode${if (remaining == 1) "" else "s"}",
                                        currentValue = listOfRatings.size.toFloat(),
                                        status = status,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            openSheet()
                                        }
                                    )
                                }
                            }
                        }

                        // Next Episode
                        if (nextToWatchEpisode != null &&
                            !(nextToWatchEpisode.episodeNumber == show.nextEpisodeToAir?.episodeNumber &&
                            nextToWatchEpisode.seasonNumber == show.nextEpisodeToAir.seasonNumber)) {
                            item {
                                val headerName = "Next Episode"
                                CollapsibleHeader(
                                    headerName,
                                    isOpened = headerName !in state.collapsedHeaders,
                                    onClick = {
                                        if (it) state.collapsedHeaders.remove(headerName)
                                        else state.collapsedHeaders.add(headerName)
                                    }
                                ) {
                                    RateItemCard(
                                        title = nextToWatchEpisode.name,
                                        subtitle = "Season ${nextToWatchEpisode.seasonNumber}, Episode ${nextToWatchEpisode.episodeNumber}",
                                        coverImagePath = if (!nextToWatchEpisode.stillPath.isNullOrBlank())
                                            "https://image.tmdb.org/t/p/w300${nextToWatchEpisode.stillPath}" else null,
                                        rating = ratings[nextToWatchEpisode.seasonNumber]?.get(nextToWatchEpisode.episodeNumber),
                                        isLoading = false,
                                        placeholderRatio = 16f / 9f,
                                        padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        bubbleText = if (nextToWatchEpisode.runtime > 0) formatTime(nextToWatchEpisode.runtime) else null,
                                        onClick = { onEpisodeClick(
                                            show.id,
                                            nextToWatchEpisode.seasonNumber,
                                            nextToWatchEpisode.episodeNumber
                                        ) },
                                        spoilers = spoilers,
                                        spoilName = spoilName,
                                        //modifier = Modifier.width(260.dp)
                                    )
                                }
                            }
                        }

                    }


                    // Upcoming Episode
                    if (show.nextEpisodeToAir != null) {
                        val episode = show.nextEpisodeToAir
                        item {
                            val headerName = "Upcoming Episode"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                Column {
                                    DateProgressBar(
                                        startDateString = show.lastEpisodeToAir?.airDate,
                                        endDateString = episode.airDate,
                                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                                    )
                                    RateItemCard(
                                        title = episode.name,
                                        subtitle = "S${episode.seasonNumber}E${episode.episodeNumber}  |  ${formatDate(episode.airDate, pattern = "MMM. d")}",
                                        coverImagePath = if (!episode.stillPath.isNullOrBlank())
                                            "https://image.tmdb.org/t/p/w300${episode.stillPath}" else null,
                                        rating = ratings[episode.seasonNumber]?.get(episode.episodeNumber),
                                        isLoading = false,
                                        placeholderRatio = 16f / 9f,
                                        padding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        onClick = { onEpisodeClick(
                                            show.id,
                                            episode.seasonNumber,
                                            episode.episodeNumber
                                        ) },
                                        spoilers = spoilEpisode(episode.seasonNumber, episode.episodeNumber),
                                        spoilName = spoilName,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Creators
                    show.createdBy.takeIf { it.isNotEmpty() }?.let { creator ->
                        item {
                            val headerName = "Creators"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                LazyRow(
                                    modifier = Modifier.height(150.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(creator.take(10), key = { it.creditId }) { member ->
                                        PersonCard(
                                            name = member.name,
                                            position = null,
                                            profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                            width = 80.dp,
                                            height = 100.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cast
                    if (show.credits != null && show.credits.cast.isNotEmpty()) {
                        item {
                            val headerName = "Cast"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                LazyRow(
                                    modifier = Modifier.height(200.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    show.credits?.cast?.takeIf { it.isNotEmpty() }?.let { cast ->
                                        items(cast.take(10), key = { it.creditId }) { member ->
                                            PersonCard(
                                                name = member.name,
                                                position = member.character,
                                                profilePath = member.profilePath?.let { "https://image.tmdb.org/t/p/w185$it" },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //Images
                    state.images?.posters?.takeIf { it.isNotEmpty() }?.let { images ->
                        item {
                            val headerName = "Posters"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                AdaptiveImageCarousel(
                                    baseUrl = "https://image.tmdb.org/t/p/w500",
                                    images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                    itemWidth = 110.dp,
                                    itemHeight = 180.dp,
                                    shape = MaterialTheme.shapes.large,
                                )
                            }
                        }
                    }
                    state.images?.backdrops?.takeIf { it.isNotEmpty() }?.let { images ->
                        item {
                            val headerName = "Backdrops"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                AdaptiveImageCarousel(
                                    baseUrl = "https://image.tmdb.org/t/p/w780",
                                    images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                    itemWidth = 240.dp,
                                    shape = MaterialTheme.shapes.large,
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Episodes
                    item { SectionHeader("Episodes") }
                    item {
                        DisplaySelector(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            selectedIndex = selectedRatings,
                            onSelectionChanged = {
                                selectedRatings = it
                                if (selectedRatings == 0 && episodesState.imdbRatings.isEmpty() &&
                                    isSaved && !episodesState.isLoadingRatings) {
                                    episodesViewModel.fetchImdbRatings(imdbId = show.externalIds?.imdbId)
                                }
                            },
                            options = listOf("IMDb", "TMDb", "Yours"),
                        )
                    }
                    item {
                        DisplaySelector(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            selectedIndex = state.selectedEpisodeMode,
                            onSelectionChanged = viewModel::onModeSelect,
                            options = listOf("List", "Grid", "Wrapped", "Timeline"),
                            unCheckedIcons = listOf(Icons.AutoMirrored.Outlined.List, Icons.Outlined.GridOn, Icons.AutoMirrored.Outlined.WrapText, Icons.Outlined.Timeline),
                            checkedIcons = listOf(Icons.AutoMirrored.Filled.List, Icons.Filled.GridOn, Icons.AutoMirrored.Filled.WrapText, Icons.Filled.Timeline),
                        )
                    }

                    when {
                        episodesState.isLoadingEpisodes -> item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                CircularWavyProgressIndicator()
                            }
                        }
                        episodesState.seasonEpisodes.isNotEmpty() -> {
                            when (state.selectedEpisodeMode) {
                                0 -> {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            SortBySelectionButton(
                                                selected = state.sortMode,
                                                onSelect = viewModel::onSortModeSelect,
                                            )

                                            AnimatedVisibility(
                                                visible = state.sortMode == SortMode.BY_SEASON,
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.End,
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                                            childrenGroups.keys.forEach { parent ->
                                                                state.expandedSeasons.add(parent?.title)
                                                            }
                                                        },
                                                        shapes = ButtonDefaults.shapes(),
                                                    ) {
                                                        Icon(
                                                            Icons.Default.UnfoldMore,
                                                            contentDescription = "Expand",
                                                            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                                        )
                                                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                                        Text(
                                                            "Expand",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    OutlinedButton(
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                                            childrenGroups.keys.forEach { parent ->
                                                                state.expandedSeasons.remove(parent?.title)
                                                            }
                                                        },
                                                        shapes = ButtonDefaults.shapes(),
                                                    ) {
                                                        Icon(
                                                            Icons.Default.UnfoldLess,
                                                            contentDescription = "Collapse",
                                                            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                                        )
                                                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                                        Text(
                                                            "Collapse",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    when (state.sortMode) {
                                        SortMode.BY_SEASON -> {
                                            item {
                                                ChildrenList(
                                                    childrenGroups = childrenGroups,
                                                    onChildClick = onChildClick,
                                                    expandedParents = state.expandedSeasons,
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    displayNotNullCounter = selectedRatings == 2,
                                                )
                                            }
                                        }
                                        else -> {
                                            item {
                                                RateItemList(
                                                    items = sortedEpisodes,
                                                    onChildClick = onChildClick,
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            OutlinedToggleButton(
                                                checked = invertedGrid,
                                                onCheckedChange = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                                    invertedGrid = it
                                                },
                                                shapes = ToggleButtonDefaults.shapes(),
                                            ) {
                                                Text(
                                                    "Inverted",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                    }
                                    item {
                                        ChildrenGrid(
                                            contentPadding = PaddingValues(horizontal = 12.dp),
                                            childrenGroups = childrenGroups,
                                            rowText = { "E$it" },
                                            columnText = { "S$it" },
                                            onChildClick = onChildClick,
                                        )
                                    }
                                }
                                2 -> {
                                    item {
                                        SettingListItem(
                                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                                            title = "Columns",
                                            description = "Value: ${columnsWrapped.toInt() + 1}",
                                            position = ListItemPosition.SINGLE,
                                            supportingContent = {
                                                Slider(
                                                    columnsWrapped,
                                                    onValueChange = { value ->
                                                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                                        columnsWrapped = value
                                                    },
                                                    steps = 13,
                                                    valueRange = 0f..14f
                                                )
                                            }
                                        )
                                    }
                                    item {
                                        ChildrenWrapped(
                                            contentPadding = PaddingValues(horizontal = 20.dp),
                                            childrenGroups = childrenGroups,
                                            columns = columnsWrapped.toInt() + 1,
                                            onChildClick = onChildClick,
                                        )
                                    }
                                }
                                3 -> {
                                    item {
                                        ChildrenTimeline(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            childrenGroups = childrenGroups,
                                            onChildClick = onChildClick,
                                        )
                                    }
                                }
                                else -> {
                                    item {
                                        Text("Not implemented", modifier = Modifier.padding(horizontal = 16.dp))
                                        Spacer(modifier = Modifier.padding(vertical = 400.dp))
                                    }
                                }
                            }

                        }
                    }

                    state.reviews?.results?.takeIf { it.isNotEmpty() }?.let { reviews ->
                        item {
                            val headerName = "Reviews"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(reviews, key = { it.id }) { review ->
                                        ReviewCard(
                                            modifier = Modifier.size(width = 320.dp, height = 190.dp),
                                            name = review.author,
                                            supportingText = formatDate(review.updatedAt),
                                            avatarPath = "https://image.tmdb.org/t/p/w92${review.authorDetails?.avatarPath}",
                                            rating = review.authorDetails?.rating?.div(10f),
                                            content = review.content,
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