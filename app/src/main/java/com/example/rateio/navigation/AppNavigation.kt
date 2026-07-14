package com.example.rateio.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.example.rateio.features.home.HomeScreen
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.browse.BrowseScreen
import com.example.rateio.presentation.category.EnhancedCategoryScreen
import com.example.rateio.presentation.leaderboard.LeaderboardScreen
import com.example.rateio.presentation.profile.ProfileScreen
import com.example.rateio.presentation.rating.SavedRateItemScreen
import com.example.rateio.presentation.rating.display.RatingTransformationsConstants
import com.example.rateio.presentation.rating.display.getRatingColor
import com.example.rateio.presentation.rating.openlibrary.OLWorkDetailScreen
import com.example.rateio.presentation.rating.steam.SteamGameDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbEpisodeDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbMovieDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbPersonDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbShowDetailScreen
import com.example.rateio.presentation.settings.SettingsAppearanceScreen
import com.example.rateio.presentation.settings.SettingsCategoriesScreen
import com.example.rateio.presentation.settings.SettingsDatabaseScreen
import com.example.rateio.presentation.settings.SettingsRatingScreen
import com.example.rateio.presentation.settings.SettingsScreen
import com.example.rateio.presentation.settings.rating.RatingTransformationSettingsScreen
import com.example.rateio.ui.theme.AppTheme
import kotlinx.serialization.json.Json


@Composable
fun AppNavigation(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDetailScreen = currentDestination?.hasRoute<Route.TopLevel.Home>() == false &&
            !currentDestination.hasRoute<Route.TopLevel.Leaderboard>() &&
            !currentDestination.hasRoute<Route.TopLevel.Browse>() &&
            !currentDestination.hasRoute<Route.TopLevel.Profile>()
    val isVisible = !isDetailScreen && currentDestination != null

    val transitionMillis = 400

    val rateColors = getRatingColor(1f)
    val colors = NavigationBarItemDefaults.colors().copy(
        selectedIndicatorColor = rateColors.backgroundColor,
        selectedIconColor = rateColors.foregroundColor,
        selectedTextColor = rateColors.foregroundColor,
        unselectedTextColor = MaterialTheme.colorScheme.tertiaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.tertiaryContainer,
    )
    val style = MaterialTheme.typography.bodySmall

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = isVisible,
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
                enterTransition = { slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(
                        transitionMillis
                    )
                ) },
                exitTransition = { slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(
                        transitionMillis
                    )
                ) },
                popEnterTransition = { slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(
                        transitionMillis
                    )
                ) },
                popExitTransition = { slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(
                        transitionMillis
                    )
                ) }
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
                        }
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
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.Rating> {
                    SettingsRatingScreen(
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
                composable<Route.SettingsLevel.Database> {
                    SettingsDatabaseScreen(
                        onBackClick = { navController.popBackStack() },
                    )
                }
                composable<Route.SettingsLevel.Categories> {
                    SettingsCategoriesScreen(
                        onRatingTransformationClick = {
                            navController.navigate(Route.SettingsLevel.RatingTransformation)
                        },
                        onBackClick = { navController.popBackStack() },
                    )
                }


                composable<Route.TmdbShowDetail> { back ->
                    val route = back.toRoute<Route.TmdbShowDetail>()
                    TmdbShowDetailScreen(
                        showId = route.showId,
                        isSaved = false,
                        onBackClick = { navController.popBackStack() },
                        onEpisodeClick = { _, episodeItem ->
                            val metadata = episodeItem.metadataJSON?.let {
                                runCatching {
                                    Json.decodeFromString<TmdbEpisodeMetadata>(it)
                                }.getOrNull()
                            }
                            if (metadata != null) {
                                navController.navigate(Route.TmdbEpisodeDetail(
                                    metadata.showId,
                                    metadata.seasonNumber,
                                    metadata.episodeNumber
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
                        onNextClick = { nextSeason, nextEpisode ->
                            navController.navigate(Route.TmdbEpisodeDetail(route.showId, nextSeason, nextEpisode)) {
                                popUpTo<Route.TmdbShowDetail>()
                            }
                        },
                        onPreviousClick = { prevSeason, prevEpisode ->
                            navController.navigate(Route.TmdbEpisodeDetail(route.showId, prevSeason, prevEpisode)) {
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
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
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