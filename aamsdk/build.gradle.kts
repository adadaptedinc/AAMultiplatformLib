import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("convention.publication")
    id("io.github.luca992.multiplatform-swiftpackage") version "2.1.1"
    kotlin("plugin.serialization") version "1.8.10"
}

val libraryName = "AAMultiplatformLib"
val libraryVersion = "1.2.3"
group = "com.adadapted"
version = libraryVersion

repositories {
    google()
    mavenCentral()
}

kotlin {
    android()

    android {
        publishLibraryVariants("release", "debug")
    }

    val xcframework = XCFramework(libraryName)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework(libraryName) {
            xcframework.add(this)
        }
        it.compilations.getByName("main") {
            val uikit by cinterops.creating {
                defFile("src/nativeInterop/cinterop/uikit.def")
                includeDirs("$rootDir/../$libraryName/aamsdk/src")
            }
        }
    }

    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
        binaries.all {
            binaryOptions["freezing"] = "disabled"
        }
    }

    sourceSets {
        val ktorVersion = "2.2.3"
        val kotlinVersion = "1.8.10"
        val kotlinXDateTimeVersion = "0.4.0"
        val kotlinCoroutineVersion = "1.6.1"
        val napierVersion = "2.6.1"

        val commonMain by getting {
            dependencies {
                // Ktor
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinXDateTimeVersion")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutineVersion")

                //Logging
                implementation("io.github.aakira:napier:$napierVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.material:material:1.9.0")
                implementation("io.ktor:ktor-client-android:$ktorVersion")
                implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
                implementation("io.ktor:ktor-client-mock:$ktorVersion")
            }
        }
        val iosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
        }
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 28
        targetSdk = 33
        consumerProguardFiles("aamsdk-proguard-rules.pro")
    }
}

multiplatformSwiftPackage {
    swiftToolsVersion("5.3")
    targetPlatforms {
        iOS { v("14") }
    }
    distributionMode {
        remote("https://github.com/adadaptedinc/AAMultiplatformLib")
    }
    zipFileName("AAMultiplatformLib")
    outputDirectory(File(projectDir.parent))
}
