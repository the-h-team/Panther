import java.util.Base64

plugins {
    `java-library`
    `maven-publish`
}

java {
    withJavadocJar()
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:reference", true)
    options.quiet()
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>(name) {
                pom {
                    description.set(project.description!!)
                    url.set(project.properties["url"] as String)
                    inceptionYear.set(project.properties["inceptionYear"] as String)
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
