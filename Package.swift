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
            url: "https://github.com/guardian/feast-multiplatform-library/releases/download/0.0.1-poc/FeastSharedLib.xcframework.zip",
            checksum:"ef97cf32291fe80fd9e5695a699957794b39c945a40768db9240beb9b761c62e")
    ]
)