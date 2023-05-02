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
            checksum: "039068c905853259463dc989ecc891eba17436d19519c6da2e398cfe192240dc"
        ),
    ]
)
