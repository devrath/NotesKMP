import androidx.room.gradle.RoomExtension
import com.istudio.noteskmp.convention.libs
import com.istudio.noteskmp.convention.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/** Room (KMP) with the bundled SQLite driver and KSP for every target. Apply after kmp.library. */
class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("androidx.room")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                "commonMainApi"(libs.library("androidx-room-runtime"))
                "commonMainApi"(libs.library("androidx-sqlite-bundled"))
                "kspAndroid"(libs.library("androidx-room-compiler"))
                "kspIosArm64"(libs.library("androidx-room-compiler"))
                "kspIosSimulatorArm64"(libs.library("androidx-room-compiler"))
            }
        }
    }
}
