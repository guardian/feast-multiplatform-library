plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}


// Fix CVEs: CVE-2025-24970 (GHSA-4g8c-wm8x-jfhw), CVE-2026-42583, CVE-2026-42584, CVE-2026-42587, CVE-2026-45416, CVE-2026-44249, CVE-2026-50010
// Force all affected Netty artifacts to patched version (transitive via AGP)
// CVE-2025-24970: SslHandler packet validation flaw; fixed in 4.1.118.Final — current forced version satisfies this.
// Fix CVE-2025-14813: force patched Bouncy Castle provider when pulled transitively.
// Fix CVEs: CVE-2026-54512, CVE-2026-54513
// Force jackson-databind and related artifacts to patched version (transitive via AGP/Kotlin tooling)
allprojects {
    configurations.all {
        resolutionStrategy {
            force("io.netty:netty-codec:${libs.versions.netty.get()}")
            force("io.netty:netty-codec-http:${libs.versions.netty.get()}")
            force("io.netty:netty-codec-http2:${libs.versions.netty.get()}")
            force("io.netty:netty-handler:${libs.versions.netty.get()}")
            force("org.bouncycastle:bcprov-jdk18on:${libs.versions.bouncycastle.get()}")
            force("org.bouncycastle:bcprov-jdk15to18:${libs.versions.bouncycastle.get()}")
            force("com.fasterxml.jackson.core:jackson-databind:${libs.versions.jackson.get()}")
            force("com.fasterxml.jackson.core:jackson-core:${libs.versions.jackson.get()}")
            force("com.fasterxml.jackson.core:jackson-annotations:${libs.versions.jackson.get()}")
        }
    }
}

