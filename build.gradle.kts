plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.4.21"
    `maven-publish`
}

group = "fr.acinq.tor"
version = "1.0"

repositories {
    google()
    jcenter()
}

val projectNdkVersion: String by extra { "21.3.6528147" }

android {
    compileSdkVersion(30)
    ndkVersion = projectNdkVersion
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

kotlin {
    explicitApi()

    android {
        publishLibraryVariants("release")
    }

    ios {
        compilations["main"].cinterops.create("tor_in_thread") {
            includeDirs.headerFilterOnly("$rootDir/native/tor_in_thread")
            tasks[interopProcessingTaskName].dependsOn(":native:buildTor_in_thread${target.name.capitalize()}")
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

afterEvaluate {
    configure(listOf("Debug", "Release").map { tasks["externalNativeBuild$it"] }) {
        dependsOn(":native:buildTor_in_threadAndroidArm64-v8a")
        dependsOn(":native:buildTor_in_threadAndroidArmeabi-v7a")
        dependsOn(":native:buildTor_in_threadAndroidX86")
        dependsOn(":native:buildTor_in_threadAndroidX86_64")
    }
}

afterEvaluate {
    tasks.withType<com.android.build.gradle.tasks.factory.AndroidUnitTest>().all {
        enabled = false
    }
}
