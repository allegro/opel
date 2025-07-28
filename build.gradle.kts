plugins {
    `java-library`
    groovy
    jacoco
    id("publishing-conventions")
    id("benchmark-conventions")
    alias(libs.plugins.axion.release)
    alias(libs.plugins.test.logger)
}

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.parboiled:parboiled-java:1.4.1")
    api("org.slf4j:slf4j-api:2.0.17")
    testImplementation("org.spockframework:spock-core:2.4-M6-groovy-4.0")
}

group = "pl.allegro.tech"
version = scmVersion.version

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
