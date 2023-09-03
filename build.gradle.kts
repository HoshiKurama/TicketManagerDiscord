import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

application {
    mainClass.set("com.github.hoshikurama.tmdiscord.DummyFile")
}

group = "com.github.hoshikurama"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(path = ":BukkitForks", configuration = "shadow"))
    implementation(project(path = ":Velocity", configuration = "shadow"))
    implementation(project(path = ":WaterfallForks", configuration = "shadow"))
}

tasks {
    shadowJar {
        dependencies {
            include(project(":BukkitForks"))
            include(project(":Velocity"))
            include(project(":WaterfallForks"))
        }
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}