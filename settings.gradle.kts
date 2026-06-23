pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "feast-multiplatform-library"
include(":library")
include(":core:api")
include(":core:networking")
include(":core:graphql")
