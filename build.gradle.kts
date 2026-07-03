plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}


// Fix CVEs: CVE-2026-42583, CVE-2026-42584, CVE-2026-42587, CVE-2026-45416, CVE-2026-44249, CVE-2026-50010
// Force all affected Netty artifacts to patched version (transitive via AGP)
// Fix CVEs: CVE-2026-54512, CVE-2026-54513
// Force jackson-databind and related artifacts to patched version (transitive via AGP/Kotlin tooling)
allprojects {
    configurations.all {
        resolutionStrategy {
            force("io.netty:netty-codec:${libs.versions.netty.get()}")
            force("io.netty:netty-codec-http:${libs.versions.netty.get()}")
            force("io.netty:netty-codec-http2:${libs.versions.netty.get()}")
            force("io.netty:netty-handler:${libs.versions.netty.get()}")
            force("com.fasterxml.jackson.core:jackson-databind:${libs.versions.jackson.get()}")
            force("com.fasterxml.jackson.core:jackson-core:${libs.versions.jackson.get()}")
            force("com.fasterxml.jackson.core:jackson-annotations:${libs.versions.jackson.get()}")
        }
    }
}

