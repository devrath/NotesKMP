rootProject.name = "NotesKmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
include(":core:domain")
include(":core:presentation")
include(":core:designsystem")
include(":core:database")
include(":core:data")
include(":core:testing")
include(":feature:notes:domain")
include(":feature:notes:presentation")
include(":feature:note:domain")
include(":feature:note:presentation")
include(":feature:search:domain")
include(":feature:search:presentation")
