import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
	alias(libs.plugins.kotlinSerialization)
}

kotlin {
	androidTarget {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
		}
	}

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(libs.kotlinx.coroutines.core)
				implementation(libs.kotlinx.serialization.json)
				implementation(libs.kotlinx.datetime)
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
				implementation(libs.retrofit.core)
				implementation(libs.retrofit.kotlinx.serialization.converter)
				implementation(libs.okhttp.core)
				implementation(libs.okhttp.logging.interceptor)
			}
		}
	}
}

android {
	namespace = "com.gu.recipe.core.networking"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
	}
}

