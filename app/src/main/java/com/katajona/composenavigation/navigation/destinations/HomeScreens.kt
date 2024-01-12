package com.katajona.composenavigation.navigation.destinations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.katajona.composenavigation.R
import com.katajona.composenavigation.navigation.BottomNavItem
import com.katajona.composenavigation.navigation.NavigationDestination
import com.katajona.composenavigation.navigation.composable
import com.katajona.composenavigation.screens.ExtraDataScreen
import com.katajona.composenavigation.screens.HomeScreen

object HomeScreens {
    data object Home :
        NavRoute, BottomNavItem(R.string.home, Icons.Default.Home) {
        override val route = Route(this)

        fun get(): NavigationDestination = route.getRouteWithParams()
        override fun getScreen(navGraph: NavGraphBuilder) {
            navGraph.composable(this) {
                HomeScreen()
            }
        }
    }

    data object ExtraData : NavRoute {
        private const val textParam = "text"
        private const val countParam = "count"
        override val route = Route(
            this,
            textParam,
            countParam,
            deepLinks = listOf(
                "extra?text={$textParam}&count={$countParam}",
                "extraData/{$textParam}?amount={$countParam}"
            )
        )

        fun get(title: String?, count: Int): NavigationDestination = route.getRouteWithParams(
            textParam to title,
            countParam to count.toString()
        )

        override fun getArguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(textParam) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(countParam) {
                    type = NavType.IntType
                }
            )
        }

        override fun getScreen(navGraph: NavGraphBuilder) {
            navGraph.composable(this) { backStackEntry ->
                ExtraDataScreen(
                    backStackEntry.arguments?.getString(textParam) ?: "",
                    backStackEntry.arguments?.getInt(countParam) ?: 0
                )
            }
        }
    }

}