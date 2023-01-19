plugins {
    `java-library`
}

// Release notes: https://github.com/google/guava/releases/tag/v31.1
val guavaVersion by extra("31.1-jre")

dependencies {
    // Hide guava from consumers
    implementation("com.google.guava:guava:$guavaVersion") //TODO: remove or narrow further
}
