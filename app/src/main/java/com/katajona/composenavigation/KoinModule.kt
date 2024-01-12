package com.katajona.composenavigation

import com.katajona.composenavigation.navigation.Router
import org.koin.dsl.module

val koinModule = module {
    single { Router() }
}
