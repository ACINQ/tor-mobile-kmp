pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android") useModule("com.android.tools.build:gradle:7.1.3")
        }
    }
}
rootProject.name = "tor-mobile-kmp"

include(":native")
