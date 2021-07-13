import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.20"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "bercic.mihael"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}



dependencies {
    testImplementation(kotlin("test"))
    implementation("org.openjfx:javafx:17-ea+14")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    javafx {
        modules("javafx.controls", "javafx.fxml", "javafx.media")
    }
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}