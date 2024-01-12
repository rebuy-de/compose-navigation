package com.katajona.composenavigation

import com.katajona.composenavigation.navigation.NavigationType
import com.katajona.composenavigation.navigation.destinations.HomeScreens
import com.katajona.composenavigation.navigation.destinations.SettingsScreens
import com.katajona.composenavigation.screens.SettingsScreen
import org.junit.Test

import org.junit.Assert.*

class ExampleUnitTest {

    private var router = RouterMock()

    @Test
    fun `NavigateTo Settings Test`() {
        router.dispatch(NavigationType.NavigateTo(SettingsScreens.Settings.get()))
        router.assert<NavigationType.NavigateTo> {
            it.target == SettingsScreens.Settings.get()
        }
    }

    @Test
    fun `NavigateTo ExtraData Test`() {
        router.dispatch(NavigationType.NavigateTo(HomeScreens.ExtraData.get("test", 3)))
        router.assert<NavigationType.NavigateTo> {
            it.target == HomeScreens.ExtraData.get("test", 3)
        }
    }

    @Test
    fun `NavigateUp Test`() {
        router.dispatch(NavigationType.NavigateUp)
        router.assert<NavigationType.NavigateUp>()
    }
}