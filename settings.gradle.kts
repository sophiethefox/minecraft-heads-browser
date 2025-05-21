pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://maven.architectury.dev")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net")
    }

    plugins {
        val egtVersion = "0.6.7"
        id("gg.essential.multi-version.root") version egtVersion
        id("org.jetbrains.kotlin.jvm") version "1.9.0"
    }
}

rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.21.0-fabric",
    "1.21.5-fabric",
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        // This is where the `build` folder and per-version overwrites will reside
        projectDir = file("versions/$version")

        // All subprojects get configured by the same `build.gradle.kts` file, the string is relative to projectDir
        // You could use separate build files for each project, but usually that would just be duplicating lots of code
        buildFileName = "../../build.gradle.kts"
    }
}