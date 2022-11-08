plugins {
    `java-library`
    id("panther.shadow-conventions")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveClassifier.set("relocated")

    // Minimize guava anyway (see https://imperceptiblethoughts.com/shadow/configuration/minimizing/#:~:text=Dependencies%20scoped%20as%20api)
    minimize()

    // Relocate gson, guava (what's left) and json-simple
    // gson first
    relocate("com.google.gson", "io.github.sanctum.panther.relocated.gson")
    // then guava
    relocate("com.google", "io.github.sanctum.panther.relocated.guava.google")
    // guava -> jsr305:3.0.2
    relocate("javax.annotation", "io.github.sanctum.panther.relocated.guava.jsr305")
    // guava -> checker_qual:3.12.0
    relocate("org.checkerframework", "io.github.sanctum.panther.relocated.guava.checker_qual")
    // json-simple
    relocate("org.json.simple", "io.github.sanctum.panther.relocated.json_simple")
}
