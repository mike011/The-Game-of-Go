// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "GoEngine",
    platforms: [.iOS(.v18), .macOS(.v15)],
    products: [
        .library(name: "GoEngine", targets: ["GoEngine"])
    ],
    targets: [
        .target(name: "GoEngine"),
        .testTarget(name: "GoEngineTests", dependencies: ["GoEngine"]),
    ]
)
