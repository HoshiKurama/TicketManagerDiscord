plugins {
    kotlin("jvm")
    id("com.github.ben-manes.versions") version "0.50.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

application {
    mainClass.set("com.github.hoshikurama.tmdiscord.BukkitPlatform")
}

group = "com.github.hoshikurama"
version = "3.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.github.HoshiKurama.TicketManager_API:Common:11.0.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    implementation(project(":Core"))
}

tasks {
    shadowJar {
        dependencies {

            // Provided by TicketManager
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("org.jetbrains.kotlinx:.*:.*"))
            exclude(dependency("com.github.HoshiKurama.TicketManager_API:Common:.*"))
        }

        // Provided by TicketManager
        relocate("kotlin", "com.github.hoshikurama.ticketmanager.shaded.kotlin")
        relocate("kotlinx", "com.github.hoshikurama.ticketmanager.shaded.kotlinx")

        // Included but relocated internally
        relocate("_COROUTINE", "com.github.hoshikurama.tmdiscord.shaded._COROUTINE")
        relocate("co.touchlab.stately", "com.github.hoshikurama.tmdiscord.shaded.touchlab.stately")
        relocate("com.google", "com.github.hoshikurama.tmdiscord.shaded.google")
        relocate("mu", "com.github.hoshikurama.tmdiscord.shaded.mu")
        relocate("javax.annotation", "com.github.hoshikurama.tmdiscord.shaded.javax.annotation")
        relocate("io.ktor", "com.github.hoshikurama.tmdiscord.shaded.ktor")
        relocate("dev.kord", "com.github.hoshikurama.tmdiscord.shaded.kord")
        relocate("org.checkerframework", "com.github.hoshikurama.tmdiscord.shaded.checkerframework")
        relocate("org.yaml.snakeyaml", "com.github.hoshikurama.tmdiscord.shaded.snakeyaml")
    }
}