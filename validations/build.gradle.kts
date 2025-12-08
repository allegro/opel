plugins {
    id("java")
    groovy
}

group = "pl.allegro.tech"
version = "1.1.17-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    testImplementation("org.spockframework:spock-core:2.4-M6-groovy-4.0")
}

tasks.test {
    useJUnitPlatform()
}