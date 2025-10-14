import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.io.FileInputStream
import java.security.MessageDigest

object Config {
    const val GROUP_ID = "com.gu"
    const val MAVEN_ARTIFACT_ID = "feast-multiplatform-library"
    const val SPM_FRAMEWORK_NAME = "FeastMultiplatformLibrary"
    const val BUNDLE_ID = "com.gu.feast.shared"
    const val GITHUB_REPO = "guardian/feast-multiplatform-library"
    const val PACKAGE_DESCRIPTION = "A Kotlin Multiplatform library to handle recipe templates"
}

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


    val xcf = XCFramework(Config.SPM_FRAMEWORK_NAME)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = Config.SPM_FRAMEWORK_NAME

            // Specify CFBundleIdentifier to uniquely identify the framework
            binaryOption("bundleId", Config.BUNDLE_ID)
            xcf.add(this)
            isStatic = true
        }
    }

    val versionFile = layout.projectDirectory.file("../version.txt")
    js(IR) {
        useCommonJs()
        binaries.executable()
        nodejs()
        generateTypeScriptDefinitions()

        compilations["main"].packageJson {
            customField("name", "@guardian/feast-multiplatform-library")
            customField("version", versionFile.asFile.readText().trim())
            customField("description", Config.PACKAGE_DESCRIPTION)
            customField("keywords", listOf("kotlin", "multiplatform"))
            customField("license", "Apache-2.0")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

android {
    namespace = Config.BUNDLE_ID
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
    publications {
        register<MavenPublication>("release") {
            groupId = Config.GROUP_ID
            artifactId = Config.MAVEN_ARTIFACT_ID

            version = file("../version.txt").readText().trim()


            pom {
                name.set("Feast Multiplatform Library")
                description.set(Config.PACKAGE_DESCRIPTION)
                url.set("https://github.com/${Config.GITHUB_REPO}")
                packaging = "aar"
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
                    connection.set("scm:git:git://github.com/${Config.GITHUB_REPO}.git")
                    developerConnection.set("scm:git:git://github.com/${Config.GITHUB_REPO}.git")
                    url.set("https://github.com/${Config.GITHUB_REPO}")
                }
            }

            // Use the artifacts called "release" for publishing.
            afterEvaluate {
                from(components["release"])
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
    dependsOn("assemble${Config.SPM_FRAMEWORK_NAME}XCFramework")

    val xcframeworkPath = layout.buildDirectory.dir("XCFrameworks/release/${Config.SPM_FRAMEWORK_NAME}.xcframework")
    from(xcframeworkPath)
    archiveFileName.set("${Config.SPM_FRAMEWORK_NAME}.xcframework.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    doLast {
        println("XCFramework zipped to: ${archiveFile.get().asFile.absolutePath}")
    }
}

tasks.register("publishXCFrameworkToGitHub") {
    dependsOn("zipXCFramework")

    // Use Provider APIs for configuration cache compatibility
    val versionFile = layout.projectDirectory.file("../version.txt")
    val zipFileProvider = layout.buildDirectory.file("distributions/${Config.SPM_FRAMEWORK_NAME}.xcframework.zip")
    val packageSwiftFile = layout.projectDirectory.file("../Package.swift")

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
            name: "${Config.SPM_FRAMEWORK_NAME}",
            platforms: [
                .iOS(.v${libs.versions.ios.get()}),
            ],
            products: [
                .library(name: "${Config.SPM_FRAMEWORK_NAME}", targets: ["${Config.SPM_FRAMEWORK_NAME}"])
            ],
            targets: [
                .binaryTarget(
                    name: "${Config.SPM_FRAMEWORK_NAME}",
                    url: "https://github.com/${Config.GITHUB_REPO}/releases/download/$version/${Config.SPM_FRAMEWORK_NAME}.xcframework.zip",
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