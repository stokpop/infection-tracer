import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "nl.stokpop.infection"
version = "0.0.1-SNAPSHOT"

val ktor_version = "1.3.2"

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.3.61"
    // generate json adapters
    kotlin("kapt") version "1.3.61"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    mavenLocal()
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.2.1")

    implementation("com.ryanharter.ktor:ktor-moshi:1.0.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.9.2")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.9.2")


    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}

application {
    // Define the main class for the application.
    mainClassName = "nl.stokpop.infection.InfectionTracerKt"
}

//kotlin.sourceSets["main"].kotlin.srcDirs("src")
//kotlin.sourceSets["test"].kotlin.srcDirs("test")
//
//sourceSets["main"].resources.srcDirs("resources")
//sourceSets["test"].resources.srcDirs("testresources")


