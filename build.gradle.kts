plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

tasks.wrapper {
    gradleVersion = "8.0.2"
    distributionType = Wrapper.DistributionType.ALL
}

nexusPublishing {
    repositories {
        create("sonatype") {
            val instance = findProperty("sonatype.instance") as String
            nexusUrl.set(uri("$instance${findProperty("sonatype.nexusPath")!!}"))
            snapshotRepositoryUrl.set(uri("$instance${findProperty("sonatype.snapshotsPath")!!}"))
        }
    }
}
