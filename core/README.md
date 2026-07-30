# Feast Multiplatform Library - GraphQL

## What is it?

This is a Kotlin Multiplatform (KMP) library that provides GraphQL network integration and data fetching repositories for Guardian Feast client applications on both **Android** and **iOS**.

It encapsulates network communication, Apollo GraphQL query execution, response parsing, and exposes clean Kotlin coroutine / Swift async interfaces and Koin dependency injection modules.

---

## Scope of this library

### 1. Responsibilities
- **GraphQL Integration:** Manages GraphQL queries, operations, schema mapping, and serialization.
- **Repository Abstraction:** Exposes high-level data interfaces (`GraphQLRepository`) so client apps do not need to deal with GraphQL queries directly.
- **Cross-Platform Delivery:** Delivers Android AAR/JAR artifacts via Maven and iOS frameworks/Swift Packages via XCFramework.

### 2. Architecture & Modules
- **`core:graphql`**: Internal module containing Apollo GraphQL configurations, generated query models, network execution logic, and endpoint providers.
- **`core:api`**: Public-facing entry point exporting `GraphQLRepository`, platform-specific DI initialization functions (`androidFeastApiModule`, `iosFeastApiModule`), and Swift wrappers (`FeastIos`).

### 3. Versioning Strategy vs `library` Module
`core` uses an independent versioning lifecycle from the root `library` module:
- **`core` Modules (`core:api` & `core:graphql`):** Versioned independently via `core/version.txt` (currently `1.0.0-alpha01`), following Semantic Versioning (SemVer) suited for network and API contract releases.
- **`library` Module:** Uses root `version.txt` corresponding to unit conversions and recipe scaling domain rules.

---

## How to publish to Maven Central?

> This section will be filled once we have a release YAML file ready in the CI pipeline to publish to Maven Central.
## How to publish on local maven?

Follow the steps below to publish the `core` library to your local maven repository(run command in terminal at the root of the project):
1. Gradle sync to make sure all dependencies are downloaded

#### For Android:
1. ./gradlew :core:graphql:assemble
2. ./gradlew :core:graphql:build
3. ./gradlew :core:graphql:publishToMavenLocal 
4. ./gradlew :core:api:assemble
5. ./gradlew :core:api:build
6. ./gradlew :core:api:publishToMavenLocal

#### For iOS:
1. ./gradlew :core:graphql:assemble
2. ./gradlew :core:graphql:build
3. ./gradlew :core:graphql:assembleFeastMultiplatformGraphQLXCFramework
4. ./gradlew :core:api:assemble
5. ./gradlew :core:api:build
6. ./gradlew :core:api:assembleFeastMultiplatformAPIXCFramework

## iOS Setup

<This section will be filled up after confirming or by an iOS develper>

## Android Setup

Consumers only need to depend on `core:api`. Transitive dependencies (including `core:graphql`) are bundled automatically via Maven POM metadata:

// build.gradle.kts (module level)

dependencies {
implementation("com.gu:feast-multiplatform-api:<latest-version>") // e.g. 1.0.0-alpha01
}