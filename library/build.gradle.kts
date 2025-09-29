import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.metalava)
    kotlin("plugin.serialization") version "1.9.0"
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }


    val xcf = XCFramework("FeastSharedLib")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "FeastSharedLib"

            // Specify CFBundleIdentifier to uniquely identify the framework
            binaryOption("bundleId", "com.gu.feast.shared")
            xcf.add(this)
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
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
    namespace = "org.jetbrains.kotlinx.multiplatform.library.template"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
            groupId = "com.gu"
            artifactId = "feast-multiplatform-library"

            version = file("../version.txt").readText().trim()


            pom {
                name.set("Feast Multiplatform Library")
                description.set("A Kotlin Multiplatform library to handle recipe templates")
                url.set("https://github.com/guardian/feast-multiplatform-library")
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
                    connection.set("scm:git:git://github.com/guardian/feast-multiplatform-library.git")
                    developerConnection.set("scm:git:git://github.com/guardian/feast-multiplatform-library.git")
                    url.set("https://github.com/guardian/feast-multiplatform-library")
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