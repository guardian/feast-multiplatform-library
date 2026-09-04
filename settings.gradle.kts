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
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":library")
