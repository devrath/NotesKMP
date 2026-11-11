package com.istudio.noteskmp.core.presentation.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val IMPORT_MIME_TYPES = arrayOf("text/markdown", "text/x-markdown", "text/plain")
private const val EXPORT_MIME_TYPE = "text/markdown"

@Composable
actual fun rememberMarkdownFilePicker(
    maxBytes: Long,
    onResult: (PickedFile?) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            currentOnResult(null)
        } else {
            scope.launch {
                currentOnResult(withContext(Dispatchers.IO) { readPickedFile(context, uri, maxBytes) })
            }
        }
    }
    return remember(launcher) { FilePickerLauncher { launcher.launch(IMPORT_MIME_TYPES) } }
}

private fun readPickedFile(context: Context, uri: Uri, maxBytes: Long): PickedFile {
    val resolver = context.contentResolver
    var name = uri.lastPathSegment.orEmpty()
    return try {
        var size = -1L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)?.let { name = it }
                    if (!cursor.isNull(1)) size = cursor.getLong(1)
                }
            }
        if (size > maxBytes) return PickedFile.TooLarge(name)
        val stream = resolver.openInputStream(uri) ?: return PickedFile.Unreadable(name)
        // Read one byte past the limit, so a provider that reported no size is still caught.
        val bytes = stream.use { it.readNBytesCompat(maxBytes + 1) }
        if (bytes.size > maxBytes) PickedFile.TooLarge(name) else PickedFile.Read(name, bytes)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        PickedFile.Unreadable(name)
    }
}

private fun java.io.InputStream.readNBytesCompat(limit: Long): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(64 * 1024)
    var total = 0L
    while (total < limit) {
        val read = read(buffer, 0, minOf(buffer.size.toLong(), limit - total).toInt())
        if (read < 0) break
        out.write(buffer, 0, read)
        total += read
    }
    return out.toByteArray()
}

/**
 * Content waiting for the user to pick where to save it. It outlives a configuration change
 * while the document creator is open; a full note can be too large for saved state.
 */
private object PendingExport {
    var content: String? = null
}

@Composable
actual fun rememberMarkdownExporter(onResult: (ExportResult) -> Unit): MarkdownExporter {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(EXPORT_MIME_TYPE),
    ) { uri ->
        val content = PendingExport.content
        PendingExport.content = null
        when {
            uri == null -> currentOnResult(ExportResult.Cancelled)
            content == null -> currentOnResult(ExportResult.Failed)
            else -> scope.launch {
                currentOnResult(withContext(Dispatchers.IO) { writeExport(context, uri, content) })
            }
        }
    }
    return remember(launcher) {
        MarkdownExporter { fileName, content ->
            PendingExport.content = content
            launcher.launch(fileName)
        }
    }
}

private fun writeExport(context: Context, uri: Uri, content: String): ExportResult = try {
    val stream = context.contentResolver.openOutputStream(uri, "wt")
    if (stream == null) {
        ExportResult.Failed
    } else {
        stream.use { it.write(content.encodeToByteArray()) }
        ExportResult.Saved
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    ExportResult.Failed
}
