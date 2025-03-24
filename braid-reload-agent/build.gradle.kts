plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.jar {
    manifest.attributes(
        "Premain-Class" to "io.wispforest.BraidReloadAgent"
    )
}
