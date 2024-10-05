import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.0.20"
    `maven-publish`
}

group = "ru.landgrafhomyak.collections"
version = "1.2"

repositories {
    mavenCentral()
    maven("https://maven.landgrafhomyak.ru/")
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvmToolchain(8)
    jvm {
    }
    js(IR) {
        browser()
        nodejs()
    }
    wasmWasi {
        nodejs()
    }
    wasmJs {
        nodejs()
        browser()
        d8()
    }

    macosArm64()
    macosX64()
    linuxArm64()
    linuxX64()
    mingwX64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation("ru.landgrafhomyak.collections:binary-tree-utilities:1.0")
            }
        }
    }
}