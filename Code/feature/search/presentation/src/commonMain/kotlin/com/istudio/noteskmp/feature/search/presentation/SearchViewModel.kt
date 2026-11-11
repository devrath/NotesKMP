package com.istudio.noteskmp.feature.search.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NoteSearchHit
import com.istudio.noteskmp.core.domain.note.NoteRepository
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.domain.search.findMatches
import com.istudio.noteskmp.core.domain.search.searchTerms
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.toUiText
import com.istudio.noteskmp.feature.search.domain.SearchSnippet
import com.istudio.noteskmp.feature.search.domain.buildSnippet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant

/** Results update this long after the last keystroke. */
internal const val SEARCH_DEBOUNCE_MILLIS = 150L
private const val KEY_QUERY = "query"

data class SearchState(
    val query: String = "",
    /** The terms the current [results] were found with. */
    val searchedTerms: List<String> = emptyList(),
    val results: List<SearchResultUi> = emptyList(),
    val now: Instant = Instant.fromEpochMilliseconds(0),
    val error: UiText? = null,
) {
    val isQueryEmpty: Boolean get() = searchTerms(query).isEmpty()

    /** Nothing found for what was typed, once the search for it has finished. */
    val hasNoResults: Boolean
        get() = !isQueryEmpty && results.isEmpty() && searchedTerms == searchTerms(query)
}

data class SearchResultUi(
    val id: Long,
    val title: NoteTitle,
    val titleMatches: List<IntRange>,
    val snippet: SearchSnippet,
    val editedAt: Instant,
    val isPinned: Boolean,
)

sealed interface SearchAction {
    data class OnQueryChange(val query: String) : SearchAction
}

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
    private val clock: Clock,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState(query = savedStateHandle[KEY_QUERY] ?: ""))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.map { searchTerms(it.query) }
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .collectLatest(::search)
        }
    }

    fun onAction(action: SearchAction) {
        when (action) {
            is SearchAction.OnQueryChange -> {
                // The query is kept when the user opens a result and comes back.
                savedStateHandle[KEY_QUERY] = action.query
                _state.update { it.copy(query = action.query) }
            }
        }
    }

    private suspend fun search(terms: List<String>) {
        if (terms.isEmpty()) {
            _state.update { it.copy(searchedTerms = terms, results = emptyList(), error = null) }
            return
        }
        when (val result = repository.search(terms)) {
            is Result.Success -> {
                val results = withContext(dispatchers.default) { result.data.map { it.toSearchResultUi(terms) } }
                _state.update {
                    it.copy(searchedTerms = terms, results = results, now = clock.now(), error = null)
                }
            }
            is Result.Error -> _state.update {
                it.copy(searchedTerms = terms, results = emptyList(), error = result.error.toUiText())
            }
        }
    }
}

private fun NoteSearchHit.toSearchResultUi(terms: List<String>): SearchResultUi {
    val titleText = when (val t = title) {
        is NoteTitle.Typed -> t.text
        is NoteTitle.Fallback -> t.text
        NoteTitle.Untitled -> ""
    }
    return SearchResultUi(
        id = id,
        title = title,
        titleMatches = findMatches(titleText, terms),
        snippet = buildSnippet(strippedBody, terms),
        editedAt = editedAt,
        isPinned = isPinned,
    )
}
