plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.note.domain)
            implementation(libs.kotlinx.datetime)
        }
    }
}
