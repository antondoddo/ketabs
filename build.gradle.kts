import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "me.damianopetrungaro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-auth:1.6.8")
    implementation("io.ktor:ktor-auth-jwt:1.6.8")
    implementation("io.ktor:ktor-server-core:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8")
    implementation("io.ktor:ktor-serialization:1.6.8")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("io.arrow-kt:arrow-core:1.0.1")
    implementation("org.mindrot:jbcrypt:0.4")

    testImplementation(kotlin("test"))
    testImplementation("io.github.serpro69:kotlin-faker:1.10.0")
    testImplementation("io.kotest:kotest-runner-junit5:5.2.1")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.2.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.8")
    testImplementation("io.ktor:ktor-server-test-host:1.6.8")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}
