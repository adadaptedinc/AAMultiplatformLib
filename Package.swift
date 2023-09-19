// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "AAMultiplatformLib",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "AAMultiplatformLib",
            targets: ["AAMultiplatformLib"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "AAMultiplatformLib",
            url: "https://github.com/adadaptedinc/AAMultiplatformLib/AAMultiplatformLib.zip",
            checksum: "f077a48ace45f8826d60f6555f18001a248da50461ed4ea852f8e0f75bb9cf9e"
        ),
    ]
)
