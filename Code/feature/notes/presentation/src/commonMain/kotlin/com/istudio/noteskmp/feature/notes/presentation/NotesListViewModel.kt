package com.istudio.noteskmp.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.core.domain.note.NoteRepository
import com.istudio.noteskmp.core.domain.note.NoteSummary
import com.istudio.noteskmp.core.domain.note.noteTitle
import com.istudio.noteskmp.core.domain.note.summarizeNoteText
import com.istudio.noteskmp.core.domain.onFailure
import com.istudio.noteskmp.core.domain.onSuccess
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.file.ExportResult
import com.istudio.noteskmp.core.presentation.file.PickedFile
import com.istudio.noteskmp.core.presentation.noteExport
import com.istudio.noteskmp.core.presentation.toUiText
import com.istudio.noteskmp.feature.notes.domain.ImportError
import com.istudio.noteskmp.feature.notes.domain.decodeImportedFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/** How long a just-imported note stays outlined. */
internal const val IMPORT_HIGHLIGHT_MILLIS = 2_000L
private const val CLOCK_TICK_MILLIS = 60_000L

class NotesListViewModel(
    private val repository: NoteRepository,
    private val sampleNotes: SampleNotes,
    private val clock: Clock,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(NotesListState(now = clock.now()))
    val state = _state.asStateFlow()

    private val _events = Channel<NotesListEvent>()
    val events = _events.receiveAsFlow()

    /** Name of the file being exported, for the confirmation once the platform reports back. */
    private var exportingFileName: String? = null

    init {
        viewModelScope.launch {
            // Before the first list, so a first launch never shows the empty state.
            repository.createSampleNotesOnFirstLaunch { sampleNotes.create() }
            repository.observeNotes().collect(::onNotes)
        }
        viewModelScope.launch {
            while (true) {
                delay(CLOCK_TICK_MILLIS)
                _state.update { it.copy(now = clock.now()) }
            }
        }
    }

    fun onAction(action: NotesListAction) {
        when (action) {
            is NotesListAction.OnNoteLongClick -> _state.update { it.copy(menuNoteId = action.noteId) }
            NotesListAction.OnMenuDismiss -> _state.update { it.copy(menuNoteId = null) }
            is NotesListAction.OnTogglePinClick -> togglePin(action.noteId)
            is NotesListAction.OnExportClick -> export(action.noteId)
            is NotesListAction.OnExportFinished -> onExportFinished(action.result)
            is NotesListAction.OnDeleteClick -> askToDelete(action.noteId)
            NotesListAction.OnDeleteConfirm -> deletePending()
            NotesListAction.OnDeleteDismiss -> _state.update { it.copy(pendingDelete = null) }
            is NotesListAction.OnFilePicked -> action.file?.let(::import)
            NotesListAction.OnScrolledToNote -> _state.update { it.copy(scrollToNoteId = null) }
        }
    }

    private fun onNotes(notes: List<NoteSummary>) {
        val entries = notes.map { it.toNoteEntryUi() }
        _state.update { state ->
            state.copy(
                isLoading = false,
                pinned = entries.filter { it.isPinned },
                others = entries.filterNot { it.isPinned },
                now = clock.now(),
            )
        }
    }

    private fun findEntry(id: Long): NoteEntryUi? =
        state.value.pinned.firstOrNull { it.id == id } ?: state.value.others.firstOrNull { it.id == id }

    private fun togglePin(id: Long) {
        _state.update { it.copy(menuNoteId = null) }
        val entry = findEntry(id) ?: return
        viewModelScope.launch {
            repository.setPinned(id, !entry.isPinned).onFailure { showSnackbar(it.toUiText()) }
        }
    }

    private fun askToDelete(id: Long) {
        val entry = findEntry(id)
        _state.update {
            it.copy(menuNoteId = null, pendingDelete = entry?.let { e -> PendingDeleteUi(e.id, e.title) })
        }
    }

    private fun deletePending() {
        val pending = state.value.pendingDelete ?: return
        _state.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            repository.deleteNote(pending.noteId).onFailure { showSnackbar(it.toUiText()) }
        }
    }

    private fun export(id: Long) {
        _state.update { it.copy(menuNoteId = null) }
        viewModelScope.launch {
            when (val result = repository.getNote(id)) {
                is Result.Error -> showSnackbar(result.error.toUiText())
                is Result.Success -> {
                    val export = noteExport(result.data.title, result.data.body)
                    exportingFileName = export.fileName
                    _events.send(NotesListEvent.Export(export.fileName, export.content))
                }
            }
        }
    }

    private fun onExportFinished(result: ExportResult) {
        val fileName = exportingFileName.orEmpty()
        exportingFileName = null
        result.toUiText(fileName)?.let(::showSnackbar)
    }

    private fun import(file: PickedFile) {
        viewModelScope.launch {
            val decoded = when (file) {
                is PickedFile.TooLarge -> Result.Error(ImportError.TOO_LARGE)
                is PickedFile.Unreadable -> Result.Error(ImportError.UNREADABLE)
                is PickedFile.Read -> withContext(dispatchers.default) { decodeImportedFile(file.name, file.bytes) }
            }
            when (decoded) {
                is Result.Error -> showSnackbar(decoded.error.toUiText())
                is Result.Success -> createImported(decoded.data)
            }
        }
    }

    private suspend fun createImported(note: NewNote) {
        repository.createNote(note)
            .onSuccess { id ->
                _state.update { it.copy(recentlyImportedId = id, scrollToNoteId = id) }
                val title = noteTitle(note.title, summarizeNoteText(note.title, note.body).fallbackTitle)
                _events.send(NotesListEvent.ShowSnackbar(importedMessage(title), openNoteId = id))
                delay(IMPORT_HIGHLIGHT_MILLIS)
                _state.update { if (it.recentlyImportedId == id) it.copy(recentlyImportedId = null) else it }
            }
            .onFailure { showSnackbar(it.toUiText()) }
    }

    private fun showSnackbar(message: UiText) {
        viewModelScope.launch { _events.send(NotesListEvent.ShowSnackbar(message)) }
    }
}

private fun NoteSummary.toNoteEntryUi() = NoteEntryUi(
    id = id,
    title = title,
    preview = preview,
    editedAt = editedAt,
    isPinned = isPinned,
)
