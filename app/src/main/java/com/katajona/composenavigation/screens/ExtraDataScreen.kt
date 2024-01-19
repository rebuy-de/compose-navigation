package com.katajona.composenavigation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.Router
import com.katajona.composenavigation.navigation.destinations.HomeScreens
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraDataScreen(text: String, count: Int, router: Router = koinInject()) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        router.dispatch(NavigationType.NavigateUp)
                    }) {
                        Icon(Icons.Outlined.ArrowBack, "")
                    }
                }, title = { Text(text = "ExtraData screen") }
            )
        },
        content = {
            Column {
                Row(modifier = Modifier.padding(it)) {
                    Text(text = text)
                    Text(text = count.toString())
                }
                Button(onClick = {
                    router.dispatch(NavigationType.PopUpTo(HomeScreens.Home, false))
                }) {
                    Text(text = "pop to home")
                }
            }
        }
    )
}