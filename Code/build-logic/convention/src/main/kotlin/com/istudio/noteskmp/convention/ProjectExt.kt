package com.istudio.noteskmp.convention

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.util.Locale

private const val ROOT_PACKAGE = "com.istudio.noteskmp"

val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

fun VersionCatalog.version(alias: String): String =
    findVersion(alias).get().requiredVersion

/** `:core:domain` becomes `com.istudio.noteskmp.core.domain`. */
fun Project.pathToPackageName(): String =
    ROOT_PACKAGE + path.replace(':', '.').replace("-", "").lowercase(Locale.ROOT)

/** `:shared` becomes `Shared`, `:core:domain` becomes `CoreDomain`. */
fun Project.pathToFrameworkName(): String =
    path.split(':', '-', '_')
        .filter { it.isNotEmpty() }
        .joinToString("") { part -> part.replaceFirstChar { it.titlecase(Locale.ROOT) } }
