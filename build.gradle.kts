plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.5.31"
    `maven-publish`
}

buildscript {
    dependencies {
        classpath("io.ktor:ktor-client-okhttp:${Versions.ktor}")
        classpath("io.ktor:ktor-client-auth-jvm:${Versions.ktor}")
    }
}

group = "fr.acinq.tor"
version = "0.2.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 31
    ndkVersion = Versions.ndk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    externalNativeBuild {
        cmake {
            path = File("src/androidMain/c/CMakeLists.txt")
        }
    }
}

kotlin {
    explicitApi()

    android {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    ios {
        compilations["main"].cinterops.create("tor_in_thread") {
            includeDirs.headerFilterOnly("$rootDir/native/tor_in_thread")
            tasks[interopProcessingTaskName].dependsOn(":native:buildTor_in_thread${target!!.name.capitalize()}")
        }
    }

    val secp256k1Version = "0.6.4"

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("fr.acinq.secp256k1:secp256k1-kmp:$secp256k1Version")
                implementation("io.ktor:ktor-network:${Versions.ktor}")
                implementation("io.ktor:ktor-network-tls:${Versions.ktor}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("fr.acinq.secp256k1:secp256k1-kmp-jni-android:${secp256k1Version}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
                implementation("androidx.test.ext:junit:1.1.3")
                implementation("androidx.test.espresso:espresso-core:3.4.0")
            }
        }

        val iosMain by getting
        val iosTest by getting

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
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

afterEvaluate {
    tasks.withType<AbstractTestTask> {
        testLogging {
            events("passed", "skipped", "failed", "standard_out", "standard_error")
            showExceptions = true
            showStackTraces = true
        }
    }
}


val snapshotNumber: String? by project
val gitRef: String? by project
val eapBranch = gitRef?.split("/")?.last() ?: "dev"

publishing {
    publications.withType<MavenPublication>().configureEach {
        version = project.version.toString()
        pom {
            name.set("Kotlin Multiplatform Tor library")
            description.set("A Kotlin Multiplatform library for Android & iOS to start, connect to, and control a Tor proxy.")
            url.set("https://github.com/ACINQ/tor-mobile-kmp")
            licenses {
                name.set("Apache License v2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
            issueManagement {
                system.set("Github")
                url.set("https://github.com/ACINQ/tor-mobile-kmp/issues")
            }
            scm {
                connection.set("https://github.com/ACINQ/tor-mobile-kmp.git")
            }
            developers {
                developer {
                    name.set("ACINQ")
                    email.set("hello@acinq.co")
                }
            }
        }
    }
}
