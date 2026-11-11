package com.istudio.noteskmp.feature.note.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.ApplicationScope
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.core.domain.note.Note
import com.istudio.noteskmp.core.domain.note.NoteRepository
import com.istudio.noteskmp.core.domain.onFailure
import com.istudio.noteskmp.core.domain.onSuccess
import com.istudio.noteskmp.core.domain.search.findMatches
import com.istudio.noteskmp.core.domain.search.searchTerms
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.file.ExportResult
import com.istudio.noteskmp.core.presentation.noteExport
import com.istudio.noteskmp.core.presentation.toUiText
import com.istudio.noteskmp.feature.note.domain.EditorValue
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat
import com.istudio.noteskmp.feature.note.domain.applyFormat
import com.istudio.noteskmp.feature.note.domain.continueList
import com.istudio.noteskmp.feature.note.domain.parseMarkdown
import com.istudio.noteskmp.feature.note.domain.toggleChecklistItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Autosave runs after this much typing inactivity. */
internal const val AUTOSAVE_DEBOUNCE_MILLIS = 500L

/**
 * Drafts up to this size are kept in saved state, which survives process death on Android.
 * Larger drafts would overflow the saved-state limit, so they rely on autosave alone.
 */
internal const val MAX_DRAFT_LENGTH = 100_000

// Not "noteId": the route argument already uses that key.
private const val KEY_NOTE_ID = "createdNoteId"
private const val KEY_DRAFT_TITLE = "draftTitle"
private const val KEY_DRAFT_BODY = "draftBody"
private const val KEY_MODE = "mode"

/**
 * One note, read or written. Nothing typed is ever lost: changes save 500 ms after typing
 * stops, immediately when the screen leaves the foreground, and once more when it closes.
 * Saves run in [applicationScope] so they finish even after this ViewModel is cleared.
 */
class NoteViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val args: NoteArgs,
    private val repository: NoteRepository,
    private val applicationScope: ApplicationScope,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(NoteState())
    val state = _state.asStateFlow()

    private val _events = Channel<NoteEvent>()
    val events = _events.receiveAsFlow()

    private val saveMutex = Mutex()
    private var autosaveJob: Job? = null
    private var parseJob: Job? = null

    private var noteId: Long? = savedStateHandle[KEY_NOTE_ID] ?: args.noteId

    /** Created by this screen, so it's discarded if it ends up empty. */
    private var isCreatedHere = args.noteId == null

    /** The text as last written to the database. */
    private var savedTitle = ""
    private var savedBody = ""
    private var isDeleted = false

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val draftTitle: String? = savedStateHandle[KEY_DRAFT_TITLE]
        val draftBody: String? = savedStateHandle[KEY_DRAFT_BODY]
        val savedMode = savedStateHandle.get<String>(KEY_MODE)?.let { NoteMode.valueOf(it) }
        val id = noteId
        val note: Note? = if (id == null) {
            null
        } else {
            when (val result = repository.getNote(id)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    _events.send(NoteEvent.ShowSnackbar(result.error.toUiText()))
                    _events.send(NoteEvent.Close)
                    return
                }
            }
        }
        savedTitle = note?.title.orEmpty()
        savedBody = note?.body.orEmpty()
        val title = draftTitle ?: savedTitle
        val body = draftBody ?: savedBody
        val mode = savedMode ?: if (args.noteId == null) NoteMode.Edit else NoteMode.Preview
        val anchor = args.searchQuery?.let { firstMatchOffset(body, it) } ?: 0
        _state.update {
            it.copy(
                isLoading = false,
                title = title,
                body = TextFieldValue(body, TextRange(if (mode == NoteMode.Edit && note == null) body.length else 0)),
                mode = mode,
                isPinned = note?.isPinned ?: false,
                scrollAnchor = anchor,
                scrollRequest = it.scrollRequest + 1,
                focusBodyRequest = args.noteId == null && savedMode == null,
            )
        }
        if (mode == NoteMode.Preview) reparse()
        // A draft restored after process death still has to reach the database.
        if (title != savedTitle || body != savedBody) scheduleSave()
    }

    fun onAction(action: NoteAction) {
        when (action) {
            is NoteAction.OnTitleChange -> onTextChange(title = action.title, body = state.value.body)
            is NoteAction.OnBodyChange -> onBodyChange(action.value)
            is NoteAction.OnFormatClick -> format(action.format, action.clipboardText)
            is NoteAction.OnToggleModeClick -> toggleMode(action.anchor)
            is NoteAction.OnPreviewDoubleTap -> editAt(action.offset)
            is NoteAction.OnChecklistToggle -> toggleChecklist(action.checkboxOffset)
            NoteAction.OnBodyFocused -> _state.update { it.copy(focusBodyRequest = false) }
            NoteAction.OnMenuClick -> _state.update { it.copy(isMenuOpen = true) }
            NoteAction.OnMenuDismiss -> _state.update { it.copy(isMenuOpen = false) }
            NoteAction.OnTogglePinClick -> togglePin()
            NoteAction.OnExportClick -> export()
            is NoteAction.OnExportFinished -> onExportFinished(action.result)
            NoteAction.OnDeleteClick -> _state.update { it.copy(isMenuOpen = false, isDeleteDialogVisible = true) }
            NoteAction.OnDeleteConfirm -> delete()
            NoteAction.OnDeleteDismiss -> _state.update { it.copy(isDeleteDialogVisible = false) }
            is NoteAction.OnLinkLongPress -> _state.update { it.copy(linkMenu = LinkMenuUi(action.blockIndex, action.url)) }
            NoteAction.OnLinkMenuDismiss -> _state.update { it.copy(linkMenu = null) }
            NoteAction.OnLeaveForeground -> saveNow()
        }
    }

    // ---- Editing ----

    /**
     * Raw input that list continuation replaced. The text field can send that same raw value
     * once more from its own buffer before it shows the replacement; that echo is ignored.
     */
    private var replacedInput: TextFieldValue? = null

    private fun onBodyChange(value: TextFieldValue) {
        val replaced = replacedInput
        replacedInput = null
        if (replaced != null && value.text == replaced.text && value.selection == replaced.selection) return
        val continued = continueList(state.value.body.toEditorValue(), value.toEditorValue())
        if (continued != null) replacedInput = value
        val next = continued?.toTextFieldValue() ?: value
        onTextChange(title = state.value.title, body = next)
    }

    private fun format(format: MarkdownFormat, clipboardText: String?) {
        val formatted = applyFormat(state.value.body.toEditorValue(), format, clipboardText)
        onTextChange(title = state.value.title, body = formatted.toTextFieldValue())
    }

    private fun onTextChange(title: String, body: TextFieldValue) {
        val textChanged = title != state.value.title || body.text != state.value.body.text
        _state.update { it.copy(title = title, body = body, isSaved = if (textChanged) false else it.isSaved) }
        if (!textChanged) return
        rememberDraft(title, body.text)
        scheduleSave()
    }

    private fun rememberDraft(title: String, body: String) {
        savedStateHandle[KEY_DRAFT_TITLE] = title
        savedStateHandle[KEY_DRAFT_BODY] = if (body.length <= MAX_DRAFT_LENGTH) body else null
    }

    private fun toggleMode(anchor: Int) {
        val next = if (state.value.mode == NoteMode.Edit) NoteMode.Preview else NoteMode.Edit
        savedStateHandle[KEY_MODE] = next.name
        _state.update {
            it.copy(mode = next, scrollAnchor = anchor, scrollRequest = it.scrollRequest + 1, linkMenu = null)
        }
        if (next == NoteMode.Preview) {
            saveNow()
            reparse()
        }
    }

    private fun editAt(offset: Int) {
        val body = state.value.body
        val cursor = offset.coerceIn(0, body.text.length)
        savedStateHandle[KEY_MODE] = NoteMode.Edit.name
        _state.update {
            it.copy(
                mode = NoteMode.Edit,
                body = body.copy(selection = TextRange(cursor)),
                scrollAnchor = cursor,
                scrollRequest = it.scrollRequest + 1,
                focusBodyRequest = true,
                linkMenu = null,
            )
        }
    }

    /** Ticking a box is an edit: it changes the Markdown and saves straight away. */
    private fun toggleChecklist(checkboxOffset: Int) {
        val body = state.value.body
        val toggled = toggleChecklistItem(body.text, checkboxOffset) ?: return
        onTextChange(title = state.value.title, body = body.copy(text = toggled))
        saveNow()
        reparse()
    }

    private fun reparse() {
        val text = state.value.body.text
        parseJob?.cancel()
        parseJob = viewModelScope.launch {
            val blocks = withContext(dispatchers.default) { parseMarkdown(text) }
            _state.update { it.copy(blocks = blocks) }
        }
    }

    // ---- Saving ----

    private fun scheduleSave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(AUTOSAVE_DEBOUNCE_MILLIS)
            save()
        }
    }

    private fun saveNow(): Job {
        autosaveJob?.cancel()
        return applicationScope.launch { save() }
    }

    /** Writes the current text if it changed since the last save. One save at a time. */
    private suspend fun save() = saveMutex.withLock {
        if (isDeleted) return@withLock
        val title = state.value.title
        val body = state.value.body.text
        if (title == savedTitle && body == savedBody) return@withLock
        val id = noteId
        if (id == null) {
            // A new note isn't saved until it has some text.
            if (title.isBlank() && body.isBlank()) return@withLock
            repository.createNote(NewNote(title, body)).onSuccess { newId ->
                noteId = newId
                savedStateHandle[KEY_NOTE_ID] = newId
                onSaved(title, body)
            }.onFailure { showSnackbar(it.toUiText()) }
        } else {
            repository.updateNote(id, title, body)
                .onSuccess { onSaved(title, body) }
                .onFailure { showSnackbar(it.toUiText()) }
        }
    }

    private fun onSaved(title: String, body: String) {
        savedTitle = title
        savedBody = body
        _state.update {
            if (it.title == title && it.body.text == body) it.copy(isSaved = true) else it
        }
    }

    override fun onCleared() {
        autosaveJob?.cancel()
        // The screen is closing: write what's left, or drop a new note left empty.
        applicationScope.launch {
            val isEmpty = state.value.title.isBlank() && state.value.body.text.isBlank()
            if (isCreatedHere && isEmpty) {
                saveMutex.withLock {
                    noteId?.let { repository.deleteNote(it) }
                    isDeleted = true
                }
            } else {
                save()
            }
        }
    }

    // ---- Actions ----

    private fun togglePin() {
        _state.update { it.copy(isMenuOpen = false) }
        val pinned = !state.value.isPinned
        viewModelScope.launch {
            autosaveJob?.cancel()
            save()
            val id = noteId ?: return@launch
            repository.setPinned(id, pinned)
                .onSuccess { _state.update { it.copy(isPinned = pinned) } }
                .onFailure { showSnackbar(it.toUiText()) }
        }
    }

    private var exportingFileName: String? = null

    private fun export() {
        _state.update { it.copy(isMenuOpen = false) }
        viewModelScope.launch {
            val export = noteExport(state.value.title, state.value.body.text)
            exportingFileName = export.fileName
            _events.send(NoteEvent.Export(export.fileName, export.content))
        }
    }

    private fun onExportFinished(result: ExportResult) {
        val fileName = exportingFileName.orEmpty()
        exportingFileName = null
        result.toUiText(fileName)?.let(::showSnackbar)
    }

    private fun delete() {
        _state.update { it.copy(isDeleteDialogVisible = false) }
        autosaveJob?.cancel()
        viewModelScope.launch {
            val deleted = saveMutex.withLock {
                val id = noteId
                val result = if (id == null) Result.Success(Unit) else repository.deleteNote(id)
                if (result is Result.Success) isDeleted = true
                result
            }
            when (deleted) {
                is Result.Success -> _events.send(NoteEvent.Close)
                is Result.Error -> showSnackbar(deleted.error.toUiText())
            }
        }
    }

    private fun showSnackbar(message: UiText) {
        viewModelScope.launch { _events.send(NoteEvent.ShowSnackbar(message)) }
    }
}

/** What the note screen was opened with. */
data class NoteArgs(
    /** Null for a new note. */
    val noteId: Long?,
    /** Set when opened from search, to scroll to the first match. */
    val searchQuery: String? = null,
)

private fun firstMatchOffset(body: String, query: String): Int? =
    findMatches(body, searchTerms(query)).firstOrNull()?.first

private fun TextFieldValue.toEditorValue() = EditorValue(text, selection.start, selection.end)

private fun EditorValue.toTextFieldValue() = TextFieldValue(text, TextRange(selectionStart, selectionEnd))
