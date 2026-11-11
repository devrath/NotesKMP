plugins {
    alias(libs.plugins.convention.kmp.library)
}

// Test doubles shared by every module's unit tests. Only ever a test dependency.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.domain)
        }
    }
}
