plugins {
    id("net.minecraftforge.gradle") version("[6.0,6.2)") apply(false)
}

subprojects {
    apply(plugin = "java")
    repositories {
        mavenCentral()
    }
}