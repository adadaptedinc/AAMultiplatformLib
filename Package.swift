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
            checksum: "97edd7ae22157a65558f9e78f80150cff4a3cc802371f65093ed2f7a35dca938"
        ),
    ]
)
