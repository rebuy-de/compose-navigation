package com.katajona.composenavigation.navigation.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.katajona.composenavigation.R
import com.katajona.composenavigation.navigation.BottomNavItem
import com.katajona.composenavigation.navigation.NavigationDestination
import com.katajona.composenavigation.screens.ExtraDataScreen
import com.katajona.composenavigation.screens.HomeScreen

object HomeScreens {
    data object Home :
        NavRoute, BottomNavItem(R.string.home, Icons.Default.Home) {
        override val screen = NavGraphView.ComposableGraphView(this) {
            HomeScreen()
        }
        fun get(): NavigationDestination = getRouteWithParams()

    }

    data object ExtraData : NavRoute {
        private const val textParam = "text"
        private const val countParam = "count"

        override val screen = NavGraphView.ComposableGraphView(this) { backStackEntry ->
            ExtraDataScreen(
                backStackEntry.arguments?.getString(textParam) ?: "",
                backStackEntry.arguments?.getInt(countParam) ?: 0
            )
        }
        override val deepLink: List<String>
            get() = listOf(
                "extra?text={$textParam}&count={$countParam}",
                "extraData/{$textParam}?amount={$countParam}"
            )
        override val arguments: List<NamedNavArgument>
            get() = listOf(
                navArgument(textParam) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(countParam) {
                    type = NavType.IntType
                }
            )

        fun get(title: String?, count: Int): NavigationDestination = getRouteWithParams(
            textParam to title,
            countParam to count.toString()
        )

    }
}