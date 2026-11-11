package com.istudio.noteskmp

import androidx.compose.ui.window.ComposeUIViewController
import com.istudio.noteskmp.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) {
    App()
}
