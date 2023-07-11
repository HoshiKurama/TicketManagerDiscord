import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.0"
}

group = "com.github.hoshikurama"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}