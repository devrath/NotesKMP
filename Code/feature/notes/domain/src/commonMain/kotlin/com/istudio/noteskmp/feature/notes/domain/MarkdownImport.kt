package com.istudio.noteskmp.feature.notes.domain

import com.istudio.noteskmp.core.domain.Error
import com.istudio.noteskmp.core.domain.Result
import com.istudio.noteskmp.core.domain.note.NewNote

/** Files over 5 MB are rejected. */
const val IMPORT_MAX_BYTES: Long = 5L * 1024 * 1024

enum class ImportError : Error {
    TOO_LARGE,
    NOT_TEXT,
    UNREADABLE,
}

private val Utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

/**
 * Turns an imported file into a note: the title is the file name without its extension,
 * the body is the file decoded as UTF-8 with a byte-order mark removed and `\r\n` / `\r`
 * line endings turned into `\n`. Nothing else about the text changes.
 */
fun decodeImportedFile(fileName: String, bytes: ByteArray): Result<NewNote, ImportError> {
    if (bytes.size > IMPORT_MAX_BYTES) return Result.Error(ImportError.TOO_LARGE)
    val start = if (bytes.startsWith(Utf8Bom)) Utf8Bom.size else 0
    val text = try {
        bytes.decodeToString(startIndex = start, endIndex = bytes.size, throwOnInvalidSequence = true)
    } catch (e: CharacterCodingException) {
        return Result.Error(ImportError.NOT_TEXT)
    }
    // Valid UTF-8 can still be binary; text files never contain NUL.
    if ('\u0000' in text) return Result.Error(ImportError.NOT_TEXT)
    return Result.Success(NewNote(title = importTitle(fileName), body = normalizeLineEndings(text)))
}

/** "Ideas.md" becomes "Ideas", "notes.v2.txt" becomes "notes.v2", ".md" becomes "". */
fun importTitle(fileName: String): String {
    val name = fileName.substringAfterLast('/')
    val dot = name.lastIndexOf('.')
    return if (dot >= 0) name.substring(0, dot) else name
}

fun normalizeLineEndings(text: String): String =
    if ('\r' !in text) text else text.replace("\r\n", "\n").replace('\r', '\n')

private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
    size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
