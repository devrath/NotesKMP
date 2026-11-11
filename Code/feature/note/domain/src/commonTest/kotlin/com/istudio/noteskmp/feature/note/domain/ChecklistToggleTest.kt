package com.istudio.noteskmp.feature.note.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ChecklistToggleTest {

    @Test
    fun `ticks and unticks - changing only the box`() {
        assertThat(toggleChecklistItem("- [ ] Buy milk", 2)).isEqualTo("- [x] Buy milk")
        assertThat(toggleChecklistItem("- [x] Buy milk", 2)).isEqualTo("- [ ] Buy milk")
        assertThat(toggleChecklistItem("- [X] Buy milk", 2)).isEqualTo("- [ ] Buy milk")
    }

    @Test
    fun `only the box at the offset changes`() {
        val body = "- [ ] a\n- [ ] b"

        assertThat(toggleChecklistItem(body, 10)).isEqualTo("- [ ] a\n- [x] b")
    }

    @Test
    fun `offset not on a box changes nothing`() {
        assertThat(toggleChecklistItem("- [ ] a", 0)).isNull()
        assertThat(toggleChecklistItem("- [?] a", 2)).isNull()
        assertThat(toggleChecklistItem("[ ", 0)).isNull()
        assertThat(toggleChecklistItem("abc", -1)).isNull()
        assertThat(toggleChecklistItem("abc", 99)).isNull()
    }

    @Test
    fun `round trip from the parser offset`() {
        val body = "Intro\n\n> - [ ] quoted task"
        val offset = (parseMarkdown(body).last() as MarkdownBlock.ListItem).marker.let { (it as ListMarker.Task).checkboxOffset }

        assertThat(toggleChecklistItem(body, offset)).isEqualTo("Intro\n\n> - [x] quoted task")
    }
}
