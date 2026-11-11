package com.istudio.noteskmp.core.presentation.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject
import platform.posix.memcpy

@Composable
actual fun rememberMarkdownFilePicker(
    maxBytes: Long,
    onResult: (PickedFile?) -> Unit,
): FilePickerLauncher {
    val scope = rememberCoroutineScope()
    val currentOnResult by rememberUpdatedState(onResult)
    // UIKit holds the delegate weakly, so it is kept here for as long as the picker can show.
    val delegate = remember {
        DocumentPickerDelegate { url ->
            if (url == null) {
                currentOnResult(null)
            } else {
                scope.launch {
                    currentOnResult(withContext(Dispatchers.IO) { readPickedFile(url, maxBytes) })
                }
            }
        }
    }
    return remember(delegate) {
        FilePickerLauncher {
            val types = listOfNotNull(
                UTType.typeWithFilenameExtension("md"),
                UTType.typeWithFilenameExtension("markdown"),
                UTTypePlainText,
            )
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = types, asCopy = true)
            picker.allowsMultipleSelection = false
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class DocumentPickerDelegate(
    private val onPicked: (NSURL?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onPicked(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readPickedFile(url: NSURL, maxBytes: Long): PickedFile {
    val name = url.lastPathComponent.orEmpty()
    return try {
        val path = url.path ?: return PickedFile.Unreadable(name)
        val attributes = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
        val size = (attributes?.get(NSFileSize) as? NSNumber)?.longLongValue ?: -1L
        if (size > maxBytes) return PickedFile.TooLarge(name)
        val data = NSData.dataWithContentsOfURL(url) ?: return PickedFile.Unreadable(name)
        if (data.length.toLong() > maxBytes) return PickedFile.TooLarge(name)
        PickedFile.Read(name, data.toByteArray())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        PickedFile.Unreadable(name)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}

@Composable
actual fun rememberMarkdownExporter(onResult: (ExportResult) -> Unit): MarkdownExporter {
    val currentOnResult by rememberUpdatedState(onResult)
    return remember {
        MarkdownExporter { fileName, content ->
            val url = writeTemporaryFile(fileName, content)
            val presenter = topViewController()
            if (url == null || presenter == null) {
                currentOnResult(ExportResult.Failed)
                return@MarkdownExporter
            }
            val sheet = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
            sheet.completionWithItemsHandler = { _, completed, _, error ->
                currentOnResult(
                    when {
                        error != null -> ExportResult.Failed
                        completed -> ExportResult.Shared
                        else -> ExportResult.Cancelled
                    },
                )
            }
            // iPad shows the sheet as a popover, which needs an anchor.
            sheet.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(sheet, animated = true, completion = null)
        }
    }
}

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
private fun writeTemporaryFile(fileName: String, content: String): NSURL? {
    val path = NSTemporaryDirectory() + fileName
    val written = NSString.create(string = content)
        .writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    return if (written) NSURL.fileURLWithPath(path) else null
}

@Suppress("DEPRECATION")
private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
