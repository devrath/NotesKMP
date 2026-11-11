import com.istudio.noteskmp.convention.configureComposeMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project

/** KMP library with Compose Multiplatform (core:presentation, core:designsystem). */
class CmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.istudio.noteskmp.convention.kmp.library")
            configureComposeMultiplatform()
        }
    }
}
