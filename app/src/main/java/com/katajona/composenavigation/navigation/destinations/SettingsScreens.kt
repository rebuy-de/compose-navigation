package com.katajona.composenavigation.navigation.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.katajona.composenavigation.R
import com.katajona.composenavigation.navigation.BottomNavItem
import com.katajona.composenavigation.navigation.NavigationDestination
import com.katajona.composenavigation.screens.DialogScreen
import com.katajona.composenavigation.screens.RegisterScreen
import com.katajona.composenavigation.screens.SettingsScreen


object SettingsScreens {
    data object Settings :
        NavRoute, BottomNavItem(R.string.settings, Icons.Default.Settings) {
        override val screen = NavGraphView.ComposableGraphView(this) {
            SettingsScreen()
        }

        fun get(): NavigationDestination = getRouteWithParams()

    }

    data object Register : NavRoute {
        override val screen = NavGraphView.BottomSheetGraphView(this) {
            RegisterScreen()
        }

        fun get(): NavigationDestination = getRouteWithParams()


    }

    data object Dialog : NavRoute {
        private const val textParam = "text"
        override val screen = NavGraphView.DialogGraphView(this) { backStackEntry ->
            DialogScreen(backStackEntry.arguments?.getString(textParam) ?: "")
        }
        override val arguments: List<NamedNavArgument>
            get() = listOf(
                navArgument(textParam) {
                    type = NavType.StringType
                },
            )

        fun get(title: String): NavigationDestination = getRouteWithParams(
            textParam to title,
        )

    }
}