rootProject.name = "Panther"
sequenceOf(
    "annotations",
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
        // TODO
    }
}