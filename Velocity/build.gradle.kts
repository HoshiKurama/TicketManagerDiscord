plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.ben-manes.versions") version "0.47.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
    java
}

application {
    mainClass.set("com.github.hoshikurama.tmdiscord.VelocityPlatform")
}

group = "com.github.hoshikurama"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven {
        name = "velocity"
        url = uri("https://nexus.velocitypowered.com/repository/maven-public/")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")

    implementation("com.github.HoshiKurama.TicketManager_API:Common:10.0.0-RC34")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation(project(":Core"))
}

tasks {
    shadowJar {
        dependencies {
            exclude { it.moduleGroup.startsWith("net.kyori") }
        }
        relocate("_COROUTINE", "com.github.hoshikurama.tmdiscord.shaded._COROUTINE")
        relocate("co.touchlab.stately", "com.github.hoshikurama.tmdiscord.shaded.touchlab.stately")
        relocate("com.google", "com.github.hoshikurama.tmdiscord.shaded.google")
        relocate("mu", "com.github.hoshikurama.tmdiscord.shaded.mu")
        relocate("javax.annotation", "com.github.hoshikurama.tmdiscord.shaded.javax.annotation")
        relocate("io.ktor", "com.github.hoshikurama.tmdiscord.shaded.ktor")
        relocate("dev.kord", "com.github.hoshikurama.tmdiscord.shaded.kord")
        relocate("org.checkerframework", "com.github.hoshikurama.tmdiscord.shaded.checkerframework")
        relocate("org.yaml.snakeyaml", "com.github.hoshikurama.tmdiscord.shaded.snakeyaml")
        relocate("kotlinx.serialization", "com.github.hoshikurama.tmdiscord.shaded.serialization")
        relocate("kotlin", "com.github.hoshikurama.tmdiscord.shaded.kotlin")
        relocate("kotlinx", "com.github.hoshikurama.tmdiscord.shaded.kotlinx")
        relocate("org.slf4j", "com.github.hoshikurama.tmdiscord.shaded.slf4j")
        relocate("org.intellij", "com.github.hoshikurama.tmdiscord.shaded.intellij")
        relocate("org.jetbrains", "com.github.hoshikurama.tmdiscord.shaded.jetbrains")
        //relocate("", "com.github.hoshikurama.tmdiscord.shaded.")
    }
}