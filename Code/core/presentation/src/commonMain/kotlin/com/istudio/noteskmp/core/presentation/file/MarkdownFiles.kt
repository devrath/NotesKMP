package com.istudio.noteskmp.core.presentation.file

import androidx.compose.runtime.Composable

/** A file the user picked to import. Reading happens off the main thread. */
sealed interface PickedFile {
    val name: String

    /** The file's bytes, at most the size limit the picker was given. */
    class Read(override val name: String, val bytes: ByteArray) : PickedFile

    /** Larger than the size limit; not read. */
    data class TooLarge(override val name: String) : PickedFile

    data class Unreadable(override val name: String) : PickedFile
}

fun interface FilePickerLauncher {
    fun launch()
}

/**
 * The system file picker, filtered to Markdown and plain-text files, for exactly one file.
 * [onResult] gets null when the user cancels.
 */
@Composable
expect fun rememberMarkdownFilePicker(
    maxBytes: Long,
    onResult: (PickedFile?) -> Unit,
): FilePickerLauncher

sealed interface ExportResult {
    /** Written to a file the user chose. The app confirms it, since the platform doesn't. */
    data object Saved : ExportResult

    /** Handed to the share sheet, which confirms the outcome itself. */
    data object Shared : ExportResult

    data object Cancelled : ExportResult
    data object Failed : ExportResult
}

fun interface MarkdownExporter {
    /** Offers [content] as a UTF-8 file named [fileName] through the system save or share flow. */
    fun export(fileName: String, content: String)
}

/**
 * The platform save flow: the Storage Access Framework document creator on Android, the
 * share sheet (with Save to Files) on iOS.
 */
@Composable
expect fun rememberMarkdownExporter(onResult: (ExportResult) -> Unit): MarkdownExporter
