package com.istudio.noteskmp.feature.notes.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

/** The home screen: all notes. */
@Serializable
data object NotesListRoute

fun NavGraphBuilder.notesGraph(
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    composable<NotesListRoute> {
        NotesListRoot(
            onNoteClick = onNoteClick,
            onCreateNoteClick = onCreateNoteClick,
            onSearchClick = onSearchClick,
        )
    }
}
