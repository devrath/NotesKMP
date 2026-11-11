package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color roles from `design/assets/tokens.css`. Components read these through
 * `NotesTheme.colors` or `MaterialTheme.colorScheme`, never as raw values.
 */
@Immutable
data class NotesColors(
    val background: Color,
    val surface: Color,
    /** Search field, toolbar, code. */
    val surface2: Color,
    /** Pressed, selected. */
    val surface3: Color,
    val onBackground: Color,
    /** Previews, timestamps. */
    val onMuted: Color,
    /** Muted Markdown syntax in the editor. */
    val markdownMarker: Color,
    /** Decorative dividers. */
    val outline: Color,
    /** Input borders, focus-adjacent. */
    val outlineStrong: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val onPrimarySoft: Color,
    /** Create button, brand mark. */
    val accent: Color,
    val onAccent: Color,
    /** Search match; always paired with bold. */
    val highlightBackground: Color,
    val highlightForeground: Color,
    val danger: Color,
    val onDanger: Color,
    val codeBackground: Color,
    val codeForeground: Color,
    val checkDone: Color,
    val snackbarBackground: Color,
    val snackbarForeground: Color,
    val snackbarAction: Color,
    val scrim: Color,
    val focus: Color,
)

private val Paper = Color(0xFFF7F8FA)
private val Ink = Color(0xFF171A2B)
private val Cobalt = Color(0xFF2F45B5)
private val Marker = Color(0xFFFFE14D)
private val Graphite = Color(0xFF5A5F73)

internal val LightNotesColors = NotesColors(
    background = Paper,
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEEF0F5),
    surface3 = Color(0xFFE3E6EE),
    onBackground = Ink,
    onMuted = Graphite,
    markdownMarker = Color(0xFF636982),
    outline = Color(0xFFDDE0E8),
    outlineStrong = Color(0xFF858AA0),
    primary = Cobalt,
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFFE4E8FB),
    onPrimarySoft = Color(0xFF1E2E80),
    accent = Marker,
    onAccent = Ink,
    highlightBackground = Marker,
    highlightForeground = Ink,
    danger = Color(0xFFB42318),
    onDanger = Color(0xFFFFFFFF),
    codeBackground = Color(0xFFEEF0F5),
    codeForeground = Color(0xFF2A2F48),
    checkDone = Color(0xFF6A7086),
    snackbarBackground = Color(0xFF252A40),
    snackbarForeground = Color(0xFFF1F2F7),
    snackbarAction = Marker,
    scrim = Color(0x5C10131F),
    focus = Cobalt,
)

internal val DarkNotesColors = NotesColors(
    background = Color(0xFF10131F),
    surface = Color(0xFF171B2A),
    surface2 = Color(0xFF20253A),
    surface3 = Color(0xFF2A3049),
    onBackground = Color(0xFFE7E9F2),
    onMuted = Color(0xFFA4A9BE),
    markdownMarker = Color(0xFF8F95AD),
    outline = Color(0xFF2A2F44),
    outlineStrong = Color(0xFF7A809A),
    primary = Color(0xFFA9B6FF),
    onPrimary = Color(0xFF0E1A5C),
    primarySoft = Color(0xFF26326E),
    onPrimarySoft = Color(0xFFDCE1FF),
    accent = Marker,
    onAccent = Ink,
    highlightBackground = Marker,
    highlightForeground = Ink,
    danger = Color(0xFFFFB4AB),
    onDanger = Color(0xFF5F0B05),
    codeBackground = Color(0xFF1C2134),
    codeForeground = Color(0xFFD6DAEA),
    checkDone = Color(0xFF8F95AD),
    snackbarBackground = Color(0xFFE7E9F2),
    snackbarForeground = Ink,
    snackbarAction = Cobalt,
    scrim = Color(0x8F000000),
    focus = Color(0xFFA9B6FF),
)

/** Maps the token roles onto Material 3 so stock components pick up the palette. */
internal fun NotesColors.toColorScheme(darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primarySoft,
        onPrimaryContainer = onPrimarySoft,
        inversePrimary = snackbarAction,
        secondary = primary,
        onSecondary = onPrimary,
        secondaryContainer = primarySoft,
        onSecondaryContainer = onPrimarySoft,
        tertiary = primary,
        onTertiary = onPrimary,
        tertiaryContainer = accent,
        onTertiaryContainer = onAccent,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onBackground,
        surfaceVariant = surface2,
        onSurfaceVariant = onMuted,
        surfaceTint = primary,
        inverseSurface = snackbarBackground,
        inverseOnSurface = snackbarForeground,
        error = danger,
        onError = onDanger,
        errorContainer = danger,
        onErrorContainer = onDanger,
        outline = outlineStrong,
        outlineVariant = outline,
        scrim = scrim,
        surfaceBright = surface,
        surfaceDim = background,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface2,
        surfaceContainerHigh = surface2,
        surfaceContainerHighest = surface3,
    )
}
