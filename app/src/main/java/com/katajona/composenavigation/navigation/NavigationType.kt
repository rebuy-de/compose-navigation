package com.katajona.composenavigation.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.navigation.NavOptions
import com.katajona.composenavigation.navigation.destinations.Route

sealed class NavigationType {
    data class DeeplinkTo(val href: String) : NavigationType()
    data class NavigateTo(val target: NavigationDestination, val navOptions: NavOptions? = null) : NavigationType()
    data object NavigateUp : NavigationType()
    data class PopUpTo(val target: Route, val inclusive: Boolean) : NavigationType()
    data class Snackbar(
        val message: String,
        val style: SnackBarStyle = SnackBarStyle.NEUTRAL,
        val actionLabel: String? = null,
        val withDismissAction: Boolean = false,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val onAction: (() -> Unit)? = null,
        val onDismiss: (() -> Unit)? = null,
    ) : NavigationType()
}
