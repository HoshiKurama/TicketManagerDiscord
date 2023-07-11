plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.github.hoshikurama"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.kord:kord-core:0.10.0")
    implementation("com.google.guava:guava:32.1.1-jre")
}