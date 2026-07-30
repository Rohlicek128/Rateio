package com.rohlicek.rateio.presentation.rating.tmdb

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.db.RateioDatabase
import com.rohlicek.rateio.data.remote.imdb.ImdbRatingRepository
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.rohlicek.rateio.data.remote.tmdb.TmdbShowMetadata
import com.rohlicek.rateio.data.remote.tmdb.toCarouselImage
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import com.rohlicek.rateio.model.computeAggregateChildrenRating
import com.rohlicek.rateio.model.computeAggregateRating
import com.rohlicek.rateio.model.computeWeightedRating
import com.rohlicek.rateio.presentation.components.AdaptiveImageCarousel
import com.rohlicek.rateio.presentation.components.CollapsibleHeader
import com.rohlicek.rateio.presentation.components.ConnectedButtonsExpressive
import com.rohlicek.rateio.presentation.components.DateProgressBar
import com.rohlicek.rateio.presentation.components.GenreChips
import com.rohlicek.rateio.presentation.components.HeroCarousel
import com.rohlicek.rateio.presentation.components.ImageSize
import com.rohlicek.rateio.presentation.components.ItemRatingStatCard
import com.rohlicek.rateio.presentation.components.ItemStatCard
import com.rohlicek.rateio.presentation.components.ModalEnumSelector
import com.rohlicek.rateio.presentation.components.PersonCard
import com.rohlicek.rateio.presentation.components.RateItemCard
import com.rohlicek.rateio.presentation.components.ReviewCard
import com.rohlicek.rateio.presentation.components.RowButtonEnumSelector
import com.rohlicek.rateio.presentation.components.ScreenError
import com.rohlicek.rateio.presentation.components.ScreenLoading
import com.rohlicek.rateio.presentation.components.rating.ChildrenDisplay
import com.rohlicek.rateio.presentation.components.rating.DisplayMode
import com.rohlicek.rateio.presentation.components.rating.ItemProgressBar
import com.rohlicek.rateio.presentation.components.rating.getTopRatedChildren
import com.rohlicek.rateio.presentation.profile.RatingsColorBarChart
import com.rohlicek.rateio.presentation.rating.RateItemDetailScreen
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.RatingTransformationsConstants
import com.rohlicek.rateio.presentation.rating.display.getCurrentRatingColorBuckets
import com.rohlicek.rateio.presentation.rating.display.getTransformedRating
import com.rohlicek.rateio.presentation.settings.ListItemPosition
import com.rohlicek.rateio.presentation.settings.ModalSettings
import com.rohlicek.rateio.presentation.settings.SettingListItem
import com.rohlicek.rateio.presentation.settings.SettingsListHeader
import com.rohlicek.rateio.presentation.settings.SettingsSwitch
import com.rohlicek.rateio.presentation.settings.SettingsTextField
import com.rohlicek.rateio.utils.bottomShadow
import com.rohlicek.rateio.utils.formatDate
import com.rohlicek.rateio.utils.formatTime
import com.rohlicek.rateio.utils.parseDate
import kotlinx.serialization.json.Json
import java.util.Locale


@Composable
fun TmdbShowDetailScreen(
    showId: Int,
    isSaved: Boolean,
    customRating: Float? = null,
    savedRank: Int? = null,
    onRatingSaved: ((Float?) -> Unit)? = null,
    onWeightSaved: ((Float?) -> Unit)? = null,
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
    val imdbRepository = remember {
        val db = RateioDatabase.getDatabase(context)
        ImdbRatingRepository(db.imdbRatingDao())
    }

    val viewModel: TmdbShowDetailViewModel = viewModel(
        factory = TmdbShowDetailViewModel.factory(showId, categoryRepository, itemRepository, imdbRepository)
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
            var metadata by remember(state.savedItem?.metadataJSON) {
                mutableStateOf(
                    state.savedItem?.metadataJSON?.let {
                        runCatching { Json.decodeFromString<TmdbShowMetadata>(it) }.getOrNull()
                    } ?: TmdbShowMetadata()
                )
            }

            val seasons = show.seasons.filter { it.seasonNumber > 0 }.sortedBy { it.seasonNumber }
            val episodesViewModel: TmdbEpisodesViewModel = viewModel(
                factory = TmdbEpisodesViewModel.factory(
                    showId = showId,
                    seasonNumbers = seasons.map { it.seasonNumber },
                    fetchRatings = !isSaved,
                    imdbRepository = imdbRepository,
                )
            )
            val episodesState by episodesViewModel.state.collectAsState()

            var selectedRatings by remember { mutableStateOf(if (!isSaved) RatingsSource.IMDB else RatingsSource.USER) }

            val userRatings = viewModel.userRatingsState.collectAsStateWithLifecycle()
            val listOfRatings by remember(userRatings.value) {
                derivedStateOf {
                    userRatings.value.values.flatMap { it.values }.filterNotNull()
                }
            }
            if (onWeightSaved != null) {
                LaunchedEffect(listOfRatings) {
                    onWeightSaved(listOfRatings.size.toFloat())
                }
            }

            val ratings: Map<Int, Map<Int, Float?>> = remember(userRatings.value, selectedRatings, episodesState) {
                when (selectedRatings) {
                    RatingsSource.IMDB -> episodesState.imdbRatings
                    RatingsSource.TMDB -> episodesState.seasonEpisodes.mapValues { (_, episodes) ->
                        episodes.associate { it -> it.episodeNumber to it.voteAverage?.div(10f).takeIf { it != 0f } }
                    }
                    RatingsSource.USER -> userRatings.value
                }
            }

            val childrenGroups: Map<RateItem?, List<RateItem>> = remember(ratings, episodesState.seasonEpisodes, state.savedItem) {
                episodesState.seasonEpisodes.entries
                    .associate { (seasonNumber, episodes) ->
                        val items = episodes.sortedBy { it.episodeNumber }.map { episode ->
                            RateItem(
                                id = -1,
                                parentId = seasonNumber.toLong(),
                                categoryId = state.savedItem?.categoryId ?: 0,
                                title = episode.name,
                                subtitle = "Season ${episode.seasonNumber}, Episode ${episode.episodeNumber}",
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
                            rating = computeAggregateChildrenRating(items),
                            coverImageUrl = season.posterPath.let { "https://image.tmdb.org/t/p/original$it" },
                            coverImageLowUrl = season.posterPath.let { "https://image.tmdb.org/t/p/w342$it" },
                            externalId = season.id.toString(),
                            externalSource = CategoryType.TMDB_SEASONS,
                        )
                        seasonItem to items
                    }
            }
            val onChildClick = { child: RateItem ->
                val parent = childrenGroups.keys.find { it?.id == child.parentId }
                if (parent != null) {
                    onEpisodeClick(parent, child,)
                }
            }

            val ratingByAverage = true
            val showAverage = remember(childrenGroups) {
                computeAggregateRating(listOfRatings)
            }
            val showWeighted = remember(showAverage, listOfRatings.size) {
                computeWeightedRating(showAverage, listOfRatings.size)
            }
            if (isSaved && ratingByAverage && onRatingSaved != null) {
                LaunchedEffect(showAverage) {
                    onRatingSaved(showAverage)
                }
            }

            val topRatedEpisodes = remember(childrenGroups) {
                getTopRatedChildren(childrenGroups.flatMap { it.value })
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


            /*val unwatchedEpisodes = remember(childrenGroups, userRatings) {
                childrenGroups.mapValues { (season, episodes) ->
                    episodes.filter { episode ->
                        val seasonNumber = season?.title?.split(" ")?.getOrNull(1)?.toInt()
                        val episodeNumber = episode.subtitle?.split(", ")?.getOrNull(1)?.split(" ")?.getOrNull(1)?.toInt()
                        userRatings.value[seasonNumber]?.get(episodeNumber) == null
                    }
                }.flatMap { it.value }
            }*/
            val unwatchedEpisodes = episodesState.seasonEpisodes.mapValues { (season, episodes) ->
                episodes.filter { episode ->
                    userRatings.value[season]?.get(episode.episodeNumber) == null
                }
            }.flatMap { it.value }
            val nextToWatchEpisode = if (unwatchedEpisodes.isNotEmpty() && status != ItemStatus.COMPLETED)
                unwatchedEpisodes.first() else null

            val expandWatchedSeason = true
            if (isSaved && status != ItemStatus.COMPLETED && status != ItemStatus.DROPPED &&
                expandWatchedSeason && nextToWatchEpisode != null) {
                state.expandedSeasons.add("Season ${nextToWatchEpisode.seasonNumber}")
            }


            val spoilers = metadata.showSpoilers || !isSaved
            val spoilEpisode = { seasonNumber: Int, episodeNumber: Int ->
                spoilers || selectedRatings != RatingsSource.USER ||
                        (selectedRatings == RatingsSource.USER && userRatings.value[seasonNumber]?.get(episodeNumber) != null)
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
                                        metadata = metadata.copy(showSpoilers = !it)
                                        onMetadataSaved?.invoke(
                                            Json.encodeToString(metadata)
                                        )
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
                                    checked = !metadata.showSpoilersName,
                                    onCheckedChange = {
                                        metadata = metadata.copy(showSpoilersName = !it)
                                        onMetadataSaved?.invoke(
                                            Json.encodeToString(metadata)
                                        )
                                    }
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
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
                coverImageUrl = (if (!isSaved) null else coverOverride) ?: show.posterPath?.let {
                    "https://image.tmdb.org/t/p/original$it"
                },
                backdropImageUrl = show.backdropPath?.let {
                    "https://image.tmdb.org/t/p/w1280$it"
                },
                rating = when {
                    !isSaved -> state.imdbRating?.averageRating
                    ratingByAverage -> showWeighted
                    else -> customRating
                },
                ratingVotes = if (!isSaved) state.imdbRating?.numVotes else null,
                //ratingLabel = savedRank?.let { formatItemRankLabel(it, CategoryType.TMDB_SHOWS) },
                ratingLabel = showAverage?.let { "Average of ${getTransformedRating(it)}" },
                ratingColorBucketsOverride = if (!isSaved || ratingByAverage) RatingColorBucketConstants.RC_IMDB_SHOWS else getCurrentRatingColorBuckets(),
                onRatingSaved = if (!ratingByAverage) onRatingSaved else null,
                //review = if (state.savedItem != null) "" else null,
                onBackClick = onBackClick,
                savedInLibrary = state.savedItem != null,
                onChangeLibrary = { viewModel.onToggleSaved(state.show!!) },
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

                    // Ratings
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            if (isSaved) {
                                ItemRatingStatCard(
                                    rating = state.imdbRating?.averageRating,
                                    votes = state.imdbRating?.numVotes,
                                    source = "IMDb",
                                    transformationOverride = RatingTransformationsConstants.TF_IMDB,
                                    colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_SHOWS,
                                    onClickUrl = show.externalIds?.imdbId?.let { "https://www.imdb.com/title/$it" },
                                )
                            }
                            ItemRatingStatCard(
                                rating = show.voteAverage?.div(10f),
                                votes = show.voteCount,
                                source = "TMDB",
                                transformationOverride = RatingTransformationsConstants.TF_PERCENTAGE,
                                colorBucketsOverride = RatingColorBucketConstants.RC_IMDB_SHOWS,
                                onClickUrl = "https://www.themoviedb.org/tv/${show.id}",
                            )
                            if (!isSaved) {
                                ItemRatingStatCard(
                                    rating = state.savedItem?.rating,
                                    votes = null,
                                    source = "Yours",
                                    showNullVotes = false,
                                )
                            }
                        }
                    }

                    // Genres
                    if (show.genres.isNotEmpty()) {
                        item {
                            GenreChips(
                                genres = show.genres.map { it.name },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }


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
                                        spoilName = metadata.showSpoilersName,
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
                                        spoilName = metadata.showSpoilersName,
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Best Episodes
                    if (topRatedEpisodes.isNotEmpty() || episodesState.isLoadingEpisodes || episodesState.isLoadingRatings) {
                        item {
                            val headerName = "Best Episodes"
                            CollapsibleHeader(
                                headerName,
                                isOpened = headerName !in state.collapsedHeaders,
                                onClick = {
                                    if (it) state.collapsedHeaders.remove(headerName)
                                    else state.collapsedHeaders.add(headerName)
                                }
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    /*Text(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                        text = if (topRatedEpisodes.size > 1)
                                            "The top ${topRatedEpisodes.size} best rated episodes"
                                        else "The best rated episode",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        //fontWeight = FontWeight.SemiBold,
                                    )*/

                                    HeroCarousel(
                                        padding = PaddingValues(bottom = 6.dp),
                                        preferredItemWidth = 330.dp,
                                        itemHeight = 186.dp,
                                        items = topRatedEpisodes,
                                        subtitleBuilder = { it.subtitle},
                                        isLoading = episodesState.isLoadingEpisodes || episodesState.isLoadingRatings,
                                        showOrderedRank = true,
                                        autoScroll = false,
                                        loop = false,
                                        dotIndicator = true,
                                        spoilThumbnail = spoilers,
                                        spoilName = metadata.showSpoilersName,
                                        placeholderPageCount = 5,
                                        onItemClick = onChildClick,
                                    )
                                }
                            }
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

                    item { Spacer(modifier = Modifier.height(38.dp)) }

                    item {
                        CollapsibleHeader(
                            "Details",
                            isOpened = true,
                            onClick = null
                        )
                    }

                    item {
                        ConnectedButtonsExpressive(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            itemSpacing = 3.dp,
                            selectedIndex = RatingsSource.entries.indexOf(selectedRatings),
                            onSelectionChanged = {
                                selectedRatings = RatingsSource.entries[it]
                                if (selectedRatings == RatingsSource.IMDB && episodesState.imdbRatings.isEmpty() &&
                                    isSaved && !episodesState.isLoadingRatings) {
                                    episodesViewModel.fetchImdbRatings()
                                }
                            },
                            options = RatingsSource.entries.map { it.displayName },
                        )
                    }

                    item {
                        RowButtonEnumSelector(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp),
                            selectedOption = state.selectedTab,
                            onOptionSelected = { viewModel.onTabSelect(it ?: ShowTabs.EPISODES) },
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .bottomShadow(16.dp)
                        )
                    }

                    when (state.selectedTab) {
                        ShowTabs.EPISODES -> {
                            // Episodes
                            item {
                                Column {
                                    ChildrenDisplay(
                                        childrenGroups = childrenGroups,
                                        onChildClick = onChildClick,
                                        columnText = { "S$it" },
                                        rowText = { "E$it" },
                                        subtitleBuilder = { item, mode ->
                                            if (mode == DisplayMode.LIST) {
                                                item.subtitle?.split(", ")?.getOrNull(1)
                                            }
                                            else item.subtitle
                                        },
                                        selectedDisplayMode = state.selectedDisplayMode,
                                        onDisplayModeSelect = viewModel::onDisplayModeSelect,
                                        selectedSortMode = state.selectedSortMode,
                                        onSortModeSelect = viewModel::onSortModeSelect,
                                        selectedOrder = state.selectedSortOrder,
                                        onOrderChange = viewModel::onSortOrderChange,
                                        expandedParents = state.expandedSeasons,
                                        isLoading = episodesState.isLoadingEpisodes,
                                        isLoadingRatings = episodesState.isLoadingRatings,
                                        spoilers = spoilers,
                                        spoilName = metadata.showSpoilersName,
                                        showChildRatedCompletion = selectedRatings == RatingsSource.USER,
                                    )
                                }
                            }
                        }
                        ShowTabs.STATISTICS -> {
                            // Statistics
                            item {
                                val headerName = "Statistics"
                                CollapsibleHeader(
                                    headerName,
                                    isOpened = headerName !in state.collapsedHeaders,
                                    onClick = {
                                        if (it) state.collapsedHeaders.remove(headerName)
                                        else state.collapsedHeaders.add(headerName)
                                    }
                                ) {
                                    RatingsColorBarChart(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        title = "${show.name}'s Buckets",
                                        entries = childrenGroups.flatMap { it.value },
                                    )
                                }
                            }
                        }
                        ShowTabs.IMAGES -> {
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
                        }
                        ShowTabs.REVIEWS -> {
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
                        else -> {
                            item { Spacer(modifier = Modifier.height(620.dp)) }
                        }
                    }

                }
            )

        }
    }
}