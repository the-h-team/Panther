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

val gsonVersion by extra("2.9.0")

dependencies {
    // Expose upstream libraries to consumers
    api("com.google.code.gson:gson:$gsonVersion")
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
    // Hide httpclient5 from consumers
    implementation("org.apache.httpcomponents.client5", "httpclient5", "5.1.3") {
        // don't drag in httpcore5-h2 because we aren't using it
        exclude(group = "org.apache.httpcomponents.core5", module = "httpcore5-h2")
    }
}

description = "The main library of Panther"
