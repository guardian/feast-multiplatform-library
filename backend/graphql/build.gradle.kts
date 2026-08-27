import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

object GraphQLConfig {
    const val GROUP_ID = "com.gu"
    const val MAVEN_ARTIFACT_ID = "feast-multiplatform-graphql"
    const val SPM_FRAMEWORK_NAME = "FeastMultiplatformGraphQL"
    const val BUNDLE_ID = "com.gu.recipe.kmp.graphql"
    const val GITHUB_REPO = "guardian/feast-multiplatform-library"
    const val PACKAGE_DESCRIPTION = "A Kotlin Multiplatform library that holds graphql impl"
}

group = GraphQLConfig.GROUP_ID
version = file("../version.txt").readText().trim()

plugins {
    `maven-publish`
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.apollo)
    alias(libs.plugins.metalava)
}

val schemaDirectory: Directory = layout.projectDirectory.dir("src/commonMain/graphql")
val localSchemaFile: RegularFile = schemaDirectory.file("schema.graphqls")

val graphQlIntrospectionUrl = providers
    .gradleProperty("graphql.introspection.url")
    .orElse(providers.environmentVariable("FEAST_GRAPHQL_INTROSPECTION_URL"))

val graphQlIntrospectionHeaders = providers
    .gradleProperty("graphql.introspection.headers")
    .orElse(providers.environmentVariable("FEAST_GRAPHQL_INTROSPECTION_HEADERS"))

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
        }
    }

    val xcf = XCFramework(GraphQLConfig.SPM_FRAMEWORK_NAME)

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.all {
            linkerOpts("-lsqlite3")
        }
        target.binaries.framework {
            baseName = GraphQLConfig.SPM_FRAMEWORK_NAME

            // Specify CFBundleIdentifier to uniquely identify the framework
            binaryOption("bundleId", GraphQLConfig.BUNDLE_ID)
            xcf.add(this)
            isStatic = false
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.apollo.runtime)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.core)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.normalised.cache.sqlite)
                implementation(libs.koin.android)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit4)
                implementation(libs.robolectric)
                implementation(libs.androidx.test.core)
            }
        }
        val iosMain by getting
    }
}


android {
    namespace = GraphQLConfig.BUNDLE_ID
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

    apollo {
        service("feast") {
            packageName.set("com.gu.recipe.backend.graphql.generated")
            srcDir("src/commonMain/graphql")
            schemaFile.set(localSchemaFile)

            graphQlIntrospectionUrl.orNull
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let { introspectionUrl ->
                    introspection {
                        endpointUrl.set(introspectionUrl)
                        schemaFile.set(localSchemaFile)

                        graphQlIntrospectionHeaders.orNull
                            ?.split(',')
                            ?.map(String::trim)
                            ?.filter(String::isNotBlank)
                            ?.forEach { rawHeader ->
                                val separatorIndex = rawHeader.indexOf(':')

                                require(separatorIndex > 0) {
                                    "Invalid GraphQL introspection header '$rawHeader'. " +
                                            "Use 'Header-Name: value;Other-Header: value'."
                                }

                                val headerName = rawHeader
                                    .substring(0, separatorIndex)
                                    .trim()

                                val headerValue = rawHeader
                                    .substring(separatorIndex + 1)
                                    .trim()

                                headers.put(headerName, headerValue)
                            }
                    }
                }
        }
    }
}

val introspectionConfigured = graphQlIntrospectionUrl.orNull
    ?.trim()
    ?.isNotEmpty() == true

if (introspectionConfigured) {
    tasks.matching { task ->
        task.name.contains("Apollo", ignoreCase = true) &&
                task.name.contains("generate", ignoreCase = true)
    }.configureEach {
        dependsOn("downloadFeastApolloSchemaFromIntrospection")
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        groupId = GraphQLConfig.GROUP_ID
        version = project.version.toString()
        artifactId = when (name) {
            "kotlinMultiplatform" -> GraphQLConfig.MAVEN_ARTIFACT_ID
            "release" -> "${GraphQLConfig.MAVEN_ARTIFACT_ID}-android"
            else -> "${GraphQLConfig.MAVEN_ARTIFACT_ID}-$name"
        }

        pom {
            name.set("Feast Multiplatform Library")
            description.set(GraphQLConfig.PACKAGE_DESCRIPTION)
            url.set("https://github.com/${GraphQLConfig.GITHUB_REPO}")

            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("guardian/feast")
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
                connection.set("scm:git:git://github.com/${GraphQLConfig.GITHUB_REPO}.git")
                developerConnection.set("scm:git:git://github.com/${GraphQLConfig.GITHUB_REPO}.git")
                url.set("https://github.com/${GraphQLConfig.GITHUB_REPO}")
            }
        }
    }

    repositories {
        // Adds a task for publishing locally to the build directory.
        // Use as `./gradlew :backend:graphql:publishReleasePublicationToCustomRepository`
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

tasks.withType<Test> {
    reports {
        junitXml.required.set(true)
    }
}