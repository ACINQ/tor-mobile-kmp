plugins {
    kotlin("multiplatform") version "1.4.21"
    id("com.android.library")
}

group = "fr.acinq.tor"
version = "1.0"

repositories {
    google()
    jcenter()
}

kotlin {
    explicitApi()

    android()

    iosX64("ios") {
        compilations["main"].cinterops.create("tor_in_thread") {
            includeDirs.headerFilterOnly("$rootDir/native/tor_in_thread")

            val suffix = when (preset!!.name) {
                "iosX64" -> "IosX86_64"
                "arm64" -> "Arm64"
                else -> error("Unknown target ${preset!!.name}")
            }
            tasks[interopProcessingTaskName].dependsOn(":native:buildTor_in_thread$suffix")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-network:1.5.0")
                implementation("io.ktor:ktor-network-tls:1.5.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.1")
                implementation("androidx.test.ext:junit:1.1.2")
                implementation("androidx.test.espresso:espresso-core:3.3.0")
            }
        }

        val iosMain by getting
        val iosTest by getting

        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    externalNativeBuild {
        cmake {}
    }
    externalNativeBuild {
        cmake {
            setPath("src/androidMain/c/CMakeLists.txt")
        }
    }
}

afterEvaluate {
    configure(listOf("Debug", "Release").map { tasks["externalNativeBuild$it"] }) {
        dependsOn(":native:buildTor_in_threadAndroidArm64-v8a")
        dependsOn(":native:buildTor_in_threadAndroidArmeabi-v7a")
        dependsOn(":native:buildTor_in_threadAndroidX86")
        dependsOn(":native:buildTor_in_threadAndroidX86_64")
    }
}

//afterEvaluate {
//    tasks.withType<com.android.build.gradle.tasks.factory.AndroidUnitTest>().all {
//        enabled = false
//    }
//}
