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

/** App theme. Light and dark follow the system setting. */
@Composable
fun NotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkNotesColors else LightNotesColors
    val typography = remember { platformNotesTypography() }
    CompositionLocalProvider(
        LocalNotesColors provides colors,
        LocalNotesTypography provides typography,
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
}
