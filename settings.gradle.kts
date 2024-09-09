rootProject.name = "Panther"
sequenceOf(
    "placeholders",
    "containers",
    "common",
    "paste"
).associateBy {
    ":panther-$it"
}.forEach { (name, path) ->
    include(name)
    project(name).projectDir = file(path)
}

plugins {
    // Enables easy resolution of a compatible jdk for subprojects
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // guava - Release notes: https://github.com/google/guava/releases/tag/v31.1
            version("guava", "31.1-jre")
            library("guava", "com.google.guava", "guava").versionRef("guava")
        }
    }
}