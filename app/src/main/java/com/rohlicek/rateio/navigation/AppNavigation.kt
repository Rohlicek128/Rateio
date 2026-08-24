package com.rohlicek.rateio.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.rohlicek.rateio.data.remote.imdb.ManualSyncWorker
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.rohlicek.rateio.data.remote.tmdb.TmdbRepository
import com.rohlicek.rateio.features.home.HomeScreen
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.presentation.browse.BrowseScreen
import com.rohlicek.rateio.presentation.category.EnhancedCategoryScreen
import com.rohlicek.rateio.presentation.components.RatingsSyncToast
import com.rohlicek.rateio.presentation.leaderboard.LeaderboardScreen
import com.rohlicek.rateio.presentation.leaderboard.TmdbLeaderboardScreen
import com.rohlicek.rateio.presentation.profile.ProfileScreen
import com.rohlicek.rateio.presentation.rating.SavedRateItemScreen
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucketConstants
import com.rohlicek.rateio.presentation.rating.display.RatingTransformationsConstants
import com.rohlicek.rateio.presentation.rating.openlibrary.OLWorkDetailScreen
import com.rohlicek.rateio.presentation.rating.steam.SteamGameDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbEpisodeDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbMovieDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbPersonDetailScreen
import com.rohlicek.rateio.presentation.rating.tmdb.TmdbShowDetailScreen
import com.rohlicek.rateio.presentation.settings.SettingsAboutScreen
import com.rohlicek.rateio.presentation.settings.SettingsAppearanceScreen
import com.rohlicek.rateio.presentation.settings.SettingsCategoriesScreen
import com.rohlicek.rateio.presentation.settings.SettingsDatabaseScreen
import com.rohlicek.rateio.presentation.settings.SettingsRatingScreen
import com.rohlicek.rateio.presentation.settings.SettingsScreen
import com.rohlicek.rateio.presentation.settings.rating.RatingColorSettingsScreen
import com.rohlicek.rateio.presentation.settings.rating.RatingTransformationSettingsScreen
import com.rohlicek.rateio.ui.theme.AppTheme
import kotlinx.serialization.json.Json


@Composable
fun AppNavigation(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current

    val tmdbRepository = remember { TmdbRepository() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDetailScreen = currentDestination?.hasRoute<Route.TopLevel.Home>() == false &&
            !currentDestination.hasRoute<Route.TopLevel.Leaderboard>() &&
            !currentDestination.hasRoute<Route.TopLevel.Browse>() &&
            !currentDestination.hasRoute<Route.TopLevel.Profile>()
    val isNavBarVisible = !isDetailScreen && currentDestination != null

    val transitionMillis = 400

    val colors = NavigationBarItemDefaults.colors().copy(
        //selectedIndicatorColor = NavigationBarItemDefaults.colors().unselectedIconColor,
        //selectedIconColor = MaterialTheme.colorScheme.surfaceContainerLow,
        selectedIndicatorColor = MaterialTheme.colorScheme.primary,
        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        unselectedTextColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    )
    val style = MaterialTheme.typography.bodySmall


    val navBarOffset by animateDpAsState(
        targetValue = if (isNavBarVisible) 104.dp else 28.dp,
        label = "navBarOffset"
    )

    val workManager = remember { WorkManager.getInstance(context) }
    val workInfos by workManager.getWorkInfosForUniqueWorkFlow("manual_imdb_sync")
        .collectAsState(initial = emptyList())

    val activeWork = workInfos.firstOrNull()
    val syncRunning = activeWork?.state == WorkInfo.State.RUNNING
    val syncProgress = activeWork?.progress?.getInt("PROGRESS", 0) ?: 0


    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = isNavBarVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 32.dp,
                                topEnd = 32.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<Route.TopLevel.Home>() == true,
                        onClick = { navController.navigateSingleTop(Route.TopLevel.Home) },
                        icon = { Icon(Icons.Default.LibraryAdd, contentDescription = null) },
                        label = { Text("Ratings", fontWeight = FontWeight.Bold, style = style) },
                        colors = colors,
                    )
                    // Leaderboard
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<Route.TopLevel.Leaderboard>() == true,
                        onClick = { navController.navigateSingleTop(Route.TopLevel.Leaderboard) },
                        icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                        label = { Text("Leaderboard", fontWeight = FontWeight.Bold, style = style) },
                        colors = colors,
                    )
                    // Browse
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<Route.TopLevel.Browse>() == true,
                        onClick = { navController.navigateSingleTop(Route.TopLevel.Browse) },
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Browse", fontWeight = FontWeight.Bold, style = style) },
                        colors = colors,
                    )
                    // Settings
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute<Route.TopLevel.Profile>() == true,
                        onClick = { navController.navigateSingleTop(Route.TopLevel.Profile) },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profile", fontWeight = FontWeight.Bold, style = style) },
                        colors = colors,
                    )
                }
            }

        }
    ) { globalPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Route.TopLevel.Home,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    enterTransition()
                },
                exitTransition = {
                    exitTransition()
                },
                popEnterTransition = {
                    popEnterTransition()
                },
                popExitTransition = {
                    popExitTransition()
                },
            ) {
                composable<Route.TopLevel.Home> {
                    HomeScreen(
                        contentPadding = globalPadding,
                        onItemClick = { id -> navController.navigate(Route.CategoryDetail(id)) },
                        onOpenSettings = {
                            navController.navigate(Route.SettingsLevel.SettingsTop)
                        }
                    )
                }

                composable<Route.TopLevel.Leaderboard> {
                    LeaderboardScreen(
                        contentPadding = globalPadding,
                        onItemClick = { item -> navController.navigate(Route.RateItemDetail(item.id)) }
                    )
                }

                composable<Route.TopLevel.Browse> {
                    BrowseScreen(
                        contentPadding = globalPadding,
                        onItemClick = { externalId, type ->
                            when (type) {
                                CategoryType.TMDB_SHOWS  ->
                                    navController.navigate(Route.TmdbShowDetail(externalId.toInt()))
                                CategoryType.TMDB_MOVIES ->
                                    navController.navigate(Route.TmdbMovieDetail(externalId.toInt()))
                                CategoryType.STEAM_GAMES ->
                                    navController.navigate(Route.SteamGameDetail(externalId))
                                CategoryType.OPEN_LIBRARY_BOOKS ->
                                    navController.navigate(Route.OLWorkDetail(externalId))
                                else -> {}
                            }
                        },
                        onTopRatedClick = { category ->
                            navController.navigate(Route.TmdbLeaderboard(category)) {
                                popUpTo<Route.TopLevel.Browse>()
                            }
                        }
                    )
                }

                composable<Route.TmdbLeaderboard> { back ->
                    val route = back.toRoute<Route.TmdbLeaderboard>()
                    TmdbLeaderboardScreen(
                        category = route.category,
                        onItemClick = { tmdbId ->
                            when (route.category) {
                                CategoryType.TMDB_SHOWS  ->
                                    navController.navigate(Route.TmdbShowDetail(tmdbId))
                                CategoryType.TMDB_MOVIES ->
                                    navController.navigate(Route.TmdbMovieDetail(tmdbId))
                                else -> {}
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable<Route.TopLevel.Profile> {
                    ProfileScreen(
                        onOpenSettings = {
                            navController.navigate(Route.SettingsLevel.SettingsTop)
                        }
                    )
                }

                composable<Route.SettingsLevel.SettingsTop> {
                    SettingsScreen(
                        onAppearanceClick = {
                            navController.navigate(Route.SettingsLevel.Appearance) {
                                popUpTo<Route.SettingsLevel.SettingsTop>()
                            }
                        },
                        onRatingClick = {
                            navController.navigate(Route.SettingsLevel.Rating) {
                                popUpTo<Route.SettingsLevel.SettingsTop>()
                            }
                        },
                        onDatabaseClick = {
                            navController.navigate(Route.SettingsLevel.Database) {
                                popUpTo<Route.SettingsLevel.SettingsTop>()
                            }
                        },
                        onCategoriesClick = {
                            navController.navigate(Route.SettingsLevel.Categories) {
                                popUpTo<Route.SettingsLevel.SettingsTop>()
                            }
                        },
                        onAboutClick = {
                            navController.navigate(Route.SettingsLevel.About) {
                                popUpTo<Route.SettingsLevel.SettingsTop>()
                            }
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.Rating> {
                    SettingsRatingScreen(
                        onRatingTransformationClick = {
                            navController.navigate(Route.SettingsLevel.RatingTransformation)
                        },
                        onRatingColorClick = {
                            navController.navigate(Route.SettingsLevel.RatingColor)
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.Appearance> {
                    SettingsAppearanceScreen(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.RatingTransformation> {
                    RatingTransformationSettingsScreen(
                        onSave = {
                            RatingTransformationsConstants.currentTransformation = it
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.RatingColor> {
                    RatingColorSettingsScreen(
                        onSave = {
                            RatingColorBucketConstants.currentBuckets = it
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.Database> {
                    SettingsDatabaseScreen(
                        syncRunning = syncRunning,
                        syncProgress = syncProgress,
                        onSyncRequest = {
                            val syncRequest = OneTimeWorkRequestBuilder<ManualSyncWorker>()
                                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                .build()

                            workManager.enqueueUniqueWork(
                                "manual_imdb_sync",
                                ExistingWorkPolicy.REPLACE,
                                syncRequest
                            )
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.Categories> {
                    SettingsCategoriesScreen(
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.About> {
                    SettingsAboutScreen(
                        onBackClick = { navController.popBackStack() },
                    )
                }


                composable<Route.TmdbShowDetail> { back ->
                    val route = back.toRoute<Route.TmdbShowDetail>()
                    TmdbShowDetailScreen(
                        showId = route.showId,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                        onEpisodeClick = { _, episodeItem, _ ->
                            val metadata = episodeItem.metadataJSON?.let {
                                runCatching {
                                    Json.decodeFromString<TmdbEpisodeMetadata>(it)
                                }.getOrNull()
                            }
                            if (metadata != null) {
                                navController.navigate(Route.TmdbEpisodeDetail(
                                    showId = metadata.showId,
                                    season = metadata.seasonNumber,
                                    episode = metadata.episodeNumber,
                                    seasonEpisodeCount = metadata.seasonEpisodeCount,
                                )) {
                                    popUpTo<Route.TmdbShowDetail>()
                                }
                            }
                        }
                    )
                }
                composable<Route.TmdbEpisodeDetail> { back ->
                    val route = back.toRoute<Route.TmdbEpisodeDetail>()
                    TmdbEpisodeDetailScreen(
                        showId = route.showId,
                        season = route.season,
                        episode = route.episode,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                        seasonEpisodeCount = route.seasonEpisodeCount,
                        onNextClick = { nextSeason, nextEpisode, nextEpisodeCount ->
                            navController.navigate(Route.TmdbEpisodeDetail(
                                route.showId,
                                nextSeason,
                                nextEpisode,
                                nextEpisodeCount,
                            )) {
                                popUpTo<Route.TmdbShowDetail>()
                            }
                        },
                        onPreviousClick = { prevSeason, prevEpisode, prevEpisodeCount ->
                            navController.navigate(Route.TmdbEpisodeDetail(
                                route.showId,
                                prevSeason,
                                prevEpisode,
                                prevEpisodeCount,
                            )) {
                                popUpTo<Route.TmdbShowDetail>()
                            }
                        },
                    )
                }

                composable<Route.TmdbMovieDetail> { back ->
                    val route = back.toRoute<Route.TmdbMovieDetail>()
                    TmdbMovieDetailScreen(
                        movieId = route.movieId,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                        onPersonClick = { personId ->
                            navController.navigate(Route.TmdbPersonDetail(personId))
                        }
                    )
                }

                composable<Route.TmdbPersonDetail> { back ->
                    val route = back.toRoute<Route.TmdbPersonDetail>()
                    TmdbPersonDetailScreen (
                        personId = route.personId,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                    )
                }


                composable<Route.SteamGameDetail> { back ->
                    val route = back.toRoute<Route.SteamGameDetail>()
                    SteamGameDetailScreen(
                        appId = route.appId,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable<Route.OLWorkDetail> { back ->
                    val route = back.toRoute<Route.OLWorkDetail>()
                    OLWorkDetailScreen(
                        workId = route.workId,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                        onChapterClick = { partItem, chapterItem ->

                        }
                    )
                }


                composable<Route.CategoryDetail> { back ->
                    val route = back.toRoute<Route.CategoryDetail>()
                    EnhancedCategoryScreen (
                        categoryId = route.categoryId,
                        onItemClick = { item ->
                            navController.navigate(Route.RateItemDetail(item.id))
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable<Route.RateItemDetail> { backStackEntry ->
                    val detail = backStackEntry.toRoute<Route.RateItemDetail>()
                    SavedRateItemScreen(
                        itemId = detail.itemId,
                        tmdbRepository = tmdbRepository,
                        onChildClick = { childId, parentId ->
                            navController.navigate(Route.RateItemDetail(childId)) {
                                popUpTo(Route.RateItemDetail(parentId))
                            }
                        },
                        onPersonClick = { personId ->
                            navController.navigate(Route.TmdbPersonDetail(personId))
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }
            }

            AnimatedVisibility(
                visible = isNavBarVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.background,
                                )
                            )
                        )
                )
            }

            RatingsSyncToast(
                isVisible = syncRunning,
                progress = syncProgress,
                onClick = {
                    val isAlreadyThere = navController.currentDestination?.hasRoute<Route.SettingsLevel.Database>() == true
                    if (!isAlreadyThere) {
                        navController.navigate(Route.SettingsLevel.Database)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarOffset)
            )
        }
    }

}

fun NavHostController.navigateSingleTop(route: Any) {
    this.navigate(route) {
        popUpTo(this@navigateSingleTop.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}


// Taken from PixelPlayer - com.theveloper.pixelplay.presentation.navigation


// Base duration for bottom-nav switches at 1x — at 0.5x system scale = ~190 ms.
private const val BOTTOM_NAV_TRANSITION_DURATION = 500

// MD3 Expressive easing for bottom-nav switches
private val BottomNavEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

private val MAIN_ROOT_TRANSITION_SPEC =
    tween<IntOffset>(durationMillis = BOTTOM_NAV_TRANSITION_DURATION, easing = BottomNavEasing)

private val MAIN_ROOT_FADE_SPEC =
    tween<Float>(durationMillis = BOTTOM_NAV_TRANSITION_DURATION / 2, easing = BottomNavEasing)


private val EmphasizedDecelerateEasing = CubicBezierEasing(0.2f, 0.85f, 0.7f, 1f)

// Accelerate for elements leaving the screen
private val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Base duration designed for 1x animation scale — looks good at full speed,
// still smooth at 0.5x (system halves it to ~225 ms).
const val TRANSITION_DURATION = 450

fun enterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { (it * 0.5f).toInt() }
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.92f,
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing)
)

// Push: Exit to Left — recedes 25% (parallax, barely moves)
fun exitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { -(it * 0.25f).toInt() }
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing)
)

// Pop: Enter from Left — parallax slide-in 25% + subtle scale up
fun popEnterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { -(it * 0.25f).toInt() }
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.95f
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedDecelerateEasing)
)

// Pop: Exit to Right — slides out 50% + slight scale down
fun popExitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { (it * 0.5f).toInt() }
) + scaleOut(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetScale = 0.92f,
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing)
)