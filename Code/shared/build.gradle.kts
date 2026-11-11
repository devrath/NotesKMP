plugins {
    alias(libs.plugins.convention.cmp.application)
}

kotlin {
    android {
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.presentation)
            implementation(projects.core.designsystem)
            implementation(projects.core.data)
            implementation(projects.feature.notes.presentation)
            implementation(projects.feature.note.presentation)
            implementation(projects.feature.search.presentation)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.bundles.koin.common)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}
