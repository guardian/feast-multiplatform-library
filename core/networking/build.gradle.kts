import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.io.FileInputStream
import java.security.MessageDigest

object NetworkingConfig {
	const val GROUP_ID = "com.gu"
	const val MAVEN_ARTIFACT_ID = "feast-multiplatform-networking"
	const val SPM_FRAMEWORK_NAME = "FeastMultiplatformNetworking"
	const val BUNDLE_ID = "com.gu.recipe.kmp.networking"
	const val GITHUB_REPO = "guardian/feast-multiplatform-library"
	const val PACKAGE_DESCRIPTION = "A Kotlin Multiplatform library to hold nwtworking urls"
}

group = NetworkingConfig.GROUP_ID
version = file("../../version.txt").readText().trim()

plugins {
	`maven-publish`
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
	alias(libs.plugins.metalava)
	alias(libs.plugins.kotlinSerialization)
}

kotlin {
	androidTarget {
			publishLibraryVariants("release")
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

	// Create a single variant for publishing called "release". Add separate jars for javadoc
	// and sources.
	publishing {
		singleVariant("release") {
			withSourcesJar()
			withJavadocJar()
		}
	}
}

publishing {
	publications.withType<MavenPublication>().configureEach {
		groupId = NetworkingConfig.GROUP_ID
		version = project.version.toString()
		artifactId = when (name) {
			"kotlinMultiplatform" -> NetworkingConfig.MAVEN_ARTIFACT_ID
			"release" -> "${NetworkingConfig.MAVEN_ARTIFACT_ID}-android"
			else -> "${NetworkingConfig.MAVEN_ARTIFACT_ID}-$name"
		}

		pom {
			name.set("Feast Multiplatform Library")
			description.set(NetworkingConfig.PACKAGE_DESCRIPTION)
			url.set("https://github.com/${NetworkingConfig.GITHUB_REPO}")

			licenses {
				license {
					name.set("Apache License, Version 2.0")
					url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
				}
			}

			developers {
				developer {
					id.set("guardian/android-developers")
					name.set("The Guardian")
					email.set("contact@guardian.co.uk")
					url.set("https://github.com/guardian")
				}
			}

			organization {
				name.set("Guardian News & Media")
				url.set("https://www.theguardian.com")
			}
			scm {
				connection.set("scm:git:git://github.com/${NetworkingConfig.GITHUB_REPO}.git")
				developerConnection.set("scm:git:git://github.com/${NetworkingConfig.GITHUB_REPO}.git")
				url.set("https://github.com/${NetworkingConfig.GITHUB_REPO}")
			}
		}
	}
	repositories {
		// Adds a task for publishing locally to the build directory.
		// Use as `./gradlew :library:publishReleasePublicationToCustomRepository`
		// Use with -Prepo.local=$LOCAL_ARTIFACTS_STAGING_PATH to output to a custom path.
		maven {
			name = "custom"
			url = uri(
				(project.findProperty("repo.local") as? String)
					?: "${project.layout.buildDirectory.asFile.get().path}/custom"
			)
		}
	}
}

// iOS XCFramework publishing tasks
tasks.register("zipXCFramework", Zip::class) {
	dependsOn("assemble${NetworkingConfig.SPM_FRAMEWORK_NAME}XCFramework")

	val xcframeworkPath = layout.buildDirectory.dir("XCFrameworks/release/${NetworkingConfig.SPM_FRAMEWORK_NAME}.xcframework")
	from(xcframeworkPath) {
		into("${NetworkingConfig.SPM_FRAMEWORK_NAME}.xcframework")
	}
	archiveFileName.set("${NetworkingConfig.SPM_FRAMEWORK_NAME}.xcframework.zip")
	destinationDirectory.set(layout.buildDirectory.dir("distributions"))

	doLast {
		println("XCFramework zipped to: ${archiveFile.get().asFile.absolutePath}")
	}
}

tasks.register("publishXCFrameworkToGitHub") {
	dependsOn("zipXCFramework")

	// Use Provider APIs for configuration cache compatibility
	val versionFile = layout.projectDirectory.file("../version.txt")
	val zipFileProvider = layout.buildDirectory.file("distributions/${NetworkingConfig.SPM_FRAMEWORK_NAME}.xcframework.zip")
	val packageSwiftFile = layout.projectDirectory.file("../Package.swift")
	val iosVersion = libs.versions.ios.get()

	doLast {
		val version = versionFile.asFile.readText().trim()
		val zipFile = zipFileProvider.get().asFile

		println("Ready to publish XCFramework version $version")
		println("Zip file location: ${zipFile.absolutePath}")
		println("File size: ${zipFile.length()} bytes")

		// Calculate checksum
		val checksum = zipFile.inputStream().use { input: FileInputStream ->
			MessageDigest.getInstance("SHA-256").digest(input.readBytes())
				.joinToString("") { "%02x".format(it) }
		}

		println("SHA-256 checksum: $checksum")

		// Generate Package.swift file
		val packageSwiftContent = """
        // swift-tools-version:5.9
        // This file is automatically generated by the Gradle build.
        // Do not edit manually - see library/build.gradle.kts for the generation logic.
        import PackageDescription
        
        let package = Package(
            name: "${NetworkingConfig.SPM_FRAMEWORK_NAME}",
            platforms: [
                .iOS(.v${iosVersion}),
            ],
            products: [
                .library(name: "${NetworkingConfig.SPM_FRAMEWORK_NAME}", targets: ["${NetworkingConfig.SPM_FRAMEWORK_NAME}"])
            ],
            targets: [
                .binaryTarget(
                    name: "${NetworkingConfig.SPM_FRAMEWORK_NAME}",
                    url: "https://github.com/${NetworkingConfig.GITHUB_REPO}/releases/download/$version/${NetworkingConfig.SPM_FRAMEWORK_NAME}.xcframework.zip",
                    checksum:"$checksum")
            ]
        )
        """.trimIndent()

		packageSwiftFile.asFile.writeText(packageSwiftContent)

		println("Generated Package.swift with version $version and checksum $checksum")
	}
}

tasks.withType<Test> {
	reports {
		junitXml.required.set(true)
	}
}