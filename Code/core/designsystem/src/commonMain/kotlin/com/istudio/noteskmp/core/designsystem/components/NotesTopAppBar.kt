package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.cd_back
import com.istudio.noteskmp.core.designsystem.generated.resources.saved_status
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesMotion
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import org.jetbrains.compose.resources.stringResource

private val AndroidBarHeight = 64.dp
private val IosBarHeight = 44.dp
private val IosBackChevronSize = 28.dp

/**
 * Flat top bar on the app background: 64 dp on Android, a 44 pt navigation bar on iOS.
 * Pair it with [NotesLargeTitle] on iOS for the notes list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        expandedHeight = if (isIos) IosBarHeight else AndroidBarHeight,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

/** iOS large-title row under the navigation bar. */
@Composable
fun NotesLargeTitle(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = NotesSpacing.space5, end = NotesSpacing.space5, top = 2.dp, bottom = NotesSpacing.space2),
    ) {
        content()
    }
}

/**
 * Back button. Android shows the arrow; iOS shows the chevron with [backLabel], the title of
 * the screen it returns to. Both are announced as "Back".
 */
@Composable
fun NotesBackButton(
    backLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(Res.string.cd_back)
    if (NotesTheme.platform == NotesPlatform.Ios) {
        TextButton(
            onClick = onClick,
            modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        ) {
            Icon(
                painter = NotesIcons.ChevronLeft,
                contentDescription = null,
                modifier = Modifier.size(IosBackChevronSize),
            )
            Text(text = backLabel, style = NotesTheme.typography.body)
        }
    } else {
        NotesIconButton(
            icon = NotesIcons.Back,
            contentDescription = description,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

/**
 * Quiet "Saved" status for the note top bar. It always keeps its space so it never moves
 * content, fades in 120 ms (instant with reduce-motion) and is a polite live region.
 */
@Composable
fun SavedStatus(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = NotesMotion.spec(NotesMotion.DURATION_FAST_MILLIS, NotesTheme.reduceMotion),
    )
    val text = stringResource(Res.string.saved_status)
    Text(
        text = text,
        style = NotesTheme.typography.meta,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = modifier
            .padding(horizontal = NotesSpacing.space1)
            .graphicsLayer { this.alpha = alpha }
            .then(
                if (isVisible) {
                    Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                } else {
                    Modifier.clearAndSetSemantics {}
                },
            ),
    )
}

@Composable
private fun TopBarsSample(isSaved: Boolean) {
    Column {
        NotesTopAppBar(
            title = { NotesWordmark(text = "Notes") },
            actions = {
                NotesIconButton(icon = NotesIcons.Search, contentDescription = "Search notes", onClick = {})
                NotesIconButton(icon = NotesIcons.Import, contentDescription = "Import Markdown file", onClick = {})
            },
        )
        NotesTopAppBar(
            navigationIcon = { NotesBackButton(backLabel = "Notes", onClick = {}) },
            actions = {
                SavedStatus(isVisible = isSaved)
                NotesIconButton(icon = NotesIcons.Preview, contentDescription = "Preview", onClick = {})
                NotesIconButton(icon = NotesIcons.MoreVert, contentDescription = "More options", onClick = {})
            },
        )
    }
}

@Composable
private fun IosTopBarsSample() {
    Column {
        NotesTopAppBar(
            actions = {
                NotesIconButton(icon = NotesIcons.Import, contentDescription = "Import Markdown file", onClick = {})
            },
        )
        NotesLargeTitle { NotesWordmark(text = "Notes") }
        Spacer(modifier = Modifier.width(NotesSpacing.space4))
        NotesTopAppBar(
            navigationIcon = { NotesBackButton(backLabel = "Notes", onClick = {}) },
            actions = {
                NotesIconButton(icon = NotesIcons.Edit, contentDescription = "Edit", onClick = {})
                NotesIconButton(icon = NotesIcons.MoreCircle, contentDescription = "More options", onClick = {})
            },
        )
    }
}

@Preview(name = "Android, saved", widthDp = 360)
@Composable
private fun NotesTopAppBarPreview() {
    NotesPreview { TopBarsSample(isSaved = true) }
}

@Preview(name = "Android, not saved", widthDp = 360)
@Composable
private fun NotesTopAppBarUnsavedPreview() {
    NotesPreview { TopBarsSample(isSaved = false) }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun NotesTopAppBarDarkPreview() {
    NotesPreview(darkTheme = true) { TopBarsSample(isSaved = true) }
}

@Preview(name = "iOS", widthDp = 393)
@Composable
private fun NotesTopAppBarIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { IosTopBarsSample() }
}

@Preview(name = "iOS dark", widthDp = 393)
@Composable
private fun NotesTopAppBarIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { IosTopBarsSample() }
}
