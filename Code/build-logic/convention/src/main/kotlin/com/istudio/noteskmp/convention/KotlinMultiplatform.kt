package com.istudio.noteskmp.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Android and iOS targets shared by every KMP module. There is no desktop or web target.
 */
internal fun Project.configureKotlinMultiplatform() {
    extensions.configure<KotlinMultiplatformExtension> {
        configureAndroidLibraryTarget {
            namespace = pathToPackageName()
            compileSdk = libs.version("android-compileSdk").toInt()
            minSdk = libs.version("android-minSdk").toInt()
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
            // Host (JVM) tests: commonTest + androidHostTest -> testAndroidHostTest
            withHostTest {
                isIncludeAndroidResources = true
            }
        }

        iosArm64()
        iosSimulatorArm64()

        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}

internal fun Project.configureAndroidLibraryTarget(
    block: KotlinMultiplatformAndroidLibraryTarget.() -> Unit,
) {
    extensions.configure<KotlinMultiplatformExtension> {
        extensions.configure<KotlinMultiplatformAndroidLibraryTarget>(block)
    }
}
