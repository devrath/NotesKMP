package com.istudio.noteskmp.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/** A user-facing string that is either a Compose resource or a dynamic value. */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class Resource(val id: StringResource, val args: Array<Any> = emptyArray()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is Resource -> stringResource(id, *args)
    }

    suspend fun asStringAsync(): String = when (this) {
        is DynamicString -> value
        is Resource -> getString(id, *args)
    }
}
