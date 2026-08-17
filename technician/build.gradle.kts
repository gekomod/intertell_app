plugins {
    id("com.android.application") version "8.5.2" apply false
    // Bumped from 1.9.24 — org.maplibre.gl:android-sdk 13.5.0 (and its
    // kotlinx-coroutines-core transitive dep) ship Kotlin metadata newer
    // than a 1.9.x compiler can read ("Class 'kotlin.Unit' was compiled
    // with an incompatible version of Kotlin"). 2.0.21 is the Compose
    // Compiler Gradle plugin era — see the plugin.compose line below and
    // app/build.gradle.kts, which replaces the old composeOptions.kotlinCompilerExtensionVersion.
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
