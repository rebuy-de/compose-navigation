package com.katajona.composenavigation

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DismissValue
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.katajona.composenavigation.ui.theme.ComposeNavigationTheme
import com.katajona.composenavigation.navigation.AppNavHost
import com.katajona.composenavigation.navigation.BottomNavigation
import com.katajona.composenavigation.navigation.destinations.HomeScreens
import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.Router
import com.katajona.composenavigation.navigation.SnackBarStyle
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeNavigationTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MainContent() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val snackbarHostState = remember { SnackbarHostState() }
    var snackBarStyle by remember { mutableStateOf(SnackBarStyle.NEUTRAL) }
    val startDestination by remember { mutableStateOf(HomeScreens.Home.get().route) }


    ListenToNavigation(
        navController,
        snackbarHostState,
        onSnackBarStyleChanged = { snackBarStyle = it })

    val dismissSnackbarState = rememberDismissState(confirmValueChange = { value ->
        if (value != DismissValue.Default) {
            snackbarHostState.currentSnackbarData?.dismiss()
            true
        } else {
            false
        }
    })

    LaunchedEffect(dismissSnackbarState.currentValue) {
        if (dismissSnackbarState.currentValue != DismissValue.Default) {
            dismissSnackbarState.reset()
        }
    }

    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) },
        snackbarHost = {
            SwipeToDismiss(
                state = dismissSnackbarState,
                background = {},
                dismissContent = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            actionColor = snackBarStyle.color,
                            snackbarData = data,
                        )
                    }
                }
            )
        }
    ) { padding ->
        ModalBottomSheetLayout(bottomSheetNavigator, sheetBackgroundColor = Color.Transparent) {
            AppNavHost(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}

@Composable
fun ListenToNavigation(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onSnackBarStyleChanged: (SnackBarStyle) -> Unit,
    router: Router = koinInject(),
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        router.sharedFlow.collect {
            when (it) {
                is NavigationType.NavigateUp -> navController.navigateUp()
                is NavigationType.PopUpTo -> navController.popBackStack(
                    it.target.routeWithParams,
                    it.inclusive
                )

                is NavigationType.NavigateTo -> navController.navigate(
                    it.target.route,
                    it.navOptions
                )

                is NavigationType.DeeplinkTo -> {
                    val uri = Uri.parse(it.href)
                    if (navController.graph.hasDeepLink(uri)) {
                        navController.navigate(uri)
                    }
                }

                is NavigationType.Snackbar -> {
                    onSnackBarStyleChanged(it.style)
                    coroutineScope.coroutineContext.cancelChildren()
                    coroutineScope.launch {
                        val snackbarResult = snackbarHostState.showSnackbar(
                            message = it.message,
                            actionLabel = it.actionLabel,
                            duration = it.duration,
                            withDismissAction = it.withDismissAction
                        )
                        when (snackbarResult) {
                            SnackbarResult.Dismissed -> {
                                it.onDismiss?.invoke()
                            }

                            SnackbarResult.ActionPerformed -> {
                                it.onAction?.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}

