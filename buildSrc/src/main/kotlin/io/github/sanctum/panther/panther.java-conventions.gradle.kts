/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // annotations only needed at compile time
    compileOnly("org.jetbrains:annotations:23.0.0")
    // test suite
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}
