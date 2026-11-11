package com.istudio.noteskmp

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.istudio.noteskmp.core.designsystem.theme.NotesMotion
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.feature.note.presentation.NoteRoute
import com.istudio.noteskmp.feature.note.presentation.noteGraph
import com.istudio.noteskmp.feature.notes.presentation.NotesListRoute
import com.istudio.noteskmp.feature.notes.presentation.notesGraph
import com.istudio.noteskmp.feature.search.presentation.SearchRoute
import com.istudio.noteskmp.feature.search.presentation.searchGraph

/** The app: the notes list is home, and the note and search screens are one step away. */
@Composable
fun App() {
    NotesTheme {
        val navController = rememberNavController()
        val reduceMotion = NotesTheme.reduceMotion
        val enter = if (reduceMotion) EnterTransition.None else fadeIn(NotesMotion.spec(NotesMotion.DURATION_BASE_MILLIS, false))
        val exit = if (reduceMotion) ExitTransition.None else fadeOut(NotesMotion.spec(NotesMotion.DURATION_BASE_MILLIS, false))
        NavHost(
            navController = navController,
            startDestination = NotesListRoute,
            enterTransition = { enter },
            exitTransition = { exit },
            popEnterTransition = { enter },
            popExitTransition = { exit },
        ) {
            notesGraph(
                onNoteClick = { navController.navigate(NoteRoute(noteId = it)) },
                onCreateNoteClick = { navController.navigate(NoteRoute()) },
                onSearchClick = { navController.navigate(SearchRoute) },
            )
            noteGraph(onBack = navController::navigateBackOnce)
            searchGraph(
                onBack = navController::navigateBackOnce,
                onResultClick = { id, query -> navController.navigate(NoteRoute(noteId = id, searchQuery = query)) },
            )
        }
    }
}

/** Pops one screen, ignoring repeats (a double tap on back) once home is reached. */
private fun NavController.navigateBackOnce() {
    if (previousBackStackEntry != null) popBackStack()
}
