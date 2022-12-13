plugins {
    `java-library`
    id("panther.shadow-conventions")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("bundled")
    // Minimize guava and httpclient5 (see https://imperceptiblethoughts.com/shadow/configuration/minimizing/#:~:text=Dependencies%20scoped%20as%20api)
    minimize()
    val relocationPackage = "relocations.panther"
    // Relocate what's left
    // guava:31.1-jre
    relocate("com.google.common", "$relocationPackage.guava")
    // guava ->depends error_prone_annotations:2.11.0
    relocate("com.google.errorprone", "$relocationPackage.guava_error_prone_annotations")
    // guava ->depends j2objc-annotations:1.3
    relocate("com.google.j2objc", "$relocationPackage.guava_j2objc_annotations")
    // guava ->depends jsr305:3.0.2
    relocate("javax.annotation", "$relocationPackage.guava_jsr305")
    // guava ->depends checker-qual:3.12.0
    relocate("org.checkerframework", "$relocationPackage.guava_checker_qual")
    // httpclient5:5.1.3
    relocate("org.apache.hc.client5", "$relocationPackage.httpclient5")
    relocate("mozilla", "$relocationPackage.httpclient5.mozilla")
    // httpclient5 ->depends httpcore5:5.1.3
    relocate("org.apache.hc.core5", "$relocationPackage.httpclient5_httpcore5")
    // httpclient5 ->depends commons-codec:1.15
    relocate("org.apache.commons.codec", "$relocationPackage.httpclient5_commons_codec")
    // Note: httpclient5 ->depends on slf4j-api:1.7.32 but it doesn't make sense to relocate it so we don't
    // Leave gson and json-simple alone
}
