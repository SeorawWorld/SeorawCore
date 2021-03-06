import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktor_version = "1.6.7"

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("io.ktor:ktor-html-builder:$ktor_version")
    compileOnly("io.ktor:ktor-server-core:$ktor_version")
    compileOnly("io.ktor:ktor-server-netty:$ktor_version")
    compileOnly("io.ktor:ktor-client-core:$ktor_version")
    compileOnly("io.ktor:ktor-client-cio:$ktor_version")
    compileOnly(kotlin("stdlib"))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<ShadowJar> {
        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}