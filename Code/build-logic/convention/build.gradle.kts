import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.istudio.noteskmp.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = libs.plugins.convention.kmp.library.get().pluginId
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = libs.plugins.convention.cmp.library.get().pluginId
            implementationClass = "CmpLibraryConventionPlugin"
        }
        register("cmpFeature") {
            id = libs.plugins.convention.cmp.feature.get().pluginId
            implementationClass = "CmpFeatureConventionPlugin"
        }
        register("cmpApplication") {
            id = libs.plugins.convention.cmp.application.get().pluginId
            implementationClass = "CmpApplicationConventionPlugin"
        }
        register("room") {
            id = libs.plugins.convention.room.get().pluginId
            implementationClass = "RoomConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = libs.plugins.convention.android.application.compose.get().pluginId
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
    }
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}
