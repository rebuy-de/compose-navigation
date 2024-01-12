package com.katajona.composenavigation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.katajona.composenavigation.navigation.destinations.HomeScreens
import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.Router
import com.katajona.composenavigation.navigation.destinations.SettingsScreens
import org.koin.compose.koinInject

@Composable
fun HomeScreen(router: Router = koinInject()) {
    Column {
        Text(text = "Home")
        Button(onClick = {
            router.dispatch(NavigationType.NavigateTo(HomeScreens.ExtraData.get("MyTitle", 5)))
        }) {
            Text(text = "open ExtraDataScreen")
        }
        Button(onClick = {
            router.dispatch(NavigationType.DeeplinkTo("deeplink://extra?text=Title%20name&count=20"))
        }) {
            Text(text = "open ExtraDataScreen with deeplink example1")
        }
        Button(onClick = {
            router.dispatch(NavigationType.DeeplinkTo("deeplink://extraData/Title%20example?amount=2"))
        }) {
            Text(text = "open ExtraDataScreen with deeplink example2")
        }
        Button(onClick = {
            router.dispatch(NavigationType.DeeplinkTo("deeplink://extra?count=20"))
        }) {
            Text(text = "open ExtraDataScreen with deeplink example3")
        }
        Button(onClick = {
            router.dispatch(NavigationType.NavigateTo(SettingsScreens.Register.get()))
        }) {
            Text(text = "open register bottom sheet")
        }
    }
}