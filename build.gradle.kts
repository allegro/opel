plugins {
    `java-library`
    groovy
    jacoco
    `maven-publish`
    signing
    id("pl.allegro.tech.build.axion-release") version "1.13.3"
    id("com.adarshr.test-logger") version "3.0.0"
    id("me.champeau.gradle.jmh") version "0.5.3"
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.parboiled:parboiled-java:1.3.1")
    implementation("org.slf4j:slf4j-api:1.7.32")

    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.9")

    jmh("org.slf4j:slf4j-simple:1.7.32")
}

group = "pl.allegro.tech"
version = scmVersion.version

tasks {
    jar {
        manifest {
            attributes(mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

jmh {
    jmhVersion = "1.14.1"
}

publishing {
    publications {
        create<MavenPublication>("sonatype") {
            artifactId = "opel"
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("opel")
                description.set("Asynchronous expression language")
                url.set("https://github.com/allegro/opel")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
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
    repositories {
        maven {
            credentials {
                username = project.properties.get("SONATYPE_USERNAME") as String?
                password = project.properties.get("SONATYPE_PASSWORD") as String?
            }
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

System.getenv("GPG_KEY_ID")?.let {
    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_KEY_ID"),
            System.getenv("GPG_PRIVATE_KEY"),
            System.getenv("GPG_PRIVATE_KEY_PASSWORD")
        )
        sign(publishing.publications)
    }
}
