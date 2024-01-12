package com.katajona.composenavigation.navigation.destinations

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import com.katajona.composenavigation.navigation.NavigationDestination

private const val uri = "deeplink://"

sealed interface NavRoute {

    val route: Route

    fun getArguments(): List<NamedNavArgument> = listOf()

    fun getScreen(navGraph: NavGraphBuilder)

    fun getDeepLinks(): List<NavDeepLink> {
        return route.deepLinks?.map { navDeepLink { uriPattern = "$uri${it}" } } ?: listOf()
    }
}

class Route(baseRoute: NavRoute, vararg parameters: String, val deepLinks: List<String>? = null) {
    val routeWithParams: String
    private val routeName = baseRoute::class.java.canonicalName?.split(".")?.takeLast(2)?.joinToString(".") ?: ""

    init {
        this.routeWithParams = if (parameters.isNotEmpty()) {
            "$routeName?" + parameters.joinToString("&") { parameter ->
                "$parameter={$parameter}"
            }
        } else {
            routeName
        }
    }


    fun getRouteWithParams(vararg parameters: Pair<String, String?>): NavigationDestination {
        val route = if (parameters.isNotEmpty()) {
            "$routeName?" + parameters.filter { it.second != null }.joinToString("&") { item ->
                "${item.first}=${item.second}"
            }
        } else routeName

        return NavigationDestination(route)
    }
}
