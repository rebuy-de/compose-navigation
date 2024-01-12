package com.katajona.composenavigation.navigation.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.katajona.composenavigation.R
import com.katajona.composenavigation.navigation.BottomNavItem
import com.katajona.composenavigation.navigation.NavigationDestination
import com.katajona.composenavigation.navigation.bottomSheet
import com.katajona.composenavigation.navigation.composable
import com.katajona.composenavigation.navigation.dialog
import com.katajona.composenavigation.screens.DialogScreen
import com.katajona.composenavigation.screens.RegisterScreen
import com.katajona.composenavigation.screens.SettingsScreen


object SettingsScreens {
    data object Settings :
        NavRoute, BottomNavItem(R.string.settings, Icons.Default.Settings) {
        override val route = Route(this)

        fun get(): NavigationDestination = route.getRouteWithParams()
        override fun getScreen(navGraph: NavGraphBuilder) {
            navGraph.composable(this) {
                SettingsScreen()
            }
        }
    }

    data object Register : NavRoute {
        override val route = Route(this)
        fun get(): NavigationDestination = route.getRouteWithParams()
        override fun getScreen(navGraph: NavGraphBuilder) {
            navGraph.bottomSheet(this) {
                RegisterScreen()
            }
        }
    }

    data object Dialog : NavRoute {
        private const val textParam = "text"
        override val route = Route(this, textParam)

        fun get(title: String): NavigationDestination = route.getRouteWithParams(
            textParam to title,
        )

        override fun getArguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(textParam) {
                    type = NavType.StringType
                },
            )
        }

        fun get(): NavigationDestination = route.getRouteWithParams()
        override fun getScreen(navGraph: NavGraphBuilder) {
            navGraph.dialog(this) { backStackEntry ->
                DialogScreen(backStackEntry.arguments?.getString(textParam) ?: "")
            }
        }
    }
}