package com.istudio.noteskmp.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin. Call exactly once per platform at startup. */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            // Register each feature's data and presentation modules here as they are added.
        )
    }
}
