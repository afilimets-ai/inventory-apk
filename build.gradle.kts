// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.22" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
