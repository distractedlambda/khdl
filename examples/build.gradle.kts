import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":khdl"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
