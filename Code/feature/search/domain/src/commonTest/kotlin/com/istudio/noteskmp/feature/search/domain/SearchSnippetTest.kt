package com.istudio.noteskmp.feature.search.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.istudio.noteskmp.core.domain.search.searchTerms
import kotlin.test.Test

class SearchSnippetTest {

    @Test
    fun `short body is shown whole with its matches`() {
        val snippet = buildSnippet("We agreed the plan", searchTerms("plan"))

        assertThat(snippet.text).isEqualTo("We agreed the plan")
        assertThat(snippet.matches).containsExactly(14..17)
    }

    @Test
    fun `snippet centres on the first body match with ellipses on word boundaries`() {
        val body = "word ".repeat(40) + "the plan here " + "tail ".repeat(60)
        val snippet = buildSnippet(body, searchTerms("plan"))

        assertThat(snippet.text.startsWith("…word")).isTrue()
        assertThat(snippet.text.endsWith("…")).isTrue()
        assertThat(snippet.text.substring(snippet.matches.first())).isEqualTo("plan")
        assertThat(snippet.text.contains("wor…")).isFalse()
    }

    @Test
    fun `title-only match shows the start of the body`() {
        val snippet = buildSnippet("Lemons, oat milk", searchTerms("groceries"))

        assertThat(snippet.text).isEqualTo("Lemons, oat milk")
        assertThat(snippet.matches).containsExactly()
    }

    @Test
    fun `line breaks read as spaces`() {
        assertThat(buildSnippet("a\nplan\nb", searchTerms("plan")).text).isEqualTo("a plan b")
    }

    @Test
    fun `empty body gives an empty snippet`() {
        assertThat(buildSnippet("", searchTerms("x")).text).isEqualTo("")
    }

    @Test
    fun `cut never splits an emoji`() {
        val body = "😀".repeat(200)
        val snippet = buildSnippet(body, emptyList(), length = 7)

        assertThat(snippet.text.removeSuffix("…").last().isHighSurrogate()).isFalse()
    }

    @Test
    fun `one huge word is still cut to length`() {
        val snippet = buildSnippet("x".repeat(1_000), searchTerms("x"))

        assertThat(snippet.text.length <= SNIPPET_LENGTH + 2).isTrue()
    }
}
