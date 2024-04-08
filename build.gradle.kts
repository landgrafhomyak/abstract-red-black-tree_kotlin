plugins {
    kotlin("multiplatform") version "1.9.0"
    `maven-publish`
}

group = "ru.landgrafhomyak.collections"
version = "1.2"

repositories {
    mavenCentral()
    maven("https://maven.landgrafhomyak.ru/")
}


kotlin {
    jvm {
        jvmToolchain(8)
    }
    js {
        browser()
        nodejs()
    }

    linuxArm64()
    linuxX64()
    mingwX64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    macosArm64()
    macosX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    ios()
    tvos()
    watchos()

//    wasm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("ru.landgrafhomyak.collections:binary-tree-utilities:1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}