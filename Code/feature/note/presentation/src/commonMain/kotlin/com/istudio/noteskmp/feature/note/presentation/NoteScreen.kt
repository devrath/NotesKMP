package com.istudio.noteskmp.feature.note.presentation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.istudio.noteskmp.core.designsystem.components.FormattingAction
import com.istudio.noteskmp.core.designsystem.components.FormattingToolbar
import com.istudio.noteskmp.core.designsystem.components.NotesBackButton
import com.istudio.noteskmp.core.designsystem.components.NotesDropdownMenu
import com.istudio.noteskmp.core.designsystem.components.NotesIconButton
import com.istudio.noteskmp.core.designsystem.components.NotesSnackbarHost
import com.istudio.noteskmp.core.designsystem.components.NotesTopAppBar
import com.istudio.noteskmp.core.designsystem.components.SavedStatus
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.core.domain.note.noteTitle
import com.istudio.noteskmp.core.domain.note.summarizeNoteText
import com.istudio.noteskmp.core.presentation.DeleteNoteDialog
import com.istudio.noteskmp.core.presentation.NoteActionsMenuItems
import com.istudio.noteskmp.core.presentation.ObserveAsEvents
import com.istudio.noteskmp.core.presentation.asString
import com.istudio.noteskmp.core.presentation.file.rememberMarkdownExporter
import com.istudio.noteskmp.feature.note.domain.MarkdownFormat
import com.istudio.noteskmp.feature.note.domain.indexOfBlockAt
import com.istudio.noteskmp.feature.note.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.note.presentation.generated.resources.back_label
import com.istudio.noteskmp.feature.note.presentation.generated.resources.cd_edit
import com.istudio.noteskmp.feature.note.presentation.generated.resources.cd_more
import com.istudio.noteskmp.feature.note.presentation.generated.resources.cd_preview
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteRoot(
    args: NoteArgs,
    onBack: () -> Unit,
    viewModel: NoteViewModel = koinViewModel { parametersOf(args) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val exporter = rememberMarkdownExporter { viewModel.onAction(NoteAction.OnExportFinished(it)) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NoteEvent.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            is NoteEvent.Export -> exporter.export(event.fileName, event.content)
            NoteEvent.Close -> onBack()
        }
    }
    // Switching apps, locking the screen or rotating saves straight away.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onAction(NoteAction.OnLeaveForeground) }

    NoteScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        onBackClick = onBack,
    )
}

@Suppress("DEPRECATION") // LocalClipboardManager: plain text is all the editor needs.
@Composable
fun NoteScreen(
    state: NoteState,
    onAction: (NoteAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
) {
    val editorScroll = rememberScrollState()
    val previewList = rememberLazyListState()
    val bodyFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboardManager.current
    val isEditing = state.mode == NoteMode.Edit
    val titleOffset = if (state.title.isNotBlank()) 1 else 0

    fun currentAnchor(): Int =
        if (isEditing) {
            editorAnchor(editorScroll, state.body.text.length)
        } else {
            state.blocks.getOrNull(previewList.firstVisibleItemIndex - titleOffset)?.sourceStart ?: 0
        }

    fun format(format: MarkdownFormat) = onAction(NoteAction.OnFormatClick(format, clipboard.getText()?.text))

    ScrollToAnchor(state, editorScroll, previewList, titleOffset)

    LaunchedEffect(state.focusBodyRequest, state.mode, state.isLoading) {
        if (state.focusBodyRequest && isEditing && !state.isLoading) {
            withFrameNanos { }
            bodyFocus.requestFocus()
            keyboard?.show()
            onAction(NoteAction.OnBodyFocused)
        }
    }

    Scaffold(
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown || !(event.isCtrlPressed || event.isMetaPressed)) {
                return@onPreviewKeyEvent false
            }
            when (event.key) {
                Key.E -> onAction(NoteAction.OnToggleModeClick(currentAnchor())).let { true }
                Key.B -> isEditing.also { if (it) format(MarkdownFormat.Bold) }
                Key.I -> isEditing.also { if (it) format(MarkdownFormat.Italic) }
                Key.K -> isEditing.also { if (it) format(MarkdownFormat.Link) }
                else -> false
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NoteTopBar(
                state = state,
                onAction = onAction,
                onBackClick = onBackClick,
                onToggleModeClick = { onAction(NoteAction.OnToggleModeClick(currentAnchor())) },
            )
        },
        snackbarHost = { NotesSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> Unit
                    isEditing -> MarkdownEditor(
                        title = state.title,
                        body = state.body,
                        onTitleChange = { onAction(NoteAction.OnTitleChange(it)) },
                        onBodyChange = { onAction(NoteAction.OnBodyChange(it)) },
                        scrollState = editorScroll,
                        bodyFocusRequester = bodyFocus,
                    )
                    else -> MarkdownPreview(
                        title = state.title,
                        blocks = state.blocks,
                        listState = previewList,
                        linkMenu = state.linkMenu,
                        onChecklistToggle = { onAction(NoteAction.OnChecklistToggle(it)) },
                        onDoubleTap = { onAction(NoteAction.OnPreviewDoubleTap(it)) },
                        onLinkLongPress = { index, url -> onAction(NoteAction.OnLinkLongPress(index, url)) },
                        onLinkMenuDismiss = { onAction(NoteAction.OnLinkMenuDismiss) },
                        onCopyLink = { clipboard.setText(AnnotatedString(it)) },
                    )
                }
            }
            // The toolbar rides on the keyboard and hides with it.
            val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
            if (isEditing && isKeyboardVisible) {
                FormattingToolbar(onActionClick = { format(it.toMarkdownFormat()) })
            }
        }
    }

    if (state.isDeleteDialogVisible) {
        DeleteNoteDialog(
            noteTitle = noteTitle(state.title, summarizeNoteText(state.title, state.body.text).fallbackTitle).asString(),
            onConfirm = { onAction(NoteAction.OnDeleteConfirm) },
            onDismiss = { onAction(NoteAction.OnDeleteDismiss) },
        )
    }
}

@Composable
private fun NoteTopBar(
    state: NoteState,
    onAction: (NoteAction) -> Unit,
    onBackClick: () -> Unit,
    onToggleModeClick: () -> Unit,
) {
    val isEditing = state.mode == NoteMode.Edit
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    NotesTopAppBar(
        navigationIcon = { NotesBackButton(backLabel = stringResource(Res.string.back_label), onClick = onBackClick) },
        actions = {
            SavedStatus(isVisible = isEditing && state.isSaved)
            // The toggle names the mode it switches to.
            NotesIconButton(
                icon = if (isEditing) NotesIcons.Preview else NotesIcons.Edit,
                contentDescription = stringResource(if (isEditing) Res.string.cd_preview else Res.string.cd_edit),
                onClick = onToggleModeClick,
                enabled = !state.isLoading,
            )
            Box {
                NotesIconButton(
                    icon = if (isIos) NotesIcons.MoreCircle else NotesIcons.MoreVert,
                    contentDescription = stringResource(Res.string.cd_more),
                    onClick = { onAction(NoteAction.OnMenuClick) },
                    enabled = !state.isLoading,
                )
                NotesDropdownMenu(expanded = state.isMenuOpen, onDismissRequest = { onAction(NoteAction.OnMenuDismiss) }) {
                    NoteActionsMenuItems(
                        isPinned = state.isPinned,
                        onTogglePinClick = { onAction(NoteAction.OnTogglePinClick) },
                        onExportClick = { onAction(NoteAction.OnExportClick) },
                        onDeleteClick = { onAction(NoteAction.OnDeleteClick) },
                    )
                }
            }
        },
    )
}

/** Scrolls the active mode to [NoteState.scrollAnchor] each time a new scroll is requested. */
@Composable
private fun ScrollToAnchor(
    state: NoteState,
    editorScroll: ScrollState,
    previewList: LazyListState,
    titleOffset: Int,
) {
    val applied = remember { mutableIntStateOf(0) }
    LaunchedEffect(state.scrollRequest, state.mode, state.blocks) {
        if (state.isLoading || applied.intValue == state.scrollRequest) return@LaunchedEffect
        when (state.mode) {
            NoteMode.Preview -> {
                // Wait for the parsed blocks of a non-empty note.
                if (state.blocks.isEmpty() && state.body.text.isNotBlank()) return@LaunchedEffect
                val index = if (state.scrollAnchor == 0) 0 else state.blocks.indexOfBlockAt(state.scrollAnchor) + titleOffset
                previewList.scrollToItem(index)
            }
            NoteMode.Edit -> {
                withFrameNanos { }
                val length = state.body.text.length
                if (length > 0) editorScroll.scrollTo((editorScroll.maxValue * state.scrollAnchor / length.toFloat()).toInt())
            }
        }
        applied.intValue = state.scrollRequest
    }
}

/** The editor's scroll position as a character offset, for a proportional handover. */
private fun editorAnchor(scroll: ScrollState, length: Int): Int =
    if (scroll.maxValue <= 0) 0 else (length * (scroll.value / scroll.maxValue.toFloat())).toInt()

private fun FormattingAction.toMarkdownFormat(): MarkdownFormat = when (this) {
    FormattingAction.Heading -> MarkdownFormat.Heading
    FormattingAction.Bold -> MarkdownFormat.Bold
    FormattingAction.Italic -> MarkdownFormat.Italic
    FormattingAction.BulletedList -> MarkdownFormat.BulletedList
    FormattingAction.NumberedList -> MarkdownFormat.NumberedList
    FormattingAction.Checklist -> MarkdownFormat.Checklist
    FormattingAction.Quote -> MarkdownFormat.Quote
    FormattingAction.InlineCode -> MarkdownFormat.InlineCode
    FormattingAction.CodeBlock -> MarkdownFormat.CodeBlock
    FormattingAction.Link -> MarkdownFormat.Link
}

@Preview(name = "Edit", widthDp = 360, heightDp = 640)
@Composable
private fun NoteScreenEditPreview() {
    NotesTheme(platform = NotesPlatform.Android) {
        NoteScreen(
            state = NoteState(
                isLoading = false,
                title = "Weekly plan",
                body = TextFieldValue("## This week\nFocus on **search** and *import errors*.\n\n- [x] Ship the notes list\n- [ ] Finish search"),
                isSaved = true,
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
        )
    }
}

@Preview(name = "Preview, iOS dark", widthDp = 393, heightDp = 700)
@Composable
private fun NoteScreenPreviewModePreview() {
    val body = "## This week\nFocus on **search** and *import errors*.\n\n- [x] Ship the notes list\n- [ ] Finish search\n\n> Writing comes first."
    NotesTheme(darkTheme = true, platform = NotesPlatform.Ios) {
        NoteScreen(
            state = NoteState(
                isLoading = false,
                title = "Weekly plan",
                body = TextFieldValue(body),
                mode = NoteMode.Preview,
                blocks = com.istudio.noteskmp.feature.note.domain.parseMarkdown(body),
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
        )
    }
}
