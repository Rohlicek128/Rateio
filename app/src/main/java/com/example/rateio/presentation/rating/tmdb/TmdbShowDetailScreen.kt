package com.example.rateio.presentation.rating.tmdb

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReplayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.rateio.model.computeAggregateRating
import com.example.rateio.presentation.components.AdaptiveImageCarousel
import com.example.rateio.presentation.components.CollapsibleHeader
import com.example.rateio.presentation.components.DateProgressBar
import com.example.rateio.presentation.components.DisplaySelector
import com.example.rateio.presentation.components.GenreChips
import com.example.rateio.presentation.components.ImageSize
import com.example.rateio.presentation.components.ItemStatCard
import com.example.rateio.presentation.components.LibraryToggle
import com.example.rateio.presentation.components.ModalEnumSelector
import com.example.rateio.presentation.components.PersonCard
import com.example.rateio.presentation.components.RateItemCard
import com.example.rateio.presentation.components.ReviewCard
import com.example.rateio.presentation.components.ScreenError
import com.example.rateio.presentation.components.ScreenLoading
import com.example.rateio.presentation.components.rating.ChildrenDisplay
import com.example.rateio.presentation.components.rating.ItemProgressBar
import com.example.rateio.presentation.rating.RateItemDetailScreen
import com.example.rateio.presentation.rating.display.RatingColorBucketConstants
import com.example.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.example.rateio.presentation.settings.ListItemPosition
import com.example.rateio.presentation.settings.ModalSettings
import com.example.rateio.presentation.settings.SettingListItem
import com.example.rateio.presentation.settings.SettingsListHeader
import com.example.rateio.presentation.settings.SettingsSwitch
import com.example.rateio.presentation.settings.SettingsTextField
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.formatItemRankLabel
import com.example.rateio.utils.formatTime
import com.example.rateio.utils.parseDate
import kotlinx.serialization.json.Json
import java.util.Locale


@Composable
fun TmdbShowDetailScreen(
    showId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    savedRank: Int? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onStatusSaved: ((ItemStatus) -> Unit)? = null,
    onCoverOverrideSaved: ((String?) -> Unit)? = null,
    onMetadataSaved: ((String?) -> Unit)? = null,
    onBackClick: () -> Unit,
    onEpisodeClick: (seasonItem: RateItem, episodeItem: RateItem) -> Unit,
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
            ScreenLoading()
        }
        state.error != null -> {
            ScreenError(state.error)
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

            val userRatings = viewModel.userRatingsState.collectAsStateWithLifecycle()
            val listOfRatings = userRatings.value.values.flatMap { it.values }.filterNotNull()

            val ratings: Map<Int, Map<Int, Float?>> = when (selectedRatings) {
                0 -> episodesState.imdbRatings
                1 -> episodesState.seasonEpisodes.mapValues { (_, episodes) ->
                    episodes.associate { it -> it.episodeNumber to it.voteAverage?.div(10f).takeIf { it != 0f } }
                }
                2 -> userRatings.value
                else -> userRatings.value
            }

            val childrenGroups: Map<RateItem?, List<RateItem>> = episodesState.seasonEpisodes.entries
                .associate { (seasonNumber, episodes) ->
                    val items = episodes.sortedBy { it.episodeNumber }.map { episode ->
                        RateItem(
                            id = -1,
                            parentId = seasonNumber.toLong(),
                            categoryId = state.savedItem?.categoryId ?: 0,
                            title = episode.name,
                            subtitle = "Episode ${episode.episodeNumber}",
                            length = if (episode.runtime > 0) episode.runtime.toFloat() else null,
                            rating = ratings[episode.seasonNumber]?.get(episode.episodeNumber),
                            coverImageUrl = episode.stillPath?.let { "https://image.tmdb.org/t/p/original$it" } ?: "",
                            coverImageLowUrl = episode.stillPath?.let { "https://image.tmdb.org/t/p/w300$it" } ?: "",
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
                        parentId = state.savedItem?.id,
                        categoryId = state.savedItem?.categoryId ?: 0,
                        title = "Season $seasonNumber",
                        subtitle = "${(season.airDate ?: "N/A").take(4)} | ${season.episodeCount} episodes",
                        length = season.episodeCount.toFloat(),
                        rating = computeAggregateRating(items),
                        coverImageUrl = season.posterPath.let { "https://image.tmdb.org/t/p/original$it" },
                        coverImageLowUrl = season.posterPath.let { "https://image.tmdb.org/t/p/w342$it" },
                        externalId = season.id.toString(),
                        externalSource = CategoryType.TMDB_SEASONS,
                    )
                    seasonItem to items
                }
            val onChildClick = { child: RateItem ->
                val parent = childrenGroups.keys.find { it?.id == child.parentId }
                if (parent != null) {
                    onEpisodeClick(parent, child,)
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
            if (isSaved && expandWatchedSeason && nextToWatchEpisode != null) {
                state.expandedSeasons.add("Season ${nextToWatchEpisode.seasonNumber}")
            }


            /*val episodesCount = if (selectedRatings == 2) listOfRatings.size else sortedBestEpisodes.size
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
            }*/

            val spoilers = metadata.showSpoilers || !isSaved
            var spoilName by remember { mutableStateOf(true) }
            val spoilEpisode = { seasonNumber: Int, episodeNumber: Int ->
                spoilers || (selectedRatings == 2 && userRatings.value[seasonNumber]?.get(episodeNumber) != null) || selectedRatings != 2
            }

            var showStatusSelector by remember { mutableStateOf(false) }

            // Settings
            var coverOverride by remember(state.savedItem) { mutableStateOf(state.savedItem?.coverImageOverride) }

            var showSettings by remember { mutableStateOf(false) }
            if (showSettings) {
                ModalSettings(
                    title = "${show.name}'s Settings",
                    onDismiss = { showSettings = false }
                ) {
                    item { SettingsListHeader("Spoilers") }
                    item {
                        SettingListItem(
                            title = "Hide Thumbnails",
                            description = "Override if unrated episodes will show their thumbnail image",
                            icon = Icons.Default.HideImage,
                            position = ListItemPosition.START,
                            trailingContent = {
                                SettingsSwitch(
                                    checked = !metadata.showSpoilers,
                                    onCheckedChange = {
                                        onMetadataSaved?.invoke(
                                            Json.encodeToString(metadata.copy(showSpoilers = !it))
                                        )
                                        viewModel.updateSavedItem()
                                    }
                                )
                            }
                        )
                    }
                    item {
                        SettingListItem(
                            title = "Hide Names",
                            description = "Override if unrated episodes will show their name",
                            icon = Icons.Default.CommentsDisabled,
                            position = ListItemPosition.END,
                            trailingContent = {
                                SettingsSwitch(
                                    checked = !spoilName,
                                    onCheckedChange = { spoilName = !it }
                                )
                            }
                        )
                    }

                    item { SettingsListHeader("Visuals") }
                    item {
                        SettingListItem(
                            title = "Cover Image Override",
                            description = "Override the cover image url that will be displayed (ideally 2:3 ratio)",
                            position = ListItemPosition.SINGLE,
                            supportingContent = {
                                SettingsTextField(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    value = coverOverride ?: "",
                                    onValueChange = { value ->
                                        coverOverride = value
                                        onCoverOverrideSaved?.invoke(value)
                                    },
                                    singleLine = false,
                                    placeholder = { Text("eg. https://example.org/image.jpg") },
                                )
                            }
                            ,
                            trailingContent = {
                                AnimatedVisibility(state.savedItem?.coverImageOverride != null) {
                                    IconButton(
                                        onClick = {
                                            coverOverride = null
                                            onCoverOverrideSaved?.invoke(null)
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, null)
                                    }
                                }
                            }
                        )
                    }
                }
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
                coverImageUrl = coverOverride ?: show.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = show.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = if (!isSaved) state.imdbRating?.normalizedRating else customRating,
                ratingVotes = if (!isSaved) state.imdbRating?.voteCount else null,
                ratingLabel = savedRank?.let { formatItemRankLabel(it, CategoryType.TMDB_SHOWS) },
                ratingColorBucketsOverride = if (!isSaved) RatingColorBucketConstants.RC_IMDB_SHOWS else getCurrentRatingColorBuckets(),
                onRatingSaved = onRatingSaved,
                review = "",
                onBackClick = onBackClick,
                canAddToLibrary = false,
                onOpenSettings = if (isSaved) {
                    { showSettings = true }
                } else null,
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
                                        showStatusSelector = true
                                    }
                                )
                            }

                            if (showStatusSelector) {
                                ModalEnumSelector(
                                    title = "Status",
                                    selectedOption = status,
                                    onOptionSelected = {
                                        status = it
                                        onStatusSaved?.invoke(it)
                                    },
                                    separatedOptions = listOf(ItemStatus.NONE),
                                    onDismiss = { showStatusSelector = false },
                                )
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
                                        onClick = {
                                            //onEpisodeClick(
                                            //    show.id,
                                            //    nextToWatchEpisode.seasonNumber,
                                            //    nextToWatchEpisode.episodeNumber
                                            //)
                                        },
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
                                        onClick = {
                                            //onEpisodeClick(
                                            //    show.id,
                                            //    episode.seasonNumber,
                                            //    episode.episodeNumber
                                            //)
                                        },
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
                                    urlBuilder = { size, path ->
                                        "https://image.tmdb.org/t/p/${when(size) {
                                            ImageSize.MEDIUM -> "w500"
                                            ImageSize.LARGE -> "original"
                                        }}${path}"
                                    },
                                    images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                    itemWidth = 110.dp,
                                    itemHeight = 180.dp,
                                    shape = MaterialTheme.shapes.large,
                                    maximizable = true,
                                    supportingContent = { url, onDismiss ->
                                        Button(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                                                coverOverride = url
                                                onCoverOverrideSaved?.invoke(url)
                                                onDismiss()
                                            },
                                            shapes = ButtonDefaults.shapes(),
                                        ) {
                                            Icon(
                                                Icons.Default.ReplayCircleFilled,
                                                contentDescription = "Override",
                                                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                                            )
                                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                                            Text(
                                                "Override",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
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
                                    urlBuilder = { size, path ->
                                        "https://image.tmdb.org/t/p/${when(size) {
                                            ImageSize.MEDIUM -> "w780"
                                            ImageSize.LARGE -> "original"
                                        }}${path}"
                                    },
                                    images.sortedBy { -it.voteCount }.map { it.toCarouselImage() },
                                    itemWidth = 240.dp,
                                    shape = MaterialTheme.shapes.large,
                                    maximizable = true,
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    // Episodes
                    item {
                        val headerName = "Episodes"
                        CollapsibleHeader(
                            headerName,
                            isOpened = headerName !in state.collapsedHeaders,
                            onClick = {
                                if (it) state.collapsedHeaders.remove(headerName)
                                else state.collapsedHeaders.add(headerName)
                            }
                        ) {
                            Column {
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
                                ChildrenDisplay(
                                    childrenGroups = childrenGroups,
                                    onChildClick = onChildClick,
                                    columnText = { "S$it" },
                                    rowText = { "E$it" },
                                    selectedDisplayMode = state.selectedDisplayMode,
                                    onDisplayModeSelect = viewModel::onDisplayModeSelect,
                                    selectedSortMode = state.selectedSortMode,
                                    onSortModeSelect = viewModel::onSortModeSelect,
                                    expandedParents = state.expandedSeasons,
                                    isLoading = episodesState.isLoadingEpisodes,
                                    spoilers = spoilers,
                                    spoilName = spoilName,
                                )
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
                                    items(reviews.sortedByDescending { parseDate(it.updatedAt) }, key = { it.id }) { review ->
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