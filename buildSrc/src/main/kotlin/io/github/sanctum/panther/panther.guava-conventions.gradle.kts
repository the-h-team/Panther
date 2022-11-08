plugins {
    `java-library`
}

dependencies {
    // Hide guava from consumers
    implementation("com.google.guava:guava:31.1-jre") //TODO: remove or narrow further
}
