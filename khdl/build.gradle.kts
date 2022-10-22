import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
