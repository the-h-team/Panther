@file:Suppress("GradlePackageUpdate")

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("panther.java-conventions")
    id("panther.guava-conventions")
    id("panther.publish-conventions")
    id("panther.external-shadow-conventions")
}

dependencies {
    // Expose upstream libraries to consumers
    api("com.google.code.gson:gson:2.9.0")
    api(
        "com.googlecode.json-simple",
        "json-simple",
        "1.1.1"
    ) {
        // prevent dragging in junit and therefore hamcrest
        exclude(group = "junit", module = "junit")
    }
    // Expose "panther-container" to consumers
    api(project(":panther-containers"))
}

description = "panther-common"