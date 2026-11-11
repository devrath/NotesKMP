package com.istudio.noteskmp.core.domain

import kotlinx.coroutines.CoroutineScope

/**
 * A scope that lives as long as the app. Work that must finish after a screen closes, such
 * as the last autosave of a note, runs here instead of in `viewModelScope`.
 */
class ApplicationScope(scope: CoroutineScope) : CoroutineScope by scope
