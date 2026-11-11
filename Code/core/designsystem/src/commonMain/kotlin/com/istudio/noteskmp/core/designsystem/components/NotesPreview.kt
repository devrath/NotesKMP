package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.istudio.noteskmp.core.designsystem.theme.NotesPlatform
import com.istudio.noteskmp.core.designsystem.theme.NotesSpacing
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

/** Wraps a component preview in the theme on the app background. */
@Composable
internal fun NotesPreview(
    darkTheme: Boolean = false,
    platform: NotesPlatform = NotesPlatform.Android,
    content: @Composable () -> Unit,
) {
    NotesTheme(darkTheme = darkTheme, platform = platform) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(NotesSpacing.space4)) {
                content()
            }
        }
    }
}
