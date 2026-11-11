package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.istudio.noteskmp.core.designsystem.generated.resources.Res
import com.istudio.noteskmp.core.designsystem.generated.resources.cd_back
import com.istudio.noteskmp.core.designsystem.generated.resources.cd_clear_search
import com.istudio.noteskmp.core.designsystem.generated.resources.search_cancel
import com.istudio.noteskmp.core.designsystem.generated.resources.search_entry
import com.istudio.noteskmp.core.designsystem.generated.resources.search_placeholder
import com.istudio.noteskmp.core.designsystem.icon.NotesIcons
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme
import org.jetbrains.compose.resources.stringResource

private val AndroidSearchBarHeight = 64.dp
private val IosFieldHeight = 36.dp
private val IosFieldRadius = 10.dp
private val IosSearchIconSize = 18.dp

/**
 * Search bar for the search screen. Android: back arrow, borderless field and a clear button
 * over a hairline. iOS: rounded field with a filled clear button, then Cancel.
 * Results update as the user types, so there is no submit step.
 *
 * @param onCloseClick back (Android) or Cancel (iOS).
 */
@Composable
fun NotesSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onSearch: () -> Unit = {},
) {
    val keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    val keyboardActions = KeyboardActions(onSearch = { onSearch() })
    if (NotesTheme.platform == NotesPlatform.Ios) {
        IosSearchField(query, onQueryChange, onCloseClick, focusRequester, keyboardOptions, keyboardActions, modifier)
    } else {
        AndroidSearchField(query, onQueryChange, onCloseClick, focusRequester, keyboardOptions, keyboardActions, modifier)
    }
}

@Composable
private fun AndroidSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    focusRequester: FocusRequester,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier,
) {
    Column(modifier = modifier.semantics { isTraversalGroup = true }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AndroidSearchBarHeight)
                .padding(horizontal = NotesSpacing.space1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(NotesSpacing.space1),
        ) {
            NotesIconButton(
                icon = NotesIcons.Back,
                contentDescription = stringResource(Res.string.cd_back),
                onClick = onCloseClick,
            )
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                textStyle = NotesTheme.typography.body,
                placeholder = { Text(text = stringResource(Res.string.search_placeholder), style = NotesTheme.typography.body) },
                singleLine = true,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            if (query.isNotEmpty()) {
                NotesIconButton(
                    icon = NotesIcons.Close,
                    contentDescription = stringResource(Res.string.cd_clear_search),
                    onClick = { onQueryChange("") },
                    isMuted = true,
                )
            }
        }
        HorizontalDivider(color = NotesTheme.colors.outline)
    }
}

@Composable
private fun IosSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    focusRequester: FocusRequester,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = NotesSpacing.space2)
            .semantics { isTraversalGroup = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = NotesSpacing.space4, end = NotesSpacing.space2)
                .focusRequester(focusRequester),
            textStyle = NotesTheme.typography.body.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                IosFieldFrame {
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.search_placeholder),
                                style = NotesTheme.typography.body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        innerTextField()
                    }
                    if (query.isNotEmpty()) {
                        NotesIconButton(
                            icon = NotesIcons.ClearFilled,
                            contentDescription = stringResource(Res.string.cd_clear_search),
                            onClick = { onQueryChange("") },
                            isMuted = true,
                        )
                    }
                }
            },
        )
        TextButton(onClick = onCloseClick, modifier = Modifier.padding(end = NotesSpacing.space2)) {
            Text(
                text = stringResource(Res.string.search_cancel),
                style = NotesTheme.typography.body,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun IosFieldFrame(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = IosFieldHeight)
            .clip(RoundedCornerShape(IosFieldRadius))
            .background(NotesTheme.colors.surface2)
            .padding(start = NotesSpacing.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = NotesIcons.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(IosSearchIconSize),
        )
        content()
    }
}

/**
 * The tappable "Search" field under the iOS large title on the notes list. It opens the
 * search screen; typing happens there.
 */
@Composable
fun NotesSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(Res.string.search_entry)
    Box(
        modifier = modifier
            .padding(start = NotesSpacing.space4, end = NotesSpacing.space4, bottom = NotesSpacing.space2)
            .clip(RoundedCornerShape(IosFieldRadius))
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
            // Keeps the 44 pt target although the field is drawn 36 pt tall.
            .heightIn(min = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        IosFieldFrame {
            Text(
                text = label,
                style = NotesTheme.typography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(name = "Android, empty", widthDp = 360)
@Composable
private fun NotesSearchFieldEmptyPreview() {
    NotesPreview { NotesSearchField(query = "", onQueryChange = {}, onCloseClick = {}) }
}

@Preview(name = "Android, query", widthDp = 360)
@Composable
private fun NotesSearchFieldPreview() {
    NotesPreview { NotesSearchField(query = "plan", onQueryChange = {}, onCloseClick = {}) }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun NotesSearchFieldDarkPreview() {
    NotesPreview(darkTheme = true) { NotesSearchField(query = "plan", onQueryChange = {}, onCloseClick = {}) }
}

@Preview(name = "iOS, empty", widthDp = 393)
@Composable
private fun NotesSearchFieldIosEmptyPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { NotesSearchField(query = "", onQueryChange = {}, onCloseClick = {}) }
}

@Preview(name = "iOS, query", widthDp = 393)
@Composable
private fun NotesSearchFieldIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { NotesSearchField(query = "plan", onQueryChange = {}, onCloseClick = {}) }
}

@Preview(name = "iOS dark", widthDp = 393)
@Composable
private fun NotesSearchFieldIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) {
        NotesSearchField(query = "plan", onQueryChange = {}, onCloseClick = {})
    }
}

@Preview(name = "iOS search entry", widthDp = 393)
@Composable
private fun NotesSearchEntryPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { NotesSearchEntry(onClick = {}) }
}

@Preview(name = "iOS search entry dark", widthDp = 393)
@Composable
private fun NotesSearchEntryDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { NotesSearchEntry(onClick = {}) }
}
