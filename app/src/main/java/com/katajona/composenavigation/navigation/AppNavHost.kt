package com.katajona.composenavigation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.katajona.composenavigation.navigation.destinations.HomeScreens
import com.katajona.composenavigation.navigation.destinations.NavRoute
import com.katajona.composenavigation.navigation.destinations.SettingsScreens
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String,
    router: Router = koinInject()
) {

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        registerScreens()
    }
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            router.dispatch(NavigationType.Snackbar(destination.route ?: ""))
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.bottomSheet(
    navRoute: NavRoute,
    content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit,
) {
    bottomSheet(
        route = navRoute.route.routeWithParams,
        arguments = navRoute.getArguments(),
        deepLinks = navRoute.getDeepLinks(),
        content = content
    )
}

fun NavGraphBuilder.composable(
    navRoute: NavRoute,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = navRoute.route.routeWithParams,
        arguments = navRoute.getArguments(),
        deepLinks = navRoute.getDeepLinks(),
        content = content
    )
}

fun NavGraphBuilder.dialog(
    navRoute: NavRoute,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    dialog(
        route = navRoute.route.routeWithParams,
        arguments = navRoute.getArguments(),
        deepLinks = navRoute.getDeepLinks(),
        content = content
    )
}


@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(HomeScreens.Home, SettingsScreens.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val selected = items.find { it.route.routeWithParams == currentDestination?.route } ?: return
    RbBottomNavigation(
        items = items, selectedItem = selected, onItemSelectionChanged = { item ->
            navController.navigate((item as NavRoute).route.routeWithParams) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
            }
        }
    )
}
