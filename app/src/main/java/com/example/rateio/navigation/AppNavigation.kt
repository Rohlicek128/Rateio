package com.example.rateio.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
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
import com.example.rateio.features.rating.RateItemDetail


@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            val isDetailScreen = currentDestination?.hasRoute<Route.RateItemDetail>() == true
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
            composable<Route.TopLevel.Home> { HomeScreen(
                contentPadding = globalPadding,
                onItemClick = { id ->
                    navController.navigate(Route.RateItemDetail(id.toString(), ""))
                })
            }
            composable<Route.TopLevel.Browse> { Text("Browse") }
            composable<Route.TopLevel.Profile> { Text("Profile") }

            composable<Route.RateItemDetail> { backStackEntry ->
                val detail = backStackEntry.toRoute<Route.RateItemDetail>()
                RateItemDetail(
                    detail.itemId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

}

fun NavHostController.navigateSingleTop(route: Any) {
    this.navigate(route) {
        popUpTo(this@navigateSingleTop.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}