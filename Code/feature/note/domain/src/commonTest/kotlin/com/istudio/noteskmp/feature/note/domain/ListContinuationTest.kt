package com.istudio.noteskmp.feature.note.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ListContinuationTest {

    /** Simulates typing Enter at the cursor (the end of [text] unless given). */
    private fun enter(text: String, cursor: Int = text.length): EditorValue? {
        val before = EditorValue(text, cursor)
        val after = EditorValue(text.substring(0, cursor) + "\n" + text.substring(cursor), cursor + 1)
        return continueList(before, after)
    }

    @Test
    fun `bullet item continues with the same bullet`() {
        assertThat(enter("- Milk")).isEqualTo(EditorValue("- Milk\n- ", 9))
        assertThat(enter("* Milk")?.text).isEqualTo("* Milk\n* ")
    }

    @Test
    fun `checklist item continues unticked even after a ticked one`() {
        assertThat(enter("- [x] Done")?.text).isEqualTo("- [x] Done\n- [ ] ")
    }

    @Test
    fun `numbered item continues with the next number and delimiter`() {
        assertThat(enter("9. Ninth")?.text).isEqualTo("9. Ninth\n10. ")
        assertThat(enter("1) First")?.text).isEqualTo("1) First\n2) ")
    }

    @Test
    fun `largest allowed number does not overflow`() {
        assertThat(enter("999999999. Big")?.text).isEqualTo("999999999. Big\n1000000000. ")
    }

    @Test
    fun `quote line continues`() {
        assertThat(enter("> Writing first")?.text).isEqualTo("> Writing first\n> ")
        assertThat(enter(">> nested")?.text).isEqualTo(">> nested\n>> ")
    }

    @Test
    fun `nested item keeps its indent`() {
        assertThat(enter("  - Nested")?.text).isEqualTo("  - Nested\n  - ")
    }

    @Test
    fun `enter on an empty item removes the marker and ends the list`() {
        assertThat(enter("- Milk\n- ")).isEqualTo(EditorValue("- Milk\n", 7))
        assertThat(enter("1. One\n2. ")).isEqualTo(EditorValue("1. One\n", 7))
        assertThat(enter("- [ ] ")).isEqualTo(EditorValue("", 0))
    }

    @Test
    fun `enter in the middle of an item is a plain newline`() {
        assertThat(enter("- Milk", cursor = 4)).isNull()
    }

    @Test
    fun `plain text and lookalikes do not continue`() {
        assertThat(enter("Hello")).isNull()
        assertThat(enter("-dash")).isNull()
        assertThat(enter("**bold**")).isNull()
        assertThat(enter("2026. A year")?.text).isEqualTo("2026. A year\n2027. ")
    }

    @Test
    fun `lists inside code fences do not continue`() {
        assertThat(enter("```\n- not a list")).isNull()
        assertThat(enter("```\ncode\n```\n- list")?.text).isEqualTo("```\ncode\n```\n- list\n- ")
    }

    @Test
    fun `changes other than a single enter are left alone`() {
        val before = EditorValue("- Milk", 6)
        assertThat(continueList(before, EditorValue("- Milk!", 7))).isNull()
        assertThat(continueList(before, EditorValue("- Milk\n\n", 8))).isNull()
        // Enter that replaced a selection.
        assertThat(continueList(EditorValue("- Milk", 2, 6), EditorValue("- \n", 3))).isNull()
    }

    @Test
    fun `item with emoji and rtl text continues`() {
        assertThat(enter("- 👨‍👩‍👧 שלום")?.text).isEqualTo("- 👨‍👩‍👧 שלום\n- ")
    }

    @Test
    fun `windows line endings are not assumed`() {
        assertThat(enter("a\n- Milk")?.text).isEqualTo("a\n- Milk\n- ")
    }
}
