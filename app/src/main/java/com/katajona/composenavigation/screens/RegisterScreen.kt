package com.katajona.composenavigation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun RegisterScreen() {
    Surface(Modifier.defaultMinSize(minHeight = 400.dp).fillMaxWidth()) {
        Column {
            Text(text = "register")
        }
    }
}