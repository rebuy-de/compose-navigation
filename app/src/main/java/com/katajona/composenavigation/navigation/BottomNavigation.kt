package com.katajona.composenavigation.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun RbBottomNavigation(
    items: List<BottomNavItem>,
    selectedItem: BottomNavItem?,
    onItemSelectionChanged: (BottomNavItem) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(id = item.title)) },
                label = { Text(text = stringResource(id = item.title)) },
                selected = selectedItem == item,
                onClick = {
                    onItemSelectionChanged(item)
                },
            )
        }
    }
}

open class BottomNavItem(@StringRes var title: Int, var icon: ImageVector)