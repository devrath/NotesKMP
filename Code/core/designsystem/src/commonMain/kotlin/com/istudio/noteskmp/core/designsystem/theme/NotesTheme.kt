package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalNotesColors = staticCompositionLocalOf { LightNotesColors }
private val LocalNotesTypography = staticCompositionLocalOf<NotesTypography> {
    error("NotesTypography is only available inside NotesTheme")
}
private val LocalNotesPlatform = staticCompositionLocalOf { currentNotesPlatform }
private val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * App theme. Light and dark follow the system setting. [platform] defaults to the running
 * platform; previews override it to show the other platform's conventions.
 */
@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    platform: NotesPlatform = currentNotesPlatform,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkNotesColors else LightNotesColors
    val typography = notesTypography(platform)
    CompositionLocalProvider(
        LocalNotesColors provides colors,
        LocalNotesTypography provides typography,
        LocalNotesPlatform provides platform,
        LocalReduceMotion provides isSystemReduceMotionEnabled(),
    ) {
        MaterialTheme(
            colorScheme = remember(colors, darkTheme) { colors.toColorScheme(darkTheme) },
            typography = remember(typography) { typography.toMaterialTypography() },
            shapes = NotesShapes,
            content = content,
        )
    }
}

/** Token roles that Material 3 has no slot for, such as Markdown markers and search highlights. */
object NotesTheme {
    val colors: NotesColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNotesColors.current

    val typography: NotesTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalNotesTypography.current

    val platform: NotesPlatform
        @Composable
        @ReadOnlyComposable
        get() = LocalNotesPlatform.current

    /** Pass to [NotesMotion.spec] so animations become instant when the system asks. */
    val reduceMotion: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReduceMotion.current
}
