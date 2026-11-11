package com.istudio.noteskmp.feature.search.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute

fun NavGraphBuilder.searchGraph(
    onBack: () -> Unit,
    onResultClick: (noteId: Long, query: String) -> Unit,
) {
    composable<SearchRoute> {
        SearchRoot(onBack = onBack, onResultClick = onResultClick)
    }
}
