import com.istudio.noteskmp.convention.libs
import com.istudio.noteskmp.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Feature presentation module: Compose plus Koin, lifecycle, saved state and navigation. */
class CmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.istudio.noteskmp.convention.cmp.library")

            dependencies {
                "commonMainImplementation"(project(":core:domain"))
                "commonMainImplementation"(project(":core:presentation"))
                "commonMainImplementation"(project(":core:designsystem"))

                "commonMainImplementation"(platform(libs.library("koin-bom")))
                "commonMainImplementation"(libs.findBundle("koin-common").get())

                "commonMainImplementation"(libs.library("androidx-lifecycle-viewmodelCompose"))
                "commonMainImplementation"(libs.library("androidx-lifecycle-runtimeCompose"))
                "commonMainImplementation"(libs.library("androidx-lifecycle-viewmodelSavedstate"))
                "commonMainImplementation"(libs.library("androidx-navigation-compose"))
            }
        }
    }
}
