package com.istudio.noteskmp.feature.note.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

/**
 * A note. [noteId] is null for a new note, which opens in edit mode. [searchQuery] is set
 * when opened from search, so the preview scrolls to the first match.
 */
@Serializable
data class NoteRoute(
    val noteId: Long? = null,
    val searchQuery: String? = null,
)

fun NavGraphBuilder.noteGraph(onBack: () -> Unit) {
    composable<NoteRoute> { entry ->
        val route = entry.toRoute<NoteRoute>()
        NoteRoot(args = NoteArgs(route.noteId, route.searchQuery), onBack = onBack)
    }
}
