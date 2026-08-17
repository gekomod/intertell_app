plugins {
    id("com.android.application") version "8.5.2" apply false
    // Bumped from 1.9.24 — org.maplibre.gl:android-sdk 13.5.0 pulls in
    // kotlin-stdlib 2.2.10 transitively, and a lower Kotlin compiler can't
    // read that metadata ("Module was compiled with an incompatible
    // version of Kotlin"); matching 2.2.10 exactly keeps everything on one
    // resolved version. This Kotlin line also needs the separate Compose
    // Compiler Gradle plugin below — see app/build.gradle.kts, which
    // replaces the old composeOptions.kotlinCompilerExtensionVersion.
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
