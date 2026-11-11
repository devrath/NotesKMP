package com.istudio.noteskmp.core.domain.search

/**
 * Search words in [query]: runs of letters and digits (combining marks stay with their
 * letter), case- and diacritic-folded, without duplicates. Punctuation and Markdown markers
 * separate words, the same way the full-text index tokenizes notes.
 */
fun searchTerms(query: String): List<String> =
    tokens(query).map { it.folded }.filter { it.isNotEmpty() }.distinct().toList()

/**
 * Every place in [text] where a word starts with one of [terms] (as returned by
 * [searchTerms]). Each range covers the matched prefix in [text], end inclusive.
 */
fun findMatches(text: String, terms: List<String>): List<IntRange> {
    if (terms.isEmpty()) return emptyList()
    val matches = mutableListOf<IntRange>()
    for (token in tokens(text)) {
        val term = terms.filter { token.folded.startsWith(it) }.maxByOrNull { it.length } ?: continue
        matches += token.start..token.originalEnd(term.length)
    }
    return matches
}

/** Whether every term matches some word of [text] by prefix. */
fun matchesAll(text: String, terms: List<String>): Boolean {
    if (terms.isEmpty()) return false
    val words = tokens(text).map { it.folded }.toList()
    return terms.all { term -> words.any { it.startsWith(term) } }
}

/** Whether any term matches some word of [text] by prefix. */
fun matchesAny(text: String, terms: List<String>): Boolean =
    terms.isNotEmpty() && tokens(text).any { token -> terms.any { token.folded.startsWith(it) } }

/** A word in the original text and its folded form, with a map back to original indices. */
private class Token(
    val start: Int,
    val folded: String,
    /** Original index of each folded char. */
    private val sourceIndex: IntArray,
    /** Original index of the token's last char, including trailing combining marks. */
    private val end: Int,
) {
    fun originalEnd(foldedLength: Int): Int {
        if (foldedLength >= folded.length) return end
        // Extend over combining marks that follow the last matched char.
        return sourceIndex[foldedLength] - 1
    }
}

private fun tokens(text: String): Sequence<Token> = sequence {
    var i = 0
    while (i < text.length) {
        if (!text[i].isWordStart()) {
            i++
            continue
        }
        val start = i
        val folded = StringBuilder()
        val indices = ArrayList<Int>()
        while (i < text.length && text[i].isWordPart()) {
            val c = text[i]
            if (!c.isCombiningMark()) {
                folded.append(foldChar(c))
                indices += i
            }
            i++
        }
        yield(Token(start, folded.toString(), indices.toIntArray(), i - 1))
    }
}

private fun Char.isWordStart(): Boolean = isLetterOrDigit() || isSurrogate()
private fun Char.isWordPart(): Boolean = isLetterOrDigit() || isCombiningMark() || isSurrogate()

private fun Char.isCombiningMark(): Boolean =
    this in '̀'..'ͯ' || this in '᪰'..'᫿' || this in '᷀'..'᷿' ||
        this in '⃐'..'⃿' || this in '︠'..'︯'

/** Lowercase and drop diacritics, one char for one char, so match ranges map back exactly. */
internal fun foldChar(c: Char): Char {
    val lower = c.lowercaseChar()
    val index = DIACRITIC_SOURCE.indexOf(lower)
    return if (index >= 0) DIACRITIC_TARGET[index] else lower
}

// Lowercase Latin letters with diacritics (Latin-1 Supplement through Latin Extended-B) and
// their base letter, generated from the Unicode decompositions plus the stroked letters.
private const val DIACRITIC_SOURCE =
    "àáâãäåçèéêëìíîïñòóôõöøùúûüýÿāăąćĉċčďđēĕėęěĝğġģĥħĩīĭįıĵķĺļľŀłńņňŉōŏőŕŗřśŝşšţťŧũūŭůűųŵŷźżžơưǎǐǒǔǖǘǚǜǟǡǧǩǫǭǰǵǹǻȁȃȅȇȉȋȍȏȑȓȕȗșțȟȧȩȫȭȯȱȳșț"
private const val DIACRITIC_TARGET =
    "aaaaaaceeeeiiiinoooooouuuuyyaaaccccddeeeeegggghhiiiiijklllllnnnnooorrrsssstttuuuuuuwyzzzouaiouuuuuaagkoojgnaaaeeiioorruusthaeooooyst"
