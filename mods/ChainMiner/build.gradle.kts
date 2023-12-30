import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    kotlin("jvm") version "1.9.22"
    id("idea")
    id("net.minecraftforge.gradle")
}

val kotlinVersion: String by project(":").extra
val forge_version: String by extra
val forge_version_range: String by extra
val kotlin_for_forge_version_range: String by project(":").extra
val loader_version_range: String by extra
val mapping_channel: String by extra
val mapping_version: String by extra
val minecraft_version: String by extra
val minecraft_version_range: String by extra
val mod_authors: String by extra
val mod_description: String by extra
val mod_group_id: String by extra
val mod_id: String by extra
val mod_license: String by extra
val mod_name: String by extra
val mod_version: String by extra

version = mod_version
group = mod_group_id

base.archivesName.set("${mod_id}-forge-${minecraft_version}")

// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

minecraft {
    mappings(mapping_channel, mapping_version)

    copyIdeResources.set(true)

    runs {

        create("client") {
            workingDirectory(project.file("run"))
            property("forge.enabledGameTestNamespaces", mod_id)

            mods {
                create(mod_id) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("server") {
            workingDirectory(project.file("run"))
            property("forge.enabledGameTestNamespaces", mod_id)
            args("--nogui")

            mods {
                create(mod_id) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("gameTestServer") {
            workingDirectory(project.file("run"))
            property("forge.enabledGameTestNamespaces", mod_id)
        }

        create("data") {
            workingDirectory(project.file("run-data"))

            args("--mod", mod_id, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))
        }
    }
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

repositories {
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    minecraft("net.minecraftforge:forge:${minecraft_version}-${forge_version}")
    implementation("thedarkcolour:kotlinforforge:4.7.0")
}

tasks.withType<ProcessResources> {
    val replaceProperties = mapOf(
            "minecraft_version" to minecraft_version,
            "minecraft_version_range" to minecraft_version_range,
            "forge_version" to forge_version,
            "forge_version_range" to forge_version_range,
            "kotlin_for_forge_version_range" to kotlin_for_forge_version_range,
            "loader_version_range" to loader_version_range,
            "mod_id" to mod_id,
            "mod_name" to mod_name,
            "mod_license" to mod_license,
            "mod_version" to mod_version,
            "mod_authors" to mod_authors,
            "mod_description" to mod_description,
    )

    inputs.properties(replaceProperties)

    filesMatching(listOf("**/mods.toml", "**/pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.withType<Jar> {
    manifest {
        attributes(mapOf(
                "Specification-Title" to mod_id,
                "Specification-Vendor" to mod_authors,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to mod_authors,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
        ))
    }

    finalizedBy("reobfJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
