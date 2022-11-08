plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
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
