package com.katajona.composenavigation.navigation

import androidx.navigation.NavGraphBuilder
import com.katajona.composenavigation.navigation.destinations.NavRoute

data class NavigationDestination(val route: String)

fun NavGraphBuilder.registerScreens() {
    NavRoute::class.sealedSubclasses.forEach { it.objectInstance?.getScreen(this) }
}