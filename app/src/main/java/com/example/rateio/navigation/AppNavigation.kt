package com.example.rateio.navigation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.rateio.features.home.HomeScreen
import com.example.rateio.model.CategoryType
import com.example.rateio.presentation.browse.BrowseScreen
import com.example.rateio.presentation.category.LibraryCategoryScreen
import com.example.rateio.presentation.profile.ProfileScreen
import com.example.rateio.presentation.rating.SavedRateItemScreen
import com.example.rateio.presentation.rating.steam.SteamGameDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbEpisodeDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbMovieDetailScreen
import com.example.rateio.presentation.rating.tmdb.TmdbShowDetailScreen


@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            val isDetailScreen = currentDestination?.hasRoute<Route.TopLevel.Home>() == false &&
                    !currentDestination.hasRoute<Route.TopLevel.Browse>() &&
                    !currentDestination.hasRoute<Route.TopLevel.Profile>()
            val isVisible = !isDetailScreen && currentDestination != null

            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .align(Alignment.BottomCenter)
                            .zIndex(2f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.background,
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY,
                                )
                            )
                    )

                    NavigationBar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(3f)
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
                            label = { Text("Ratings", fontWeight = FontWeight.Bold) }
                        )
                        // Browse
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<Route.TopLevel.Browse>() == true,
                            onClick = { navController.navigateSingleTop(Route.TopLevel.Browse) },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Browse", fontWeight = FontWeight.Bold) }
                        )
                        // Settings
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<Route.TopLevel.Profile>() == true,
                            onClick = { navController.navigateSingleTop(Route.TopLevel.Profile) },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("Profile", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

        }
    ) { globalPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.TopLevel.Home,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable<Route.TopLevel.Home> {
                HomeScreen(
                    contentPadding = globalPadding,
                    onItemClick = { id -> navController.navigate(Route.CategoryDetail(id)) }
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
                            else -> {}
                        }
                    }
                )
            }

            composable<Route.TopLevel.Profile> {
                ProfileScreen(
                    onOpenSettings = {

                    }
                )
            }


            composable<Route.TmdbShowDetail> { back ->
                val route = back.toRoute<Route.TmdbShowDetail>()
                TmdbShowDetailScreen(
                    showId = route.showId,
                    isSaved = false,
                    onBackClick = { navController.popBackStack() },
                    onEpisodeClick = {showId, seasonNumber, episodeNumber ->
                        navController.navigate(Route.TmdbEpisodeDetail(showId, seasonNumber, episodeNumber)) {
                            popUpTo<Route.TmdbShowDetail>()
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
                )
            }

            composable<Route.SteamGameDetail> { back ->
                val route = back.toRoute<Route.SteamGameDetail>()
                SteamGameDetailScreen(
                    appId = route.appId,
                    onBackClick = { navController.popBackStack() },
                )
            }


            composable<Route.CategoryDetail> { back ->
                val route = back.toRoute<Route.CategoryDetail>()
                LibraryCategoryScreen(
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
                    onBackClick = { navController.popBackStack() },
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