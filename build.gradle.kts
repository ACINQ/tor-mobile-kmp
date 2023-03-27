import org.jetbrains.dokka.Platform

plugins {
    id("com.android.library") version "7.2.2"
    kotlin("multiplatform") version Versions.kotlin
    id("org.jetbrains.dokka") version Versions.kotlin
    `maven-publish`
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:${Versions.kotlin}")
    }
}

group = "fr.acinq.tor"
version = "0.2.0"

repositories {
    google()
    mavenCentral()
}

android {
    compileSdk = 33
    ndkVersion = Versions.ndk
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    externalNativeBuild {
        cmake {
            path = File("src/androidMain/c/CMakeLists.txt")
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("fr.acinq.secp256k1:secp256k1-kmp:${Versions.secp256k1}")
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
                implementation("fr.acinq.secp256k1:secp256k1-kmp-jni-android:${Versions.secp256k1}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
                implementation("androidx.test.ext:junit:1.1.4")
                implementation("androidx.test.espresso:espresso-core:3.5.0")
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

val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

afterEvaluate {
    val dokkaOutputDir = buildDir.resolve("dokka")

    tasks.dokkaHtml {
        outputDirectory.set(file(dokkaOutputDir))
        dokkaSourceSets {
            configureEach {
                val platformName = when (platform.get()) {
                    Platform.jvm -> "jvm"
                    Platform.js -> "js"
                    Platform.native -> "native"
                    Platform.common -> "common"
                }
                displayName.set(platformName)
                perPackageOption {
                    matchingRegex.set(".*\\.internal.*") // will match all .internal packages and sub-packages
                    suppress.set(true)
                }
            }
        }
    }

    val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
        delete(dokkaOutputDir)
    }

    javadocJar.dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    javadocJar.from(dokkaOutputDir)
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        version = project.version.toString()
        artifact(javadocJar)
        pom {
            name.set("Kotlin Multiplatform Tor library")
            description.set("A Kotlin Multiplatform library for Android & iOS to start, connect to, and control a Tor proxy.")
            url.set("https://github.com/ACINQ/tor-mobile-kmp")
            licenses {
                license {
                    name.set("Apache License v2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            issueManagement {
                system.set("Github")
                url.set("https://github.com/ACINQ/tor-mobile-kmp/issues")
            }
            scm {
                connection.set("https://github.com/ACINQ/tor-mobile-kmp.git")
                url.set("https://github.com/ACINQ/tor-mobile-kmp")
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
