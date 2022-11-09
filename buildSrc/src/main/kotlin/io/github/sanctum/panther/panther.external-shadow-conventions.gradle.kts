plugins {
    `java-library`
    id("panther.shadow-conventions")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("bundled")
    // Minimize guava (see https://imperceptiblethoughts.com/shadow/configuration/minimizing/#:~:text=Dependencies%20scoped%20as%20api)
    minimize()
    val relocationPackage = "relocations.panther"
    // Relocate what's left
    relocate("com.google.common", "$relocationPackage.guava")
    // guava ->depends error_prone_annotations:2.11.0
    relocate("com.google.errorprone", "$relocationPackage.guava_error_prone_annotations")
    // guava ->depends j2objc-annotations:1.3
    relocate("com.google.j2objc", "$relocationPackage.guava_j2objc_annotations")
    // guava ->depends jsr305:3.0.2
    relocate("javax.annotation", "$relocationPackage.guava_jsr305")
    // guava ->depends checker-qual:3.12.0
    relocate("org.checkerframework", "$relocationPackage.guava_checker_qual")
    // Leave gson and json-simple alone
}
