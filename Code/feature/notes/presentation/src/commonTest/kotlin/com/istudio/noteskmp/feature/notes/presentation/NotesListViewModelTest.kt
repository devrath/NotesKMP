package com.istudio.noteskmp.feature.notes.presentation

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.domain.note.NewNote
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.file.ExportResult
import com.istudio.noteskmp.core.presentation.file.PickedFile
import com.istudio.noteskmp.core.testing.FakeClock
import com.istudio.noteskmp.core.testing.FakeNoteRepository
import com.istudio.noteskmp.feature.notes.domain.IMPORT_MAX_BYTES
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_error_not_text
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_error_too_large
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_error_unreadable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModelTest {

    private val clock = FakeClock()
    private val repository = FakeNoteRepository(clock)
    private val store = ViewModelStore()
    private val samples = SampleNotes {
        listOf(NewNote("Welcome", "w", isPinned = true), NewNote("Cheat sheet", "c"), NewNote("Tips", "t"))
    }

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(): NotesListViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        return NotesListViewModel(repository, samples, clock, AppDispatchers(dispatcher))
            .also { store.put("vm", it) }
    }

    /** Clears the ViewModel inside the test, so its once-a-minute clock stops before runTest ends. */
    private fun listTest(block: suspend TestScope.() -> Unit) = runTest {
        try {
            block()
        } finally {
            store.clear()
        }
    }

    private fun NotesListViewModel.allIds() = state.value.let { s -> (s.pinned + s.others).map { it.id } }

    @Test
    fun `first launch creates the samples before showing the list`() = listTest {
        val vm = viewModel()

        val state = vm.state.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.pinned.map { it.title }).containsExactly(NoteTitle.Typed("Welcome"))
        assertThat(state.others.size).isEqualTo(2)
    }

    @Test
    fun `samples come only once`() = listTest {
        viewModel()
        repository.all.forEach { repository.deleteNote(it.id) }
        store.clear()
        val again = viewModel()

        assertThat(again.state.value.isEmpty).isTrue()
    }

    @Test
    fun `pinned first - then newest edit first`() = listTest {
        val vm = viewModel()
        repository.all.forEach { repository.deleteNote(it.id) }
        val old = repository.add("Old", "")
        clock.advance(1.minutes)
        val pinned = repository.add("Pinned", "", isPinned = true)
        clock.advance(1.minutes)
        val new = repository.add("New", "")

        assertThat(vm.allIds()).containsExactly(pinned, new, old)
    }

    @Test
    fun `edit times are relative to a clock that moves every minute`() = listTest {
        val vm = viewModel()
        val start = vm.state.value.now
        clock.advance(1.minutes)
        advanceTimeBy(60_001)

        assertThat(vm.state.value.now).isEqualTo(start + 1.minutes)
    }

    @Test
    fun `pinning from the long-press menu moves the note and closes the menu`() = listTest {
        val vm = viewModel()
        val tips = vm.state.value.others.last().id
        vm.onAction(NotesListAction.OnNoteLongClick(tips))
        assertThat(vm.state.value.menuNoteId).isEqualTo(tips)

        vm.onAction(NotesListAction.OnTogglePinClick(tips))

        assertThat(vm.state.value.menuNoteId).isNull()
        assertThat(vm.state.value.pinned.map { it.id }).containsExactlyInAnyOrder(1L, tips)
    }

    @Test
    fun `pinning does not change the edit time`() = listTest {
        val vm = viewModel()
        val tips = repository.all.last()
        clock.advance(10.minutes)
        vm.onAction(NotesListAction.OnTogglePinClick(tips.id))

        assertThat(repository.note(tips.id)?.editedAt).isEqualTo(tips.editedAt)
    }

    @Test
    fun `delete asks first and removes only after confirming`() = listTest {
        val vm = viewModel()
        val id = vm.state.value.others.first().id
        vm.onAction(NotesListAction.OnDeleteClick(id))
        assertThat(vm.state.value.pendingDelete?.noteId).isEqualTo(id)

        vm.onAction(NotesListAction.OnDeleteDismiss)
        assertThat(repository.note(id) != null).isTrue()

        vm.onAction(NotesListAction.OnDeleteClick(id))
        vm.onAction(NotesListAction.OnDeleteConfirm)
        assertThat(repository.note(id)).isNull()
        assertThat(vm.state.value.pendingDelete).isNull()
    }

    @Test
    fun `importing creates the note - scrolls to it and offers to open it`() = listTest {
        val vm = viewModel()
        vm.events.test {
            vm.onAction(NotesListAction.OnFilePicked(PickedFile.Read("Ideas.md", "﻿line 1\r\nline 2".encodeToByteArray())))

            val event = awaitItem() as NotesListEvent.ShowSnackbar
            val imported = repository.all.last()
            assertThat(imported.title).isEqualTo("Ideas")
            assertThat(imported.body).isEqualTo("line 1\nline 2")
            assertThat(event.openNoteId).isEqualTo(imported.id)
            assertThat((event.message as UiText.Resource).args.toList()).containsExactly("Ideas")
            assertThat(vm.state.value.scrollToNoteId).isEqualTo(imported.id)
            assertThat(vm.state.value.recentlyImportedId).isEqualTo(imported.id)

            vm.onAction(NotesListAction.OnScrolledToNote)
            assertThat(vm.state.value.scrollToNoteId).isNull()
            advanceTimeBy(IMPORT_HIGHLIGHT_MILLIS + 1)
            assertThat(vm.state.value.recentlyImportedId).isNull()
        }
    }

    @Test
    fun `import with the same title as an existing note adds a second one`() = listTest {
        val vm = viewModel()
        vm.onAction(NotesListAction.OnFilePicked(PickedFile.Read("Tips.md", "x".encodeToByteArray())))

        assertThat(repository.all.count { it.title == "Tips" }).isEqualTo(2)
    }

    @Test
    fun `each import failure has its own message and creates nothing`() = listTest {
        val vm = viewModel()
        val before = repository.all.size
        vm.events.test {
            vm.onAction(NotesListAction.OnFilePicked(PickedFile.TooLarge("big.md")))
            assertThat((awaitItem() as NotesListEvent.ShowSnackbar).message.resource()).isEqualTo(Res.string.import_error_too_large)

            vm.onAction(NotesListAction.OnFilePicked(PickedFile.Unreadable("gone.md")))
            assertThat((awaitItem() as NotesListEvent.ShowSnackbar).message.resource()).isEqualTo(Res.string.import_error_unreadable)

            vm.onAction(NotesListAction.OnFilePicked(PickedFile.Read("bin.md", byteArrayOf(0xFF.toByte(), 0x00))))
            assertThat((awaitItem() as NotesListEvent.ShowSnackbar).message.resource()).isEqualTo(Res.string.import_error_not_text)

            vm.onAction(NotesListAction.OnFilePicked(PickedFile.Read("big.md", ByteArray((IMPORT_MAX_BYTES + 1).toInt()) { 'a'.code.toByte() })))
            assertThat((awaitItem() as NotesListEvent.ShowSnackbar).message.resource()).isEqualTo(Res.string.import_error_too_large)
        }
        assertThat(repository.all.size).isEqualTo(before)
    }

    @Test
    fun `cancelling the picker does nothing`() = listTest {
        val vm = viewModel()
        vm.events.test {
            vm.onAction(NotesListAction.OnFilePicked(null))
            expectNoEvents()
        }
    }

    @Test
    fun `export hands the note over and confirms the save`() = listTest {
        val vm = viewModel()
        val id = repository.all.first { it.title == "Tips" }.id
        vm.events.test {
            vm.onAction(NotesListAction.OnExportClick(id))
            assertThat(awaitItem()).isEqualTo(NotesListEvent.Export("Tips.md", "t"))

            vm.onAction(NotesListAction.OnExportFinished(ExportResult.Saved))
            assertThat(awaitItem()).isInstanceOf<NotesListEvent.ShowSnackbar>()
            vm.onAction(NotesListAction.OnExportFinished(ExportResult.Cancelled))
            expectNoEvents()
        }
    }

    @Test
    fun `storage errors are shown`() = listTest {
        val vm = viewModel()
        vm.events.test {
            repository.error = DataError.Local.DISK_FULL
            vm.onAction(NotesListAction.OnTogglePinClick(1))
            assertThat(awaitItem()).isInstanceOf<NotesListEvent.ShowSnackbar>()
        }
    }

    private fun UiText.resource() = (this as UiText.Resource).id
}
