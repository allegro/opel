plugins {
    id("me.champeau.jmh")
}

jmh {
    jmhVersion = "1.14.1"
}

dependencies {
    jmh("org.slf4j:slf4j-simple:1.7.32")
}
