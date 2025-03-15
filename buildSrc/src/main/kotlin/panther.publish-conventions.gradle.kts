import java.util.Base64

plugins {
    `java-library`
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:reference", true)
    options.quiet()
}

// un-wire sourcesJar and javadocJar from normal assemble
tasks.named("assemble") {
    setDependsOn(dependsOn.filterNot {
        (it as? Named)?.name in listOf("sourcesJar", "javadocJar")
    })
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn.add(tasks.named("sourcesJar"))
    dependsOn.add(tasks.named("javadocJar"))
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>(name) {
                // if are on an Actions runner, set up GitHub Packages
                if (System.getenv("GITHUB_ACTIONS") == "true") {
                    repositories {
                        maven {
                            name = "GitHubPackages"
                            url = uri("https://maven.pkg.github.com/the-h-team/Panther")
                            credentials {
                                username = System.getenv("GITHUB_ACTOR")
                                password = System.getenv("GITHUB_TOKEN")
                            }
                        }
                    }
                }
                pom {
                    description.set(
                        project.description.takeIf { it != rootProject.description } ?:
                        throw IllegalStateException("Set the project description in ${project.projectDir.name}/build.gradle.kts before activating publishing.")
                    )
                    url.set(
                        project.properties["url"] as String? ?:
                        throw IllegalStateException("Set the project URL as the Gradle project property 'url' before activating publishing.")
                    )
                    inceptionYear.set(
                        project.properties["inceptionYear"] as String? ?:
                        throw IllegalStateException("Set the project inception year as the Gradle project property 'inceptionYear' before activating publishing.")
                    )
                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                            distribution.set("repo")
                        }
                    }
                    organization {
                        name.set("Sanctum")
                        url.set("https://github.com/the-h-team")
                    }
                    developers {
                        developer {
                            id.set("ms5984")
                            name.set("Matt")
                            url.set("https://github.com/ms5984")
                        }
                        developer {
                            id.set("Hempfest")
                            name.set("Austin")
                            url.set("https://github.com/Hempfest")
                        }
                        developer {
                            id.set("Rigobert0")
                            name.set("Frido")
                            url.set("https://github.com/Rigobert0")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/the-h-team/Panther.git")
                        developerConnection.set("scm:git:ssh://github.com/the-h-team/Panther.git")
                        url.set("https://github.com/the-h-team/Panther/tree/master")
                    }
                }
                from(components["java"])
            }
            if (hasProperty("signingKeyPassphrase")) {
                apply(plugin = "signing")
                configure<SigningExtension> {
                    useInMemoryPgpKeys(
                        base64Decode(findProperty("base64SigningKey") as String?),
                        findProperty("signingKeyPassphrase") as String
                    )
                    sign(publishing.publications[name])
                }
            }
        }
    }
}

fun base64Decode(base64: String?) : String? {
    if (base64 == null) return null
    return Base64.getDecoder().decode(base64).toString(Charsets.UTF_8)
}
