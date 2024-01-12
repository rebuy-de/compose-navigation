package com.katajona.composenavigation.screens


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.Router
import org.koin.compose.koinInject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogScreen(title: String, router: Router = koinInject()) {
    AlertDialog(
        onDismissRequest = { router.dispatch(NavigationType.NavigateUp) },
        title = { Text(title) },
        confirmButton = {
            Button(onClick = {
                router.dispatch(NavigationType.NavigateUp)
            }, content = { Text(text = "ok") })
        }
    )
}