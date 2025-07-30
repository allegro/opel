plugins {
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin")
    signing
}

publishing {
    afterEvaluate {
        publications {
            withType<MavenPublication> {
                pom {
                    name.set(project.name)
                    description.set("Asynchronous expression language")
                    url.set("https://github.com/allegro/opel")
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                    developers {
                        developer {
                            id.set("opel-developers")
                            name.set("OPEL-DEVELOPERS")
                        }
                    }
                    scm {
                        connection.set("scm:git@github.com:allegro/opel.git")
                        developerConnection.set("scm:git@github.com:allegro/opel.git")
                        url.set("https://github.com/allegro/opel")
                    }
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

signing {
    setRequired {
        System.getenv("GPG_KEY_ID") != null
    }
    useInMemoryPgpKeys(
        System.getenv("GPG_KEY_ID"),
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PRIVATE_KEY_PASSWORD")
    )
    sign(publishing.publications)
}
