plugins {
    java
}

group = "com.beatnetwork"
version = providers.gradleProperty("pluginVersion").get()

val paperApiVersion = providers.gradleProperty("paperApiVersion").get()
val minecraftApiVersion = providers.gradleProperty("minecraftApiVersion").get()

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    filteringCharset = "UTF-8"

    val props = mapOf(
        "version" to project.version,
        "apiVersion" to minecraftApiVersion
    )

    inputs.properties(props)

    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("BeatNetwork-Core")
}
