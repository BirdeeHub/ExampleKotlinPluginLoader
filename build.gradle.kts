plugins {
    kotlin("jvm") version "1.8.10"
}
repositories {
    mavenCentral()
}
tasks {
    clean {
        delete("outputDir/")
    }
}