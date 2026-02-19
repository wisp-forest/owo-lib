plugins {
    application
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {}

version = "0.1.0"
group = "io.wispforest"

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    val env = System.getenv()
    if (env.contains("MAVEN_URL")) {
        repositories {
            maven {
                url = uri(env["MAVEN_URL"]!!)
                credentials {
                    username = env["MAVEN_USER"]
                    password = env["MAVEN_PASSWORD"]
                }
            }
        }
    }
}