package com.istudio.noteskmp.feature.notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.istudio.noteskmp.core.designsystem.components.NoteEntry
import com.istudio.noteskmp.core.designsystem.components.NoteEntryPosition
import com.istudio.noteskmp.core.designsystem.components.NoteGroupLabel
import com.istudio.noteskmp.core.designsystem.components.NotesAccentButton
import com.istudio.noteskmp.core.designsystem.components.NotesComposeButton
import com.istudio.noteskmp.core.designsystem.components.NotesDropdownMenu
import com.istudio.noteskmp.core.designsystem.components.NotesEmptyState
import com.istudio.noteskmp.core.designsystem.components.NotesExtendedFab
import com.istudio.noteskmp.core.designsystem.components.NotesIconButton
import com.istudio.noteskmp.core.designsystem.components.NotesLargeTitle
import com.istudio.noteskmp.core.designsystem.components.NotesOutlinedButton
import com.istudio.noteskmp.core.designsystem.components.NotesSearchEntry
import com.istudio.noteskmp.core.designsystem.components.NotesSnackbarHost
import com.istudio.noteskmp.core.designsystem.components.NotesTopAppBar
import com.istudio.noteskmp.core.designsystem.components.NotesWordmark
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesMotion
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import com.istudio.noteskmp.core.domain.note.NoteTitle
import com.istudio.noteskmp.core.presentation.DeleteNoteDialog
import com.istudio.noteskmp.core.presentation.NoteActionsMenuItems
import com.istudio.noteskmp.core.presentation.ObserveAsEvents
import com.istudio.noteskmp.core.presentation.asString
import com.istudio.noteskmp.core.presentation.file.rememberMarkdownExporter
import com.istudio.noteskmp.core.presentation.file.rememberMarkdownFilePicker
import com.istudio.noteskmp.core.presentation.time.asString
import com.istudio.noteskmp.core.presentation.time.relativeTime
import com.istudio.noteskmp.feature.notes.domain.IMPORT_MAX_BYTES
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.Res
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.app_name
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.cd_import
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.cd_search_notes
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.empty_message
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.empty_title
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.group_notes
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.group_pinned
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.import_file
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.new_note
import com.istudio.noteskmp.feature.notes.presentation.generated.resources.notes_count
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant
import com.istudio.noteskmp.core.presentation.generated.resources.Res as CoreRes
import com.istudio.noteskmp.core.presentation.generated.resources.action_open

/** Room for the FAB (56 dp plus margins) so the last entry can scroll clear of it. */
private val ListBottomPadding = 96.dp

@Composable
fun NotesListRoot(
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: NotesListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val exporter = rememberMarkdownExporter { viewModel.onAction(NotesListAction.OnExportFinished(it)) }
    val filePicker = rememberMarkdownFilePicker(IMPORT_MAX_BYTES) {
        viewModel.onAction(NotesListAction.OnFilePicked(it))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NotesListEvent.Export -> exporter.export(event.fileName, event.content)
            is NotesListEvent.ShowSnackbar -> scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = event.message.asStringAsync(),
                    actionLabel = event.openNoteId?.let { getString(CoreRes.string.action_open) },
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) event.openNoteId?.let(onNoteClick)
            }
        }
    }

    NotesListScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        onNoteClick = onNoteClick,
        onCreateNoteClick = onCreateNoteClick,
        onSearchClick = onSearchClick,
        onImportClick = filePicker::launch,
    )
}

@Composable
fun NotesListScreen(
    state: NotesListState,
    onAction: (NotesListAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: () -> Unit,
    onSearchClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    val shortcutFocus = remember { FocusRequester() }
    LaunchedEffect(shortcutFocus) { shortcutFocus.requestFocus() }

    Scaffold(
        modifier = Modifier
            .focusRequester(shortcutFocus)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || !(event.isCtrlPressed || event.isMetaPressed)) {
                    return@onPreviewKeyEvent false
                }
                when (event.key) {
                    Key.N -> onCreateNoteClick().let { true }
                    Key.F -> onSearchClick().let { true }
                    else -> false
                }
            },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isIos) {
                IosListHeader(onSearchClick = onSearchClick, onImportClick = onImportClick)
            } else {
                AndroidListTopBar(onSearchClick = onSearchClick, onImportClick = onImportClick)
            }
        },
        floatingActionButton = {
            // The empty state has its own create button, so the FAB steps aside.
            if (!isIos && !state.isEmpty) {
                NotesExtendedFab(text = stringResource(Res.string.new_note), onClick = onCreateNoteClick)
            }
        },
        bottomBar = {
            if (isIos) IosListToolbar(noteCount = state.noteCount, onCreateNoteClick = onCreateNoteClick)
        },
        snackbarHost = { NotesSnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> Unit
                state.isEmpty -> NotesEmptyState(
                    icon = NotesIcons.Note,
                    title = stringResource(Res.string.empty_title),
                    message = stringResource(Res.string.empty_message),
                    modifier = Modifier.padding(top = NotesSpacing.space10),
                ) {
                    NotesAccentButton(
                        text = stringResource(Res.string.new_note),
                        icon = NotesIcons.Plus,
                        onClick = onCreateNoteClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    NotesOutlinedButton(
                        text = stringResource(Res.string.import_file),
                        icon = NotesIcons.Import,
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> NotesList(state = state, onAction = onAction, onNoteClick = onNoteClick)
            }
        }
    }

    state.pendingDelete?.let { pending ->
        DeleteNoteDialog(
            noteTitle = pending.title.asString(),
            onConfirm = { onAction(NotesListAction.OnDeleteConfirm) },
            onDismiss = { onAction(NotesListAction.OnDeleteDismiss) },
        )
    }
}

@Composable
private fun AndroidListTopBar(onSearchClick: () -> Unit, onImportClick: () -> Unit) {
    NotesTopAppBar(
        title = { NotesWordmark(text = stringResource(Res.string.app_name)) },
        actions = {
            NotesIconButton(
                icon = NotesIcons.Search,
                contentDescription = stringResource(Res.string.cd_search_notes),
                onClick = onSearchClick,
            )
            NotesIconButton(
                icon = NotesIcons.Import,
                contentDescription = stringResource(Res.string.cd_import),
                onClick = onImportClick,
            )
        },
    )
}

@Composable
private fun IosListHeader(onSearchClick: () -> Unit, onImportClick: () -> Unit) {
    Column {
        NotesTopAppBar(
            actions = {
                NotesIconButton(
                    icon = NotesIcons.Import,
                    contentDescription = stringResource(Res.string.cd_import),
                    onClick = onImportClick,
                )
            },
        )
        NotesLargeTitle { NotesWordmark(text = stringResource(Res.string.app_name)) }
        NotesSearchEntry(onClick = onSearchClick)
    }
}

/** iOS bottom toolbar: the note count and the create button within thumb reach. */
@Composable
private fun IosListToolbar(noteCount: Int, onCreateNoteClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            HorizontalDivider(thickness = 0.5.dp, color = NotesTheme.colors.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = NotesSpacing.space5, end = NotesSpacing.space4, top = NotesSpacing.space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (noteCount > 0) pluralStringResource(Res.plurals.notes_count, noteCount, noteCount) else "",
                    style = NotesTheme.typography.meta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                NotesComposeButton(
                    contentDescription = stringResource(Res.string.new_note),
                    onClick = onCreateNoteClick,
                )
            }
        }
    }
}

/** One row of the list: a group label or an entry. */
private sealed interface ListRow {
    val key: String

    data class Label(val text: StringResource) : ListRow {
        override val key: String get() = "label-$text"
    }

    data class Entry(val note: NoteEntryUi, val position: NoteEntryPosition) : ListRow {
        override val key: String get() = "note-${note.id}"
    }
}

private fun listRows(state: NotesListState): List<ListRow> = buildList {
    fun group(label: StringResource, notes: List<NoteEntryUi>) {
        if (notes.isEmpty()) return
        add(ListRow.Label(label))
        notes.forEachIndexed { index, note ->
            val position = when {
                notes.size == 1 -> NoteEntryPosition.Single
                index == 0 -> NoteEntryPosition.First
                index == notes.lastIndex -> NoteEntryPosition.Last
                else -> NoteEntryPosition.Middle
            }
            add(ListRow.Entry(note, position))
        }
    }
    group(Res.string.group_pinned, state.pinned)
    group(Res.string.group_notes, state.others)
}

@Composable
private fun NotesList(
    state: NotesListState,
    onAction: (NotesListAction) -> Unit,
    onNoteClick: (Long) -> Unit,
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    val rows = remember(state.pinned, state.others) { listRows(state) }
    val listState = rememberLazyListState()
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val reduceMotion = NotesTheme.reduceMotion

    LaunchedEffect(state.scrollToNoteId, rows) {
        val target = state.scrollToNoteId ?: return@LaunchedEffect
        val index = rows.indexOfFirst { it is ListRow.Entry && it.note.id == target }
        if (index >= 0) {
            listState.animateScrollToItem(index)
            onAction(NotesListAction.OnScrolledToNote)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = if (isIos) NotesSpacing.space4 else NotesSpacing.space3,
            end = if (isIos) NotesSpacing.space4 else NotesSpacing.space3,
            top = NotesSpacing.space1,
            bottom = if (isIos) NotesSpacing.space4 else ListBottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(if (isIos) 0.dp else NotesSpacing.space2),
    ) {
        items(items = rows, key = { it.key }, contentType = { it::class }) { row ->
            val itemModifier = Modifier.animateItem(
                fadeInSpec = null,
                fadeOutSpec = null,
                placementSpec = if (reduceMotion) {
                    null
                } else {
                    NotesMotion.spec(NotesMotion.DURATION_SLOW_MILLIS, reduceMotion = false, easing = NotesMotion.EasingEmphasized)
                },
            )
            when (row) {
                is ListRow.Label -> NoteGroupLabel(text = stringResource(row.text), modifier = itemModifier)
                is ListRow.Entry -> Box(modifier = itemModifier) {
                    val note = row.note
                    NoteEntry(
                        title = note.title.asString(),
                        preview = note.preview,
                        editedTime = relativeTime(note.editedAt, state.now, timeZone).asString(),
                        isPinned = note.isPinned,
                        onClick = { onNoteClick(note.id) },
                        onLongClick = { onAction(NotesListAction.OnNoteLongClick(note.id)) },
                        isFallbackTitle = note.title !is NoteTitle.Typed,
                        isSelected = state.menuNoteId == note.id,
                        isRecentlyImported = state.recentlyImportedId == note.id,
                        position = row.position,
                    )
                    NotesDropdownMenu(
                        expanded = state.menuNoteId == note.id,
                        onDismissRequest = { onAction(NotesListAction.OnMenuDismiss) },
                    ) {
                        NoteActionsMenuItems(
                            isPinned = note.isPinned,
                            onTogglePinClick = { onAction(NotesListAction.OnTogglePinClick(note.id)) },
                            onExportClick = { onAction(NotesListAction.OnExportClick(note.id)) },
                            onDeleteClick = { onAction(NotesListAction.OnDeleteClick(note.id)) },
                        )
                    }
                }
            }
        }
    }
}

private val PreviewNow = Instant.parse("2026-03-10T12:00:00Z")

private val PreviewState = NotesListState(
    isLoading = false,
    now = PreviewNow,
    pinned = listOf(
        NoteEntryUi(1, NoteTitle.Typed("Weekly plan"), "This week: focus on search and import errors.", Instant.parse("2026-03-09T09:00:00Z"), true),
    ),
    others = listOf(
        NoteEntryUi(2, NoteTitle.Typed("Café ideas"), "Oat flat white with cardamom.", PreviewNow, false),
        NoteEntryUi(3, NoteTitle.Fallback("Groceries for the weekend"), "Lemons, oat milk, sourdough.", Instant.parse("2026-03-10T10:00:00Z"), false),
    ),
)

@Preview(name = "Android", widthDp = 360, heightDp = 640)
@Composable
private fun NotesListScreenPreview() {
    NotesTheme(platform = NotesPlatform.Android) {
        NotesListScreen(PreviewState, {}, remember { SnackbarHostState() }, {}, {}, {}, {})
    }
}

@Preview(name = "iOS dark", widthDp = 393, heightDp = 700)
@Composable
private fun NotesListScreenIosDarkPreview() {
    NotesTheme(darkTheme = true, platform = NotesPlatform.Ios) {
        NotesListScreen(PreviewState, {}, remember { SnackbarHostState() }, {}, {}, {}, {})
    }
}

@Preview(name = "Empty", widthDp = 360, heightDp = 640)
@Composable
private fun NotesListScreenEmptyPreview() {
    NotesTheme(platform = NotesPlatform.Android) {
        NotesListScreen(NotesListState(isLoading = false), {}, remember { SnackbarHostState() }, {}, {}, {}, {})
    }
}
