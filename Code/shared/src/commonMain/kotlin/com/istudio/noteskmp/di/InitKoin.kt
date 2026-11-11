package com.istudio.noteskmp.di

import com.istudio.noteskmp.core.data.di.coreDataModule
import com.istudio.noteskmp.feature.note.presentation.di.notePresentationModule
import com.istudio.noteskmp.feature.notes.presentation.di.notesPresentationModule
import com.istudio.noteskmp.feature.search.presentation.di.searchPresentationModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Starts Koin. Call exactly once per platform at startup. */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            coreDataModule,
            notesPresentationModule,
            notePresentationModule,
            searchPresentationModule,
        )
    }
}
