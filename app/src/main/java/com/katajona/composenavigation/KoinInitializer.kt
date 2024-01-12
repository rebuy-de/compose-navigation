package com.katajona.composenavigation

import android.content.Context
import androidx.startup.Initializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

@Suppress("unused")
class KoinInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication {
        return startKoin {
            androidLogger(Level.ERROR)
            androidContext(context)
            modules(
                listOf(
                    koinModule,
                )
            )
        }
    }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
