package com.istudio.noteskmp.feature.search.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.istudio.noteskmp.core.designsystem.components.NotesEmptyState
import com.istudio.noteskmp.core.designsystem.components.NotesSearchField
import com.istudio.noteskmp.core.designsystem.components.SearchResultItem
import com.istudio.noteskmp.core.designsystem.components.highlightMatches
import com.istudio.noteskmp.core.designsystem.components.searchHighlightStyle
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.presentation.asString
import com.istudio.noteskmp.core.presentation.time.asString
import com.istudio.noteskmp.core.presentation.time.relativeTime
import com.istudio.noteskmp.feature.search.domain.SearchSnippet
import com.istudio.noteskmp.feature.search.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.search.presentation.generated.resources.empty_no_results_message
import com.istudio.noteskmp.feature.search.presentation.generated.resources.empty_no_results_title
import com.istudio.noteskmp.feature.search.presentation.generated.resources.empty_start_message
import com.istudio.noteskmp.feature.search.presentation.generated.resources.empty_start_title
import com.istudio.noteskmp.feature.search.presentation.generated.resources.result_count
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun SearchRoot(
    onBack: () -> Unit,
    onResultClick: (noteId: Long, query: String) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onResultClick = { onResultClick(it, state.query) },
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    onAction: (SearchAction) -> Unit,
    onBack: () -> Unit,
    onResultClick: (Long) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val timeZone = remember { TimeZone.currentSystemDefault() }

    // The field is focused with the keyboard up as soon as search opens.
    LaunchedEffect(focusRequester) {
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboard?.show()
    }
    // Scrolling the results hides the keyboard.
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) keyboard?.hide()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NotesSearchField(
                query = state.query,
                onQueryChange = { onAction(SearchAction.OnQueryChange(it)) },
                onCloseClick = onBack,
                focusRequester = focusRequester,
                onSearch = { keyboard?.hide() },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isQueryEmpty -> NotesEmptyState(
                    icon = NotesIcons.Search,
                    title = stringResource(Res.string.empty_start_title),
                    message = stringResource(Res.string.empty_start_message),
                    modifier = Modifier.padding(top = NotesSpacing.space10),
                )
                state.hasNoResults -> NotesEmptyState(
                    icon = NotesIcons.Search,
                    title = stringResource(Res.string.empty_no_results_title, state.query.trim()),
                    message = state.error?.asString() ?: stringResource(Res.string.empty_no_results_message),
                    modifier = Modifier.padding(top = NotesSpacing.space10),
                )
                else -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    if (state.results.isNotEmpty()) {
                        item(key = "count") {
                            Text(
                                text = pluralStringResource(Res.plurals.result_count, state.results.size, state.results.size),
                                style = NotesTheme.typography.meta,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = NotesSpacing.space5,
                                    end = NotesSpacing.space5,
                                    top = NotesSpacing.space3,
                                    bottom = 6.dp,
                                ),
                            )
                        }
                    }
                    items(state.results, key = { it.id }) { result ->
                        val highlight = searchHighlightStyle
                        val title = result.title.asString()
                        SearchResultItem(
                            title = remember(title, result.titleMatches, highlight) {
                                highlightMatches(title, result.titleMatches, highlight)
                            },
                            snippet = remember(result.snippet, highlight) {
                                highlightMatches(result.snippet.text, result.snippet.matches, highlight)
                            },
                            editedTime = relativeTime(result.editedAt, state.now, timeZone).asString(),
                            isPinned = result.isPinned,
                            onClick = { onResultClick(result.id) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Results", widthDp = 360, heightDp = 640)
@Composable
private fun SearchScreenPreview() {
    NotesTheme(platform = NotesPlatform.Android) {
        SearchScreen(
            state = SearchState(
                query = "plan",
                searchedTerms = listOf("plan"),
                now = Instant.parse("2026-03-10T12:00:00Z"),
                results = listOf(
                    SearchResultUi(
                        id = 1,
                        title = NoteTitle.Typed("Weekly plan"),
                        titleMatches = listOf(7..10),
                        snippet = SearchSnippet("This week: focus on search and import errors.", emptyList()),
                        editedAt = Instant.parse("2026-03-09T12:00:00Z"),
                        isPinned = true,
                    ),
                ),
            ),
            onAction = {},
            onBack = {},
            onResultClick = {},
        )
    }
}

@Preview(name = "No results, iOS", widthDp = 393, heightDp = 640)
@Composable
private fun SearchScreenNoResultsPreview() {
    NotesTheme(platform = NotesPlatform.Ios) {
        SearchScreen(
            state = SearchState(query = "quarterly budget", searchedTerms = listOf("quarterly", "budget")),
            onAction = {},
            onBack = {},
            onResultClick = {},
        )
    }
}
