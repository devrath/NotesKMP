package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesRadius
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

/**
 * Hosts import and export feedback. Place it above the create button so it never covers it.
 * Show messages with at least [androidx.compose.material3.SnackbarDuration.Short] (4 s).
 */
@Composable
fun NotesSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        NotesSnackbar(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onActionClick = data::performAction,
        )
    }
}

/**
 * Snackbar (Android) or floating toast (iOS) in the inverse colors, with an optional action
 * such as "Open". Announced politely by screen readers.
 */
@Composable
fun NotesSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {},
) {
    val isIos = NotesTheme.platform == NotesPlatform.Ios
    val colors = NotesTheme.colors
    Snackbar(
        modifier = modifier
            .padding(horizontal = NotesSpacing.space3)
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(if (isIos) NotesRadius.lg else NotesRadius.xs),
        containerColor = colors.snackbarBackground,
        contentColor = colors.snackbarForeground,
        action = actionLabel?.let {
            {
                TextButton(onClick = onActionClick) {
                    Text(
                        text = it,
                        style = NotesTheme.typography.label.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.snackbarAction,
                    )
                }
            }
        },
    ) {
        Text(text = message, style = NotesTheme.typography.label)
    }
}

@Composable
private fun SnackbarSample() {
    Column(verticalArrangement = Arrangement.spacedBy(NotesSpacing.space3)) {
        NotesSnackbar(message = "Imported “Ideas”", actionLabel = "Open")
        NotesSnackbar(message = "This file is too large to import (max 5 MB)")
    }
}

@Preview(name = "Android", widthDp = 360)
@Composable
private fun NotesSnackbarPreview() {
    NotesPreview { SnackbarSample() }
}

@Preview(name = "Android dark", widthDp = 360)
@Composable
private fun NotesSnackbarDarkPreview() {
    NotesPreview(darkTheme = true) { SnackbarSample() }
}

@Preview(name = "iOS toast", widthDp = 393)
@Composable
private fun NotesSnackbarIosPreview() {
    NotesPreview(platform = NotesPlatform.Ios) { SnackbarSample() }
}

@Preview(name = "iOS toast dark", widthDp = 393)
@Composable
private fun NotesSnackbarIosDarkPreview() {
    NotesPreview(darkTheme = true, platform = NotesPlatform.Ios) { SnackbarSample() }
}

@Preview(name = "Android, 200% text", widthDp = 360)
@Composable
private fun NotesSnackbarLargeFontPreview() {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
        NotesPreview { NotesSnackbar(message = "This file is too large to import (max 5 MB)", actionLabel = "Open") }
    }
}
