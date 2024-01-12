package com.katajona.composenavigation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.Router
import com.katajona.composenavigation.navigation.destinations.HomeScreens
import com.katajona.composenavigation.navigation.destinations.SettingsScreens
import org.koin.compose.koinInject


@Composable
fun SettingsScreen(router: Router = koinInject()) {
    Column {
        Text(text = "settings")
        Button(onClick = {
            router.dispatch(NavigationType.NavigateTo(SettingsScreens.Register.get()))
        }) {
            Text(text = "open register bottom sheet")
        }
        Button(onClick = {
            router.dispatch(NavigationType.NavigateTo(SettingsScreens.Dialog.get("Notify")))
        }) {
            Text(text = "open dialog")
        }
        Button(onClick = {
            router.dispatch(NavigationType.NavigateTo(HomeScreens.Home.get()))
        }) {
            Text(text = "open home")
        }
    }
}