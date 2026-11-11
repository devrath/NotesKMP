package com.istudio.noteskmp.core.domain

import kotlinx.coroutines.CoroutineDispatcher

/** Dispatchers for CPU-heavy work such as parsing or decoding, injected so tests can control them. */
class AppDispatchers(
    val default: CoroutineDispatcher,
)
