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
            checksum: "46bc8ed3e2104733abfb4bf9230f4940a3de46c080721de83302725632c0e4dd"
        ),
    ]
)
