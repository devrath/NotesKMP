package com.istudio.noteskmp.feature.note.presentation

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.istudio.noteskmp.core.domain.AppDispatchers
import com.istudio.noteskmp.core.domain.ApplicationScope
import com.istudio.noteskmp.core.domain.DataError
import com.istudio.noteskmp.core.presentation.UiText
import com.istudio.noteskmp.core.presentation.file.ExportResult
import com.istudio.noteskmp.core.testing.FakeClock
import com.istudio.noteskmp.core.testing.FakeNoteRepository
import com.istudio.noteskmp.feature.note.domain.MarkdownBlock
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private val clock = FakeClock()
    private val repository = FakeNoteRepository(clock)
    private val store = ViewModelStore()

    @AfterTest
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(
        args: NoteArgs = NoteArgs(noteId = null),
        handle: SavedStateHandle = SavedStateHandle(),
    ): NoteViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        return NoteViewModel(handle, args, repository, ApplicationScope(this), AppDispatchers(dispatcher))
            .also { store.put("vm-${it.hashCode()}", it) }
    }

    /** Closes the screen, the way leaving it does. */
    private fun TestScope.close() {
        store.clear()
        runCurrent()
    }

    private fun NoteViewModel.typeBody(text: String) =
        onAction(NoteAction.OnBodyChange(TextFieldValue(text, TextRange(text.length))))

    // ---- New notes ----

    @Test
    fun `new note opens empty in edit mode with the body focused`() = runTest {
        val vm = viewModel()

        val state = vm.state.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.mode).isEqualTo(NoteMode.Edit)
        assertThat(state.focusBodyRequest).isTrue()
    }

    @Test
    fun `new note left empty is never saved`() = runTest {
        viewModel()
        advanceTimeBy(5.minutes)
        close()

        assertThat(repository.all).isEmpty()
    }

    @Test
    fun `whitespace only counts as empty`() = runTest {
        val vm = viewModel()
        vm.typeBody("  \n\t")
        vm.onAction(NoteAction.OnTitleChange("   "))
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)
        close()

        assertThat(repository.all).isEmpty()
    }

    @Test
    fun `one character then back makes exactly one note`() = runTest {
        val vm = viewModel()
        vm.typeBody("a")
        close()

        assertThat(repository.all.map { it.body }).containsExactly("a")
    }

    @Test
    fun `autosave waits for 500 ms of inactivity`() = runTest {
        val vm = viewModel()
        vm.typeBody("H")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS - 1)
        vm.typeBody("Hi")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS - 1)
        assertThat(repository.writes).isEmpty()

        advanceTimeBy(2)
        assertThat(repository.writes).containsExactly("create:|Hi")
        assertThat(vm.state.value.isSaved).isTrue()
    }

    @Test
    fun `later edits update the note created first`() = runTest {
        val vm = viewModel()
        vm.typeBody("One")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)
        vm.typeBody("One two")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)

        assertThat(repository.all.map { it.body }).containsExactly("One two")
    }

    @Test
    fun `leaving the foreground saves at once`() = runTest {
        val vm = viewModel()
        vm.typeBody("Draft")
        vm.onAction(NoteAction.OnLeaveForeground)
        runCurrent()

        assertThat(repository.all.map { it.body }).containsExactly("Draft")
    }

    @Test
    fun `typing again hides saved until the next save`() = runTest {
        val vm = viewModel()
        vm.typeBody("A")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)
        vm.typeBody("AB")

        assertThat(vm.state.value.isSaved).isFalse()
    }

    @Test
    fun `new note emptied after it was saved is dropped on leaving`() = runTest {
        val vm = viewModel()
        vm.typeBody("Oops")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)
        vm.typeBody("")
        close()

        assertThat(repository.all).isEmpty()
    }

    @Test
    fun `save failure is reported and the text is kept`() = runTest {
        val vm = viewModel()
        vm.events.test {
            repository.error = DataError.Local.DISK_FULL
            vm.typeBody("Keep me")
            advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)

            assertThat(awaitItem()).isInstanceOf<NoteEvent.ShowSnackbar>()
            assertThat(vm.state.value.body.text).isEqualTo("Keep me")
        }
    }

    // ---- Existing notes ----

    @Test
    fun `existing note opens in preview without being rewritten`() = runTest {
        val id = repository.add("Plan", "# Week\n\nText")
        val vm = viewModel(NoteArgs(id))
        close()

        assertThat(vm.state.value.mode).isEqualTo(NoteMode.Preview)
        assertThat(vm.state.value.blocks.first()).isInstanceOf<MarkdownBlock.Heading>()
        assertThat(repository.writes).isEmpty()
    }

    @Test
    fun `existing note emptied is kept - not deleted`() = runTest {
        val id = repository.add("Plan", "Text")
        val vm = viewModel(NoteArgs(id))
        vm.onAction(NoteAction.OnTitleChange(""))
        vm.typeBody("")
        close()

        assertThat(repository.note(id)?.body).isEqualTo("")
    }

    @Test
    fun `missing note shows the error and closes`() = runTest {
        val vm = viewModel(NoteArgs(noteId = 99))

        vm.events.test {
            assertThat(awaitItem()).isInstanceOf<NoteEvent.ShowSnackbar>()
            assertThat(awaitItem()).isEqualTo(NoteEvent.Close)
        }
    }

    @Test
    fun `opening from search scrolls to the first match`() = runTest {
        val body = "intro\n\nWe agreed the **plan**"
        val id = repository.add("Meeting", body)
        val vm = viewModel(NoteArgs(id, searchQuery = "PLAN"))

        assertThat(vm.state.value.scrollAnchor).isEqualTo(body.indexOf("plan"))
    }

    // ---- Process death ----

    @Test
    fun `draft in saved state survives process death and gets saved`() = runTest {
        val id = repository.add("Plan", "old")
        val handle = SavedStateHandle(mapOf("draftTitle" to "Plan", "draftBody" to "typed before the kill"))
        val vm = viewModel(NoteArgs(id), handle)

        assertThat(vm.state.value.body.text).isEqualTo("typed before the kill")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)
        assertThat(repository.note(id)?.body).isEqualTo("typed before the kill")
    }

    @Test
    fun `new note created before process death is reopened - not duplicated`() = runTest {
        val first = viewModel()
        first.typeBody("Hello")
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)
        val handle = SavedStateHandle(mapOf("createdNoteId" to repository.all.single().id, "draftBody" to "Hello there"))

        val restored = viewModel(NoteArgs(noteId = null), handle)
        advanceTimeBy(AUTOSAVE_DEBOUNCE_MILLIS + 1)

        assertThat(repository.all.map { it.body }).containsExactly("Hello there")
        assertThat(restored.state.value.body.text).isEqualTo("Hello there")
    }

    @Test
    fun `drafts too large for saved state are not stored there`() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle = handle)
        vm.typeBody("x".repeat(MAX_DRAFT_LENGTH + 1))

        assertThat(handle.get<String>("draftBody")).isNull()
    }

    // ---- Editing ----

    @Test
    fun `enter on a list item continues the list`() = runTest {
        val vm = viewModel()
        vm.typeBody("- Milk")
        vm.typeBody("- Milk\n")

        assertThat(vm.state.value.body.text).isEqualTo("- Milk\n- ")
        assertThat(vm.state.value.body.selection).isEqualTo(TextRange(9))
    }

    @Test
    fun `the text field echoing its raw input does not undo list continuation`() = runTest {
        val vm = viewModel()
        vm.typeBody("- Milk")
        vm.typeBody("- Milk\n")
        vm.typeBody("- Milk\n")

        assertThat(vm.state.value.body.text).isEqualTo("- Milk\n- ")
    }

    @Test
    fun `toolbar formats the selection`() = runTest {
        val vm = viewModel()
        vm.onAction(NoteAction.OnBodyChange(TextFieldValue("make bold", TextRange(5, 9))))
        vm.onAction(NoteAction.OnFormatClick(MarkdownFormat.Bold, clipboardText = null))

        assertThat(vm.state.value.body.text).isEqualTo("make **bold**")
    }

    @Test
    fun `switching to preview saves and parses`() = runTest {
        val vm = viewModel()
        vm.typeBody("# Title")
        vm.onAction(NoteAction.OnToggleModeClick(anchor = 3))
        runCurrent()

        assertThat(vm.state.value.mode).isEqualTo(NoteMode.Preview)
        assertThat(vm.state.value.scrollAnchor).isEqualTo(3)
        assertThat(vm.state.value.blocks).isNotEmpty()
        assertThat(repository.all.map { it.body }).containsExactly("# Title")
    }

    @Test
    fun `double tap in preview edits at that spot`() = runTest {
        val id = repository.add("T", "first\n\nsecond")
        val vm = viewModel(NoteArgs(id))
        vm.onAction(NoteAction.OnPreviewDoubleTap(offset = 7))

        assertThat(vm.state.value.mode).isEqualTo(NoteMode.Edit)
        assertThat(vm.state.value.body.selection).isEqualTo(TextRange(7))
        assertThat(vm.state.value.focusBodyRequest).isTrue()
    }

    @Test
    fun `ticking a checklist item edits the markdown and saves right away`() = runTest {
        val id = repository.add("T", "- [ ] Buy milk")
        val vm = viewModel(NoteArgs(id))
        clock.advance(5.minutes)
        vm.onAction(NoteAction.OnChecklistToggle(checkboxOffset = 2))
        runCurrent()

        assertThat(repository.note(id)?.body).isEqualTo("- [x] Buy milk")
        assertThat(repository.note(id)?.editedAt).isEqualTo(clock.now)
    }

    // ---- Actions ----

    @Test
    fun `pinning saves the pin state`() = runTest {
        val id = repository.add("T", "b")
        val vm = viewModel(NoteArgs(id))
        vm.onAction(NoteAction.OnTogglePinClick)

        assertThat(repository.note(id)?.isPinned).isEqualTo(true)
        assertThat(vm.state.value.isPinned).isTrue()
    }

    @Test
    fun `pinning a new note saves it first`() = runTest {
        val vm = viewModel()
        vm.typeBody("Pin me")
        vm.onAction(NoteAction.OnTogglePinClick)

        assertThat(repository.all.single().isPinned).isTrue()
    }

    @Test
    fun `delete removes the note and closes - and nothing saves it back`() = runTest {
        val id = repository.add("T", "b")
        val vm = viewModel(NoteArgs(id))
        vm.events.test {
            vm.typeBody("edited")
            vm.onAction(NoteAction.OnDeleteClick)
            assertThat(vm.state.value.isDeleteDialogVisible).isTrue()
            vm.onAction(NoteAction.OnDeleteConfirm)

            assertThat(awaitItem()).isEqualTo(NoteEvent.Close)
        }
        close()

        assertThat(repository.all).isEmpty()
    }

    @Test
    fun `cancelling delete keeps the note`() = runTest {
        val id = repository.add("T", "b")
        val vm = viewModel(NoteArgs(id))
        vm.onAction(NoteAction.OnDeleteClick)
        vm.onAction(NoteAction.OnDeleteDismiss)

        assertThat(vm.state.value.isDeleteDialogVisible).isFalse()
        assertThat(repository.note(id)?.body).isEqualTo("b")
        assertThat(repository.writes.none { it.startsWith("delete") }).isTrue()
    }

    @Test
    fun `export hands over the file name and the body - and confirms a save`() = runTest {
        val id = repository.add("Weekly: plan", "a\r\nb")
        val vm = viewModel(NoteArgs(id))
        vm.events.test {
            vm.onAction(NoteAction.OnExportClick)
            assertThat(awaitItem()).isEqualTo(NoteEvent.Export("Weekly- plan.md", "a\nb"))

            vm.onAction(NoteAction.OnExportFinished(ExportResult.Saved))
            val message = (awaitItem() as NoteEvent.ShowSnackbar).message as UiText.Resource
            assertThat(message.args.toList()).containsExactly("Weekly- plan.md")
        }
    }

    @Test
    fun `share sheet and cancelled exports show nothing`() = runTest {
        val id = repository.add("T", "b")
        val vm = viewModel(NoteArgs(id))
        vm.events.test {
            vm.onAction(NoteAction.OnExportClick)
            awaitItem()
            vm.onAction(NoteAction.OnExportFinished(ExportResult.Shared))
            vm.onAction(NoteAction.OnExportFinished(ExportResult.Cancelled))
            expectNoEvents()
        }
    }
}
