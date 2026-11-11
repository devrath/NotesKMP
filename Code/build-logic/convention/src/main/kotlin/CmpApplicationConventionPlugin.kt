import com.istudio.noteskmp.convention.configureComposeMultiplatform
import com.istudio.noteskmp.convention.pathToFrameworkName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

/**
 * The shared umbrella module (`:shared`, called `:composeApp` in the skills). It is a KMP
 * library that wires every module together and builds the static iOS framework.
 */
class CmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.istudio.noteskmp.convention.kmp.library")
            configureComposeMultiplatform()

            val frameworkName = pathToFrameworkName()
            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType<KotlinNativeTarget>().configureEach {
                    binaries.framework {
                        baseName = frameworkName
                        isStatic = true
                    }
                }
            }
        }
    }
}
