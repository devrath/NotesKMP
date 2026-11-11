package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.istudio.noteskmp.core.designsystem.theme.NotesColors
import com.istudio.noteskmp.core.designsystem.theme.NotesTheme

/** Search match style: marker background plus bold, so it never relies on color alone. */
fun NotesColors.highlightSpanStyle(): SpanStyle = SpanStyle(
    background = highlightBackground,
    color = highlightForeground,
    fontWeight = FontWeight.Bold,
)

/** [NotesColors.highlightSpanStyle] for the current theme. */
val searchHighlightStyle: SpanStyle
    @Composable
    @ReadOnlyComposable
    get() = NotesTheme.colors.highlightSpanStyle()

/**
 * Applies [style] to every range in [matches] (end inclusive). Ranges may arrive unsorted,
 * overlapping or partly outside [text]; they are clamped and merged so each character is
 * styled at most once. The text itself is never changed.
 */
fun highlightMatches(
    text: String,
    matches: List<IntRange>,
    style: SpanStyle,
): AnnotatedString = buildAnnotatedString {
    append(text)
    mergeRanges(text.length, matches).forEach { range ->
        addStyle(style, range.first, range.last + 1)
    }
}

internal fun mergeRanges(length: Int, ranges: List<IntRange>): List<IntRange> {
    if (length == 0) return emptyList()
    val clamped = ranges
        .mapNotNull { range ->
            val start = range.first.coerceAtLeast(0)
            val end = range.last.coerceAtMost(length - 1)
            if (start <= end) start..end else null
        }
        .sortedBy { it.first }
    val merged = mutableListOf<IntRange>()
    for (range in clamped) {
        val last = merged.lastOrNull()
        if (last != null && range.first <= last.last + 1) {
            merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
        } else {
            merged += range
        }
    }
    return merged
}
