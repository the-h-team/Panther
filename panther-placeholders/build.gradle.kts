/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("panther.java-conventions")
    id("panther.publish-conventions")
    id("panther.shadow-conventions")
}

dependencies {
    // Expose "panther-containers" to consumers
    api(project(":panther-containers"))
}

description = "A text replacement library"
