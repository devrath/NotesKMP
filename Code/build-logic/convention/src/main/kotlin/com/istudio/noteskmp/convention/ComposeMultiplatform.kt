package com.istudio.noteskmp.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension

/**
 * Compose Multiplatform on top of [configureKotlinMultiplatform]: Compose dependencies,
 * Android resources (needed by Compose resources) and a `Res` class in the module's package.
 */
internal fun Project.configureComposeMultiplatform() {
    with(pluginManager) {
        apply("org.jetbrains.kotlin.plugin.compose")
        apply("org.jetbrains.compose")
    }

    configureAndroidLibraryTarget {
        androidResources {
            enable = true
        }
    }

    extensions.configure<ComposeExtension> {
        extensions.configure<ResourcesExtension> {
            packageOfResClass = "${pathToPackageName()}.generated.resources"
        }
    }

    dependencies {
        "commonMainImplementation"(libs.library("compose-runtime"))
        "commonMainImplementation"(libs.library("compose-foundation"))
        "commonMainImplementation"(libs.library("compose-material3"))
        "commonMainImplementation"(libs.library("compose-ui"))
        "commonMainImplementation"(libs.library("compose-components-resources"))
        "commonMainImplementation"(libs.library("compose-uiToolingPreview"))

        // Android previews: KMP modules have no build types, so debugImplementation
        // is unavailable. androidMainImplementation is the only way to get the tooling.
        "androidMainImplementation"(libs.library("compose-uiTooling"))
    }
}
