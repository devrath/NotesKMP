package com.istudio.noteskmp.feature.note.presentation.di

import com.istudio.noteskmp.feature.note.presentation.NoteArgs
import com.istudio.noteskmp.feature.note.presentation.NoteViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notePresentationModule = module {
    // NoteArgs comes from the route, so it is passed as a parameter.
    viewModel { (args: NoteArgs) -> NoteViewModel(get(), args, get(), get(), get()) }
}
