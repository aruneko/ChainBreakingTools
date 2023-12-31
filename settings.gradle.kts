pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.minecraftforge.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.5.0")
}

rootProject.name = "ChainBreakingTools Mods"

include("mods:ChainDigger")
include("mods:ChainMiner")
include("mods:ChainWoodCutter")
