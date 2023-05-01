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
            checksum: "63a15eda853c8df115e49754acc0bf2988c9a61cd374c113b485f7eea4393f34"
        ),
    ]
)
