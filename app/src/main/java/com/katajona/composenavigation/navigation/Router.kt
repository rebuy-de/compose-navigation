package com.katajona.composenavigation.navigation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Stable
import androidx.navigation.NavOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class Router : CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val _eventChannel = Channel<NavigationType>(Channel.BUFFERED)
    val sharedFlow = _eventChannel.receiveAsFlow()

    open fun dispatch(navTarget: NavigationType) {
        launch {
            _eventChannel.send(navTarget)
        }
    }
}