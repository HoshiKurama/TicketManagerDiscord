plugins {
    kotlin("jvm")
}

group = "com.github.hoshikurama"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.kord:kord-core:0.11.1")
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("org.yaml:snakeyaml:2.2")
    compileOnly("com.github.HoshiKurama.TicketManager_API:Common:11.0.0-RC7")
}