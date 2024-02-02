package com.katajona.composenavigation.navigation.destinations

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.katajona.composenavigation.navigation.NavigationDestination

private const val URI = "deeplink://"

sealed interface NavRoute {
    val screen: NavGraphView
    val arguments: List<NamedNavArgument>
        get() = listOf()
    val deepLink: List<String>
        get() = listOf()
    val absoluteDeepLinks: List<NavDeepLink>
        get() = deepLink.map { navDeepLink { uriPattern = "$URI${it}" } }
    private val route: Route
        get() = Route(this, arguments.map { it.name })

    fun getRouteUrlWithParams(): String {
        return route.routeUrlWithParams
    }

    fun getRouteWithParams(vararg parameters: Pair<String, String?>): NavigationDestination {
        return route.getRouteWithParams(*parameters)
    }
}

class Route(baseRoute: NavRoute, parameters: List<String>) {
    val routeUrlWithParams: String
    private val routeName =
        baseRoute::class.java.canonicalName?.split(".")?.takeLast(2)?.joinToString(".") ?: ""

    init {
        this.routeUrlWithParams = if (parameters.isNotEmpty()) {
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
