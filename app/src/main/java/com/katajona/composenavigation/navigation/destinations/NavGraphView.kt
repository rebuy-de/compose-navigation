package com.katajona.composenavigation.navigation.destinations

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet

sealed class NavGraphView {
    data class BottomSheetGraphView(
        val navRoute: NavRoute,
        val content: @Composable ColumnScope.(backstackEntry: NavBackStackEntry) -> Unit
    ) : NavGraphView() {
        @OptIn(ExperimentalMaterialNavigationApi::class)
        override fun build(navGraphBuilder: NavGraphBuilder) {
            navGraphBuilder.bottomSheet(
                route = navRoute.getRouteUrlWithParams(),
                arguments = navRoute.arguments,
                deepLinks = navRoute.absoluteDeepLinks,
                content = content
            )
        }
    }

    data class ComposableGraphView(
        val navRoute: NavRoute,
        val content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
    ) : NavGraphView() {
        override fun build(navGraphBuilder: NavGraphBuilder) {
            navGraphBuilder.composable(
                route = navRoute.getRouteUrlWithParams(),
                arguments = navRoute.arguments,
                deepLinks = navRoute.absoluteDeepLinks,
                content = content
            )
        }
    }

    data class DialogGraphView(
        val navRoute: NavRoute,
        val content: @Composable (NavBackStackEntry) -> Unit
    ) : NavGraphView() {
        override fun build(navGraphBuilder: NavGraphBuilder) {
            navGraphBuilder.dialog(
                route = navRoute.getRouteUrlWithParams(),
                arguments = navRoute.arguments,
                deepLinks = navRoute.absoluteDeepLinks,
                content = content
            )
        }
    }

    abstract fun build(navGraphBuilder: NavGraphBuilder)
}