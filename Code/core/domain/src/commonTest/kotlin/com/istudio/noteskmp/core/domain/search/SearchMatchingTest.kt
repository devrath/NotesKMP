package com.istudio.noteskmp.core.domain.search

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class SearchMatchingTest {

    @Test
    fun `terms are folded - split on punctuation and deduplicated`() {
        assertThat(searchTerms("  Café **PLAN**, plan; e-mail ")).containsExactly("cafe", "plan", "e", "mail")
    }

    @Test
    fun `query of only punctuation has no terms`() {
        assertThat(searchTerms("** -- ##")).isEmpty()
        assertThat(searchTerms("")).isEmpty()
    }

    @Test
    fun `fts operators cannot sneak in`() {
        assertThat(searchTerms("cats OR dogs NEAR \"x\"*")).containsExactly("cats", "or", "dogs", "near", "x")
    }

    @Test
    fun `diacritic insensitive both ways`() {
        assertThat(matchesAll("Café latte", searchTerms("cafe"))).isTrue()
        assertThat(matchesAll("Cafe latte", searchTerms("café"))).isTrue()
        assertThat(matchesAll("Łódź trip", searchTerms("lodz"))).isTrue()
    }

    @Test
    fun `combining accents match their precomposed form`() {
        val decomposed = "Café latte"

        assertThat(matchesAll(decomposed, searchTerms("café"))).isTrue()
        // The highlight covers the accent too.
        assertThat(findMatches(decomposed, searchTerms("cafe"))).containsExactly(0..4)
    }

    @Test
    fun `words match by prefix only`() {
        assertThat(matchesAll("Meeting notes", searchTerms("meet"))).isTrue()
        assertThat(matchesAll("Meeting notes", searchTerms("eting"))).isFalse()
    }

    @Test
    fun `every term must match - in any order`() {
        assertThat(matchesAll("export plan per note", searchTerms("note plan"))).isTrue()
        assertThat(matchesAll("export plan per note", searchTerms("plan budget"))).isFalse()
        assertThat(matchesAny("export plan per note", searchTerms("plan budget"))).isTrue()
    }

    @Test
    fun `matches cover the matched prefix of every word`() {
        val text = "Plan the plans, explanation"

        assertThat(findMatches(text, searchTerms("plan"))).containsExactly(0..3, 9..12)
    }

    @Test
    fun `longest matching term wins for a word`() {
        assertThat(findMatches("meeting", searchTerms("me meet"))).containsExactly(0..3)
    }

    @Test
    fun `markdown markers around a word do not stop a match`() {
        assertThat(findMatches("a **plan** b", searchTerms("plan"))).containsExactly(4..7)
    }

    @Test
    fun `no terms means no matches`() {
        assertThat(findMatches("anything", emptyList())).isEmpty()
        assertThat(matchesAll("anything", emptyList())).isFalse()
    }

    @Test
    fun `fold table maps one char to one char`() {
        assertThat(foldChar('É')).isEqualTo('e')
        assertThat(foldChar('ß')).isEqualTo('ß')
        assertThat(foldChar('Ø')).isEqualTo('o')
    }
}
