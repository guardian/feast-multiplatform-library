// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "FeastSharedLib",
    platforms: [
        .iOS(.v17),
    ],
    products: [
        .library(name: "FeastSharedLib", targets: ["FeastSharedLib"])
    ],
    targets: [
        .binaryTarget(
            name: "FeastSharedLib",
            url: "https://github.com/guardian/feast-multiplatform-library/releases/download/0.0.0-poc/FeastSharedLib.xcframework.zip",
            checksum:"d59ddd6345ed17dd29ac8f531a98f5126416dba2bd5d70282208ebf12c6d18bd")
    ]
)