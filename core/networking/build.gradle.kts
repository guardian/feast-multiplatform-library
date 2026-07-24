import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.io.FileInputStream
import java.security.MessageDigest

object NetworkingConfig {
	const val SPM_FRAMEWORK_NAME = "FeastMultiplatformNetworking"
	const val BUNDLE_ID = "com.gu.recipe.kmp.networking"
}

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
	alias(libs.plugins.metalava)
	alias(libs.plugins.kotlinSerialization)
}

kotlin {
	androidTarget {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
		}
	}

	val xcf = XCFramework(NetworkingConfig.SPM_FRAMEWORK_NAME)

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64(),
	).forEach { target ->
		target.binaries.framework {
			baseName = NetworkingConfig.SPM_FRAMEWORK_NAME

			// Specify CFBundleIdentifier to uniquely identify the framework
			binaryOption("bundleId", NetworkingConfig.BUNDLE_ID)
			xcf.add(this)
			isStatic = true
		}
	}

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlinx.coroutines.core)
				implementation(libs.kotlinx.serialization.json)
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.kotlin.test)
				implementation(libs.kotlinx.coroutines.test)
			}
		}
		val androidMain by getting {
			dependencies {
				implementation(libs.okhttp.core)
				implementation(libs.okhttp.logging.interceptor)
			}
		}
	}
}

android {
	namespace = NetworkingConfig.BUNDLE_ID
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
	}
}

tasks.withType<Test> {
	reports {
		junitXml.required.set(true)
	}
}