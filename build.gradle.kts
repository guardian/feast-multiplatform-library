plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply  false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}


// Fix CVE: Netty HTTP/2 CONTINUATION Frame Flood DoS - transitive via AGP 8.12.3 (netty 4.1.110.Final)
allprojects {
    configurations.all {
        resolutionStrategy {
            force("io.netty:netty-codec-http2:4.1.132.Final")
        }
    }
}

