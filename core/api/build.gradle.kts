import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object APIConfig {
    const val GROUP_ID = "com.gu"
    const val MAVEN_ARTIFACT_ID = "feast-multiplatform-library.core.api"
    const val SPM_FRAMEWORK_NAME = "FeastMultiplatformLibraryCoreAPI"
    const val BUNDLE_ID = "com.gu.recipe.core.api"
    const val GITHUB_REPO = "guardian/feast-multiplatform-library"
    const val PACKAGE_DESCRIPTION = "A Kotlin Multiplatform library to call graphql APIs"
}

group = APIConfig.GROUP_ID
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

    /* js(IR) {
         nodejs()
     } */

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.all {
            linkerOpts("-lsqlite3")
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core:networking"))
                api(libs.koin.core)
                implementation(project(":core:graphql"))
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
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit4)
                implementation(libs.robolectric)
            }
        }
    }
}

android {
    namespace = APIConfig.BUNDLE_ID
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
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
        groupId = APIConfig.GROUP_ID
        version = project.version.toString()
        artifactId = when (name) {
            "kotlinMultiplatform" -> APIConfig.MAVEN_ARTIFACT_ID
            "release" -> "${APIConfig.MAVEN_ARTIFACT_ID}-android"
            else -> "${APIConfig.MAVEN_ARTIFACT_ID}-$name"
        }

        pom {
            name.set("Feast Multiplatform Library")
            description.set(APIConfig.PACKAGE_DESCRIPTION)
            url.set("https://github.com/${APIConfig.GITHUB_REPO}")
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
                connection.set("scm:git:git://github.com/${APIConfig.GITHUB_REPO}.git")
                developerConnection.set("scm:git:git://github.com/${APIConfig.GITHUB_REPO}.git")
                url.set("https://github.com/${APIConfig.GITHUB_REPO}")
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