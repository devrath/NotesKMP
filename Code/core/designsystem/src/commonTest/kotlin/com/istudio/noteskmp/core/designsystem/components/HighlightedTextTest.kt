package com.istudio.noteskmp.core.designsystem.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.istudio.noteskmp.core.designsystem.theme.DarkNotesColors
import com.istudio.noteskmp.core.designsystem.theme.LightNotesColors
import kotlin.test.Test

class HighlightedTextTest {

    private val style = SpanStyle(fontWeight = FontWeight.Bold)

    private fun AnnotatedString.styledRanges(): List<IntRange> =
        spanStyles.map { it.start until it.end }

    @Test
    fun `highlights each match with the given style`() {
        val result = highlightMatches("Weekly plan, plan B", listOf(7..10, 13..16), style)

        assertThat(result.styledRanges()).containsExactly(7..10, 13..16)
        assertThat(result.spanStyles.map { it.item }).containsExactly(style, style)
    }

    @Test
    fun `never changes the text`() {
        val text = "Café **plan** 🗓️ naïve\r\n"

        val result = highlightMatches(text, listOf(0..3, 7..10, 100..200), style)

        assertThat(result.text).isEqualTo(text)
    }

    @Test
    fun `no matches gives plain text`() {
        assertThat(highlightMatches("plan", emptyList(), style).spanStyles).isEmpty()
    }

    @Test
    fun `empty text ignores every range`() {
        assertThat(highlightMatches("", listOf(0..3), style).spanStyles).isEmpty()
    }

    @Test
    fun `overlapping and contained ranges merge into one span`() {
        assertThat(mergeRanges(20, listOf(2..6, 4..9, 5..5))).containsExactly(2..9)
    }

    @Test
    fun `adjacent ranges merge so no gap shows between them`() {
        assertThat(mergeRanges(20, listOf(0..3, 4..7))).containsExactly(0..7)
    }

    @Test
    fun `ranges separated by a character stay apart`() {
        assertThat(mergeRanges(20, listOf(0..3, 5..7))).containsExactly(0..3, 5..7)
    }

    @Test
    fun `unsorted ranges come out sorted`() {
        assertThat(mergeRanges(20, listOf(12..14, 0..1, 6..8))).containsExactly(0..1, 6..8, 12..14)
    }

    @Test
    fun `ranges are clamped to the text`() {
        assertThat(mergeRanges(5, listOf(-3..1, 3..99))).containsExactly(0..1, 3..4)
    }

    @Test
    fun `ranges fully outside the text or inverted are dropped`() {
        assertThat(mergeRanges(5, listOf(5..9, -4..-1, 4..2, IntRange.EMPTY))).isEmpty()
    }

    @Test
    fun `range covering a whole emoji keeps both surrogate halves`() {
        val text = "a🗓b"

        val result = highlightMatches(text, listOf(1..2), style)

        assertThat(result.text.substring(result.spanStyles.single().start, result.spanStyles.single().end))
            .isEqualTo("🗓")
    }

    @Test
    fun `search highlight is background plus bold in both themes`() {
        listOf(LightNotesColors, DarkNotesColors).forEach { colors ->
            val highlight = colors.highlightSpanStyle()
            assertThat(highlight.background).isEqualTo(colors.highlightBackground)
            assertThat(highlight.color).isEqualTo(colors.highlightForeground)
            assertThat(highlight.fontWeight).isEqualTo(FontWeight.Bold)
        }
    }
}
