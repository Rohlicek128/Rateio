package com.example.rateio.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.rateio.features.home.HomeScreen
import com.example.rateio.features.rating.CategoryDetailScreen
import com.example.rateio.features.rating.RateItemDetailScreen
import com.example.rateio.features.settings.SettingsScreen
import com.example.rateio.presentation.browse.BrowseScreen
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
                ) {
                    NavigationBar(
                        modifier = Modifier.clip(
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
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Ratings") }
                        )
                        // Browse
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<Route.TopLevel.Browse>() == true,
                            onClick = { navController.navigateSingleTop(Route.TopLevel.Browse) },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Browse") }
                        )
                        // Settings
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute<Route.TopLevel.Profile>() == true,
                            onClick = { navController.navigateSingleTop(Route.TopLevel.Profile) },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text("Profile") }
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
                    onShowClick = { showId ->
                        navController.navigate(Route.TmdbShowDetail(showId))
                    }
                )
            }
            composable<Route.TmdbShowDetail> { back ->
                val route = back.toRoute<Route.TmdbShowDetail>()
                TmdbShowDetailScreen(
                    showId = route.showId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<Route.TopLevel.Profile> { SettingsScreen(contentPadding = globalPadding) }


            composable<Route.CategoryDetail> { back ->
                val route = back.toRoute<Route.CategoryDetail>()
                CategoryDetailScreen(
                    categoryId = route.categoryId,
                    onBackClick = { navController.popBackStack() },
                    onItemClick = { itemId -> navController.navigate(Route.RateItemDetail(itemId)) }
                )
            }

            composable<Route.RateItemDetail> { backStackEntry ->
                val detail = backStackEntry.toRoute<Route.RateItemDetail>()
                RateItemDetailScreen(
                    itemId = detail.itemId,
                    onBackClick = { navController.popBackStack() },
                    onChildClick = { childId -> navController.navigate(Route.RateItemDetail(childId)) }
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