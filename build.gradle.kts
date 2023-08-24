plugins {
    kotlin("jvm") version "1.8.10"
}
repositories {
    mavenCentral()
    gradlePluginPortal()
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
tasks {
    clean {
        delete("ExampleOut/")
        delete("plugins/")
    }
}