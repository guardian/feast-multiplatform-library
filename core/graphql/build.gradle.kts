import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
import java.net.URI

object Config {
	const val GROUP_ID = "com.gu"
	const val MAVEN_ARTIFACT_ID = "feast-multiplatform-library"
	const val SPM_FRAMEWORK_NAME = "FeastMultiplatformLibrary"
	const val BUNDLE_ID = "com.gu.feast.shared"
	const val GITHUB_REPO = "guardian/feast-multiplatform-library"
	const val PACKAGE_DESCRIPTION = "A Kotlin Multiplatform library to handle recipe templates"
}

abstract class DownloadGraphQlSchemaTask : DefaultTask() {
	@get:OutputFile
	abstract val schemaFile: RegularFileProperty

	@get:Input
	abstract val schemaDownloadUrl: Property<String>

	@get:Input
	abstract val schemaDownloadHeaders: Property<String>

	@get:Input
	abstract val strictSchemaRefresh: Property<Boolean>

	@TaskAction
	fun refreshSchema() {
		val schemaTarget = schemaFile.get().asFile
		schemaTarget.parentFile.mkdirs()

		val downloadUrl = schemaDownloadUrl.get().trim()
		if (downloadUrl.isBlank()) {
			if (strictSchemaRefresh.get()) {
				throw GradleException(
					"GraphQL schema refresh is in strict mode, but no schema download URL was configured. " +
						"Set graphql.schema.download.url or FEAST_GRAPHQL_SCHEMA_DOWNLOAD_URL.",
				)
			}
			logger.lifecycle("No GraphQL schema download URL configured. Using the checked-in schema at ${schemaTarget.path}.")
			return
		}

		val connection = URI(downloadUrl).toURL().openConnection().apply {
			connectTimeout = 15_000
			readTimeout = 30_000
			schemaDownloadHeaders.get()
				.split(';')
				.map(String::trim)
				.filter(String::isNotBlank)
				.forEach { rawHeader ->
					val separatorIndex = rawHeader.indexOf(':')
					require(separatorIndex > 0) {
						"Invalid GraphQL schema header '$rawHeader'. Use 'Header-Name: value;Other-Header: value'."
					}
					setRequestProperty(
						rawHeader.substring(0, separatorIndex).trim(),
						rawHeader.substring(separatorIndex + 1).trim(),
					)
				}
		}

		val schemaContents = connection.getInputStream().bufferedReader().use { it.readText() }
		if (schemaContents.isBlank()) {
			throw GradleException("Downloaded GraphQL schema was empty from $downloadUrl")
		}

		schemaTarget.writeText(schemaContents)
		logger.lifecycle("GraphQL schema refreshed at ${schemaTarget.path}")
	}
}

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
	alias(libs.plugins.apollo)
	alias(libs.plugins.hiltAndroid)
	alias(libs.plugins.ksp)
}

val schemaDirectory = layout.projectDirectory.dir("src/commonMain/graphql")
val localSchemaFile = schemaDirectory.file("schema.graphqls")
val graphQlSchemaDownloadUrl = providers.gradleProperty("graphql.schema.download.url")
	.orElse(providers.environmentVariable("FEAST_GRAPHQL_SCHEMA_DOWNLOAD_URL"))
val graphQlSchemaHeaders = providers.gradleProperty("graphql.schema.download.headers")
	.orElse(providers.environmentVariable("FEAST_GRAPHQL_SCHEMA_DOWNLOAD_HEADERS"))
val graphQlStrictSchemaRefresh = providers.gradleProperty("graphql.schema.strict")
	.map(String::toBooleanStrictOrNull)
	.orElse(false)


val downloadGraphQlSchema by tasks.registering(DownloadGraphQlSchemaTask::class) {
	group = "graphql"
	description = "Refreshes the Feast GraphQL schema before Apollo code generation."
	outputs.upToDateWhen { false }
	schemaFile.set(localSchemaFile)
	schemaDownloadUrl.set(graphQlSchemaDownloadUrl.orElse(""))
	schemaDownloadHeaders.set(graphQlSchemaHeaders.orElse(""))
	strictSchemaRefresh.set(graphQlStrictSchemaRefresh)
}

kotlin {
	androidTarget {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		compilerOptions {
			jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvm.get()))
		}
	}

	js(IR) {
		nodejs()
	}

	iosX64()
	iosArm64()
	iosSimulatorArm64()

	applyDefaultHierarchyTemplate()

	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":core:networking"))
				implementation(libs.apollo.runtime)
				implementation(libs.koin.core)
				implementation(libs.kotlinx.coroutines.core)
				implementation(libs.kotlinx.datetime)
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
				implementation(libs.koin.android)
				implementation(libs.hilt.android)
			}
		}
		val iosMain by getting
	}
}

dependencies {
	add("kspAndroid", libs.hilt.compiler)
}

android {
	namespace = "com.gu.recipe.core.graphql"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}

	compileOptions {
		sourceCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
		targetCompatibility = JavaVersion.toVersion(libs.versions.jvm.get())
	}
}

apollo {
	service("feast") {
		packageName.set("com.gu.recipe.core.graphql.generated")
		srcDir("src/commonMain/graphql")
		schemaFile.set(localSchemaFile)
		introspection {
			endpointUrl.set("https://unified-recipes-test.code.dev-gutools.co.uk/graphql")
			schemaFile.set(file("src/main/graphql/schema.graphqls"))
		}
	}
}

tasks.matching { task -> task.name.contains("Apollo", ignoreCase = true) && task.name.contains("generate", ignoreCase = true) }
	.configureEach {
		dependsOn(downloadGraphQlSchema)
	}


