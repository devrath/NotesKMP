package com.istudio.noteskmp.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/** WCAG 2.2 AA: 4.5:1 for text, 3:1 for non-text UI such as input borders. */
class NotesColorsContrastTest {

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
    }

    private fun NotesColors.textPairs() = mapOf(
        "onBackground on background" to (onBackground to background),
        "onBackground on surface" to (onBackground to surface),
        "onBackground on surface2" to (onBackground to surface2),
        "onBackground on surface3" to (onBackground to surface3),
        "onMuted on background" to (onMuted to background),
        "onMuted on surface" to (onMuted to surface),
        "onMuted on surface2" to (onMuted to surface2),
        "markdownMarker on background" to (markdownMarker to background),
        "markdownMarker on surface" to (markdownMarker to surface),
        "markdownMarker on surface2" to (markdownMarker to surface2),
        "primary on background" to (primary to background),
        "primary on surface" to (primary to surface),
        "onPrimary on primary" to (onPrimary to primary),
        "onPrimarySoft on primarySoft" to (onPrimarySoft to primarySoft),
        "onAccent on accent" to (onAccent to accent),
        "highlightForeground on highlightBackground" to (highlightForeground to highlightBackground),
        "danger on surface" to (danger to surface),
        "onDanger on danger" to (onDanger to danger),
        "codeForeground on codeBackground" to (codeForeground to codeBackground),
        "checkDone on surface" to (checkDone to surface),
        "checkDone on background" to (checkDone to background),
        "snackbarForeground on snackbarBackground" to (snackbarForeground to snackbarBackground),
        "snackbarAction on snackbarBackground" to (snackbarAction to snackbarBackground),
    )

    private fun NotesColors.nonTextPairs() = mapOf(
        "outlineStrong on surface" to (outlineStrong to surface),
        "outlineStrong on background" to (outlineStrong to background),
        "focus on surface" to (focus to surface),
        "focus on background" to (focus to background),
    )

    private fun assertContrast(theme: String, pairs: Map<String, Pair<Color, Color>>, minimum: Float) {
        val failures = pairs.mapNotNull { (name, colors) ->
            val ratio = contrast(colors.first, colors.second)
            if (ratio < minimum) "$theme $name: $ratio" else null
        }
        assertTrue(failures.isEmpty(), "Below $minimum:1 -> $failures")
    }

    @Test
    fun `light text colors meet AA`() = assertContrast("light", LightNotesColors.textPairs(), 4.5f)

    @Test
    fun `dark text colors meet AA`() = assertContrast("dark", DarkNotesColors.textPairs(), 4.5f)

    @Test
    fun `light non-text colors meet AA`() = assertContrast("light", LightNotesColors.nonTextPairs(), 3f)

    @Test
    fun `dark non-text colors meet AA`() = assertContrast("dark", DarkNotesColors.nonTextPairs(), 3f)
}
