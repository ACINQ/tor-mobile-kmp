import org.gradle.internal.impldep.org.apache.http.auth.UsernamePasswordCredentials
import org.gradle.internal.impldep.org.apache.http.client.methods.HttpPost
import org.gradle.internal.impldep.org.apache.http.entity.ContentType
import org.gradle.internal.impldep.org.apache.http.entity.StringEntity
import org.gradle.internal.impldep.org.apache.http.impl.auth.BasicScheme
import org.gradle.internal.impldep.org.apache.http.impl.client.HttpClients

plugins {
    id("com.android.library")
    kotlin("multiplatform") version "1.4.21"
    `maven-publish`
}

group = "fr.acinq.tor"
version = "0.1.0"

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
                val ktorVersion = "1.5.0"
                implementation("io.ktor:ktor-network:$ktorVersion")
                implementation("io.ktor:ktor-network-tls:$ktorVersion")
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
val bintrayVersion = if (snapshotNumber != null) "${project.version}-$eapBranch-$snapshotNumber" else project.version.toString()
val bintrayRepo = if (snapshotNumber != null) "snapshots" else "libs"

val bintrayUsername: String? = (properties["bintrayUsername"] as String?) ?: System.getenv("BINTRAY_USER")
val bintrayApiKey: String? = (properties["bintrayApiKey"] as String?) ?: System.getenv("BINTRAY_APIKEY")
val hasBintray = bintrayUsername != null && bintrayApiKey != null
if (!hasBintray) logger.warn("Skipping bintray configuration as bintrayUsername or bintrayApiKey is not defined")

publishing {
    if (hasBintray) {
        repositories {
            maven {
                name = "bintray"
                setUrl("https://api.bintray.com/maven/acinq/$bintrayRepo/${rootProject.name}/;publish=0")
                credentials {
                    username = bintrayUsername
                    password = bintrayApiKey
                }
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        version = bintrayVersion
        pom {
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
        }
    }
}

if (hasBintray) {
    val postBintrayPublish by tasks.creating {
        group = "bintray"
        doLast {
            HttpClients.createDefault().use { client ->
                client.execute(HttpPost("https://api.bintray.com/content/acinq/$bintrayRepo/${rootProject.name}/$bintrayVersion/publish").apply {
                    addHeader(BasicScheme().authenticate(UsernamePasswordCredentials(bintrayUsername, bintrayApiKey), this, null))
                    entity = StringEntity("{}", ContentType.APPLICATION_JSON)
                })
            }
        }
    }

    val postBintrayDiscard by tasks.creating {
        group = "bintray"
        doLast {
            HttpClients.createDefault().use { client ->
                client.execute(HttpPost("https://api.bintray.com/content/acinq/$bintrayRepo/${rootProject.name}/$bintrayVersion/publish").apply {
                    addHeader(BasicScheme().authenticate(UsernamePasswordCredentials(bintrayUsername, bintrayApiKey), this, null))
                    entity = StringEntity("{ \"discard\": true }", ContentType.APPLICATION_JSON)
                })
            }
        }
    }
}
