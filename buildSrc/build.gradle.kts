plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("me.champeau.jmh:me.champeau.jmh.gradle.plugin:0.7.2")
    implementation("io.github.gradle-nexus.publish-plugin:io.github.gradle-nexus.publish-plugin.gradle.plugin:2.0.0")
}

repositories {
    gradlePluginPortal()
}
