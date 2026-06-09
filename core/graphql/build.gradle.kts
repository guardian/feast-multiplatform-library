import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidLibrary)
	alias(libs.plugins.apollo)
}

val schemaDirectory = layout.projectDirectory.dir("src/commonMain/graphql")
val localSchemaFile = schemaDirectory.file("schema.graphqls")
val schemaDownloadUrl = providers.gradleProperty("graphql.schema.download.url")
	.orElse(providers.environmentVariable("FEAST_GRAPHQL_SCHEMA_DOWNLOAD_URL"))
val schemaHeaders = providers.gradleProperty("graphql.schema.download.headers")
	.orElse(providers.environmentVariable("FEAST_GRAPHQL_SCHEMA_DOWNLOAD_HEADERS"))
val strictSchemaRefresh = providers.gradleProperty("graphql.schema.strict")
	.map(String::toBooleanStrictOrNull)
	.orElse(false)

val downloadGraphQlSchema by tasks.registering {
	group = "graphql"
	description = "Refreshes the Feast GraphQL schema before Apollo code generation."
	outputs.file(localSchemaFile)
	inputs.property("schemaDownloadUrl", schemaDownloadUrl.orNull ?: "")
	inputs.property("schemaDownloadHeaders", schemaHeaders.orNull ?: "")
	outputs.upToDateWhen { false }

	doLast {
		val schemaTarget = localSchemaFile.asFile
		schemaTarget.parentFile.mkdirs()

		val downloadUrl = schemaDownloadUrl.orNull?.trim().orEmpty()
		if (downloadUrl.isBlank()) {
			if (strictSchemaRefresh.get()) {
				throw GradleException(
					"GraphQL schema refresh is in strict mode, but no schema download URL was configured. " +
						"Set graphql.schema.download.url or FEAST_GRAPHQL_SCHEMA_DOWNLOAD_URL.",
				)
			}
			logger.lifecycle("No GraphQL schema download URL configured. Using the checked-in schema at ${schemaTarget.path}.")
			return@doLast
		}

		val connection = URI(downloadUrl).toURL().openConnection().apply {
			connectTimeout = 15_000
			readTimeout = 30_000
			schemaHeaders.orNull
				?.split(';')
				?.map(String::trim)
				?.filter(String::isNotBlank)
				?.forEach { rawHeader ->
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
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(libs.kotlin.test)
				implementation(libs.kotlinx.coroutines.test)
				implementation(libs.koin.core)
			}
		}
		val androidMain by getting
	}
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
	}
}

tasks.matching { task -> task.name.contains("Apollo", ignoreCase = true) && task.name.contains("generate", ignoreCase = true) }
	.configureEach {
		dependsOn(downloadGraphQlSchema)
	}


